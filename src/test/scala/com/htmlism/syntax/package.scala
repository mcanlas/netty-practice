package com.htmlism

import scala.jdk.CollectionConverters._

import cats.effect._
import cats.implicits._
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.{ HttpObjectAggregator, HttpResponseDecoder }
import io.netty.util.ReferenceCounted

package object syntax {
  implicit class DecodingOps(chn: EmbeddedChannel) {
    def safeReadInbound[A <: ReferenceCounted, B](f: A => B): B =
      Resource
        .make(acquire[A](chn))(release)
        .use(a => IO(f(a)))
        .unsafeRunSync()
  }

  private def acquire[A](chn: EmbeddedChannel) =
    IO {
      val decodingChannel =
        new EmbeddedChannel(new HttpResponseDecoder, new HttpObjectAggregator(Int.MaxValue))

      chn.outboundMessages().asScala.foreach { _ =>
        val bytePayload = chn.readOutbound[AnyRef]()

        assert(decodingChannel.writeInbound(bytePayload), "decoding channel inbound queue modified")
      }

      assert(decodingChannel.inboundMessages().size == 1, "decoding channel inbound queue has one response")
      assert(decodingChannel.outboundMessages().size == 0, "decoding channel outbound queue is empty")

      decodingChannel
        .readInbound[A]
    }

  private def release[A <: ReferenceCounted](x: A) =
    IO(x.release)
      .void
}
