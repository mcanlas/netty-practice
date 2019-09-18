package com.htmlism

import cats.effect._
import cats.implicits._

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.socket.nio.NioServerSocketChannel

object HttpServer extends IOApp {
  def run(args: List[String]): IO[ExitCode] =
    Hello.bossWorker[IO].use { case (boss, worker) =>
      IO {
        println("before")

        val f =
          new ServerBootstrap()
            .group(boss, worker)
            .channel(classOf[NioServerSocketChannel])
            .childHandler(new http.HttpInitializer)
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
