package com.htmlism

import cats.effect._
import cats.implicits._

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio._
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.EventExecutorGroup

object Server extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Hello.bossWorker[IO].use { case (boss, worker) =>
      IO {
        println("before")

        val f =
          new ServerBootstrap()
            .group(boss, worker)
            .channel(classOf[NioServerSocketChannel])
            .childHandler(PipelineBuilder)
            .bind("0.0.0.0", 12345)
            .sync

        println(f.getClass)

        f
          .channel()
          .closeFuture()
          .sync

        println("after")

      }.as(ExitCode.Success)
    }
}

object Hello {
  private def shutdownGracefully[F[_]](g: EventExecutorGroup)(implicit F: Sync[F]) =
    F.delay {
      println("shutting down")
      g.shutdownGracefully()
    }.void


  def newGroup[F[_]](implicit F: Sync[F]): Resource[F, NioEventLoopGroup] =
    Resource.make {
      F.delay(new NioEventLoopGroup())
    }(shutdownGracefully[F])

  def bossWorker[F[_] : Sync]: Resource[F, (NioEventLoopGroup, NioEventLoopGroup)] =
    for {
      boss   <- newGroup[F]
      worker <- newGroup[F]
    } yield (boss, worker)
}
