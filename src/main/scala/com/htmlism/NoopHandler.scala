package com.htmlism

import io.netty.buffer.ByteBuf
import io.netty.channel.{ Channel, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer }
import io.netty.handler.codec.http.{ DefaultFullHttpResponse, HttpObjectAggregator, HttpResponseStatus, HttpServerCodec, HttpVersion }

object NoopHandler extends ChannelInboundHandlerAdapter

class EchoHandler extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    println("hello " + util.Random.nextInt())
    println(ctx)
    println("msg: " + msg)

    ctx
      .writeAndFlush {
        msg
      }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
//    ctx.flush()
//    ctx.close()
  }
}

class LastMile extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    println("hello " + util.Random.nextInt())
    println(ctx)
    println("msg: " + msg)

    ctx
      .writeAndFlush {
        msg
      }
  }

  override def channelReadComplete(ctx: ChannelHandlerContext): Unit = {
    //    ctx.flush()
    //    ctx.close()
  }
}

class NoseyHandler(s: String) extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    println(s)
    ctx.fireChannelRead(msg)
  }
}

object PipelineBuilder extends ChannelInitializer[Channel] {
  def initChannel(ch: Channel): Unit = {
    ch.pipeline()
      .addLast(new NoseyHandler("foo"), new NoseyHandler("bar"), new LastMile)
  }
}
