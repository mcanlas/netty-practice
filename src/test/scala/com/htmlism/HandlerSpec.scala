package com.htmlism

import scala.jdk.CollectionConverters._

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ ChannelHandlerContext, ChannelInboundHandler, ChannelInboundHandlerAdapter, SimpleChannelInboundHandler }
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import org.scalatest._

object HandlerSpec {
  def httpRequest: DefaultFullHttpRequest =
    new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1,
      HttpMethod.GET,
      "/unknown"
    )
}

class HandlerSpec extends FunSuite with Matchers {
  implicit class ChannelOps(chn: EmbeddedChannel) {
    def readOutboundHttp[A]: A = {
      val decodingChannel = new EmbeddedChannel(new HttpResponseDecoder, EchoHandler)
      println(decodingChannel.pipeline().names())

      chn.outboundMessages().asScala.foreach { _ =>
        println("writing...")

        val thing = chn.readOutbound[AnyRef]()
        println(thing)

        decodingChannel.writeInbound(thing)
      }

      val ret = decodingChannel.readOutbound[A]
      println("after decoding sees " + ret)

      decodingChannel.finishAndReleaseAll()

      ret
    }

    def writeInboundHttp[A](x: A): Boolean = {
      println("raw payload was " + x)
      val encodingChannel = new EmbeddedChannel(new HttpRequestEncoder)

      println {
        encodingChannel.writeOutbound(x)
      }

      val buf = encodingChannel.readOutbound[AnyRef]
      println("encoded payload became " + buf)

      chn.writeInbound(buf)
    }
  }

  ignore("outbound") {
//    val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(65536), EchoHandler)
    val chn = new EmbeddedChannel(InboundNoopHandler)

    println{
      chn.writeOutbound {
        new DefaultFullHttpRequest(
          HttpVersion.HTTP_1_1,
          HttpMethod.GET,
          "/unknown"
        ): FullHttpRequest
      }
    }

    println(chn.writeOutbound("hello"))


    val in = chn.readOutbound[DefaultFullHttpRequest]
    in.method() shouldBe HttpMethod.GET
    in.protocolVersion() shouldBe HttpVersion.HTTP_1_1

//    val res = chn.readOutboundHttp[DefaultFullHttpResponse]
//
//    res.status() shouldBe HttpResponseStatus.OK
//    res.protocolVersion() shouldBe HttpVersion.HTTP_1_1
  }

  test("simple") {
    val chn = new EmbeddedChannel()

    chn.writeInbound(123) shouldBe true
    chn.readInbound[Int] shouldBe 123

    chn.writeOutbound(456) shouldBe true
    chn.readOutbound[Int] shouldBe 456
  }



  test("echo handler with http") {
    val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(65536), IncrementOrPass)

    assert { chn.writeInbound(123) }
    chn.readInbound[Int] shouldBe 124

    chn.writeInbound(777) shouldBe true
    chn.readInbound[Int] shouldBe 778

    chn.writeInbound(HandlerSpec.httpRequest) shouldBe true
    chn.readInbound[FullHttpRequest] shouldBe HandlerSpec.httpRequest

    assert { chn.writeOutbound(456) }
    chn.readOutbound[Int] shouldBe 456
  }

  test("write one event, receive a different one") {
    val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(65536), IgnoreThenWrite)

    chn.writeInbound(123) // inbound boolean intentionally not tested, given implementation
    chn.readOutbound[String] shouldBe "hello 123"

    chn.writeInbound(456) // inbound boolean intentionally not tested, given implementation
    chn.readOutbound[String] shouldBe "hello 456"
  }

  test("write an in-memory http request, receive an http response") {
    val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(65536), HttpResponder)

    chn.writeInbound(HandlerSpec.httpRequest) // inbound boolean intentionally not tested, given implementation

    val res = chn.readOutboundHttp[HttpResponse]
    res.status() shouldBe HttpResponseStatus.OK
  }

  test("write byte-buf-based http request, receive an http response") {
    val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(65536), HttpResponder)

    chn.writeInboundHttp(HandlerSpec.httpRequest) // inbound boolean intentionally not tested, given implementation

    val res = chn.readOutboundHttp[HttpResponse]
    res.status() shouldBe HttpResponseStatus.OK
  }
}

object IgnoreThenWrite extends SimpleChannelInboundHandler[Int] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: Int): Unit =
    forEffect {
      ctx.writeAndFlush("hello " + msg.toString)
    }
}

object IncrementOrPass extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit =
    forEffect {
      msg match {
        case n: Int =>
          ctx.fireChannelRead(n + 1)
        case _ =>
          ctx.fireChannelRead(msg)
      }
    }
}

@Sharable
object HttpResponder extends SimpleChannelInboundHandler[FullHttpRequest] {
  def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val res = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.OK)

    forEffect {
      ctx.writeAndFlush(res)
    }
  }
}

object InboundNoopHandler extends SimpleChannelInboundHandler[AnyRef] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = ()
}

@Sharable
object EchoHandler extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    println("echo handler sees " + msg)

    forEffect {
      ctx.writeAndFlush(msg)
    }
  }
}

object PrintHandler extends ChannelInboundHandler {
  override def channelRegistered(ctx: ChannelHandlerContext): Unit =
    println("registered")

  override def channelUnregistered(ctx: ChannelHandlerContext): Unit =
    println("unregistered")

  override def channelActive(ctx: ChannelHandlerContext): Unit =
    println("active")

  override def channelInactive(ctx: ChannelHandlerContext): Unit =
    println("inactive")

  override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
    println("Channel read is happening: " + msg)

    forEffect {
      ctx.fireChannelRead(msg)
    }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit =
    println("read complete")

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit =
    println("user triggered")

  override def channelWritabilityChanged(ctx: ChannelHandlerContext): Unit =
    println("writability")

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit =
    println("exception " + cause.toString)

  override def handlerAdded(ctx: ChannelHandlerContext): Unit =
    println("added")

  override def handlerRemoved(ctx: ChannelHandlerContext): Unit =
    println("removed")
}
