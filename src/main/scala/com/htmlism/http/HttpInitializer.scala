package com.htmlism
package http

import scala.jdk.CollectionConverters._

import io.netty.channel.{ ChannelHandlerContext, ChannelInitializer, ChannelOutboundHandlerAdapter, ChannelPromise }
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.cors.{ CorsConfigBuilder, CorsHandler }

// curl -v -X OPTIONS --header "Origin: foo" --header "access-control-request-method: DELETE"  http://localhost:12345
class HttpInitializer extends ChannelInitializer[SocketChannel] {
  def initChannel(ch: SocketChannel): Unit =
    forEffect {
      val p = ch.pipeline

      val cfg = CorsConfigBuilder
        .forAnyOrigin()
        .allowedRequestMethods(HttpMethod.GET, HttpMethod.OPTIONS)
        .maxAge(1L)
        .allowCredentials()
        .preflightResponseHeader("zeroo", "world")
        .preflightResponseHeader("hello", "world, span")
//        .noPreflightResponseHeaders()
        .build()

      cfg.preflightResponseHeaders()
          .asScala.foreach(println)

      p.addLast(new HttpRequestDecoder())
      p.addLast(new HttpResponseEncoder())
      p.addLast(OutboundInspectorHandler)
      p.addLast(new CorsHandler(cfg))
      p.addLast(new HttpHandler())
    }
}

object OutboundInspectorHandler extends ChannelOutboundHandlerAdapter {
  override def write(ctx: ChannelHandlerContext, msg: Object, promise: ChannelPromise): Unit =
    forEffect {
      msg match {
        case x: DefaultFullHttpResponse =>
          x.headers.asScala.foreach(x => println(x.getValue))
//          println("read" + x.toString)
      }


      ctx.write(msg, promise)
    }
}
