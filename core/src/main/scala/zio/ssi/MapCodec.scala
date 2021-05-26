package zio.ssi

import zio.{Has, RIO, Task, UIO, ULayer, URIO, ZIO, ZLayer}

trait MapCodec[A]:
  def read(values: Map[String, Any]): Task[A]
  def write(a: A): UIO[Map[String, Any]]

object MapCodec:
  val identity: ULayer[Has[MapCodec[Map[String, Any]]]] =
    ZLayer.succeed {
      new MapCodec[Map[String, Any]]:
        override def read(values: Map[String, Any]) =
          ZIO.succeed(values)
        
        override def write(a: Map[String, Any]) =
          ZIO.succeed(a)
    }
