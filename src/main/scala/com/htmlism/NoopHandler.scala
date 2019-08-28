package com.htmlism

import io.netty.channel.{ Channel, ChannelHandlerContext, ChannelInboundHandlerAdapter, ChannelInitializer }

object NoopHandler extends ChannelInboundHandlerAdapter

class EchoHandler extends ChannelInboundHandlerAdapter {
  override def channelRead(ctx: ChannelHandlerContext, msg: AnyRef): Unit = {
    println("hello " + util.Random.nextInt())
    println(ctx)
    println("msg: " + msg)

    forEffect {
      ctx
        .writeAndFlush {
          msg
        }
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

    forEffect {
      ctx
        .writeAndFlush {
          msg
        }
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

    forEffect {
      ctx.fireChannelRead(msg)
    }
  }
}

object PipelineBuilder extends ChannelInitializer[Channel] {
  def initChannel(ch: Channel): Unit =
    forEffect {
      ch.pipeline()
        .addLast(new NoseyHandler("foo"), new NoseyHandler("bar"), new LastMile)
    }
}
