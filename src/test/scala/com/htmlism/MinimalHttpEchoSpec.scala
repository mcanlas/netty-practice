package com.htmlism

import java.nio.charset.Charset

import scala.jdk.CollectionConverters._

import io.netty.buffer.Unpooled
import io.netty.channel.{ ChannelHandlerContext, SimpleChannelInboundHandler }
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class MinimalHttpEchoSpec extends FunSuite with Matchers with ScalaCheckDrivenPropertyChecks {
  implicit val safeStringArbitrary: Arbitrary[String] =
    Arbitrary(Gen.alphaStr)

  def httpRequest: DefaultFullHttpRequest =
    new DefaultFullHttpRequest(
      HttpVersion.HTTP_1_1,
      HttpMethod.GET,
      "/unknown"
    )

  test("write an in-memory http request, receive an http response") {
    forAll { s: String =>
      val chn = new EmbeddedChannel(new HttpServerCodec, new HttpObjectAggregator(Int.MaxValue), new StringHttpResponder(s))

      assert(!chn.writeInbound(HandlerSpec.httpRequest), "inbound buffer not modified")
      assert(chn.inboundMessages().size == 0, "inbound queue is empty")
      assert(chn.outboundMessages().size > 0, "outbound queue is not empty")

      val decodingChannel =
        new EmbeddedChannel(new HttpResponseDecoder, new HttpObjectAggregator(Int.MaxValue))

      chn.outboundMessages().asScala.foreach { d =>
        val bytePayload = chn.readOutbound[AnyRef]()

        assert(decodingChannel.writeInbound(bytePayload), "decoding channel inbound queue modified")
      }

      assert(decodingChannel.inboundMessages().size == 1, "decoding channel inbound queue has one response")
      assert(decodingChannel.outboundMessages().size == 0, "decoding channel outbound queue is empty")

      decodingChannel
        .readInbound[FullHttpResponse]
        .content()
        .toString(Charset.defaultCharset()) shouldBe s
    }
  }
}

class StringHttpResponder(s: String) extends SimpleChannelInboundHandler[FullHttpRequest] {
  def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val buf = Unpooled.wrappedBuffer(s.getBytes)

    val res = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.CREATED,
      buf)

    res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
    res.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, res.content().readableBytes());

    ctx.writeAndFlush(res)
  }
}
