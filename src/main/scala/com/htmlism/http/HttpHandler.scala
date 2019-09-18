package com.htmlism
package http

import io.netty.channel.{ ChannelHandlerContext, SimpleChannelInboundHandler }
import io.netty.handler.codec.http._

class HttpHandler extends SimpleChannelInboundHandler[HttpRequest] {
  def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit =
    forEffect {
      val response =
        new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK)

      response
        .headers()
//        .add(HttpHeaderNames.DATE, "asdf \", , , ,, ")

      ctx
        .writeAndFlush(response)

      ctx
        .close()
    }
}
