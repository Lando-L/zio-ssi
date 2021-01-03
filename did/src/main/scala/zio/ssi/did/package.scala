package zio.ssi

import zio.{Has, RIO, Tag, URIO, ZIO}

import java.net.URI

package object did {
  type DidOps[A] = Has[internal.DidOps[A]]
  type DidCodec[A] = Has[internal.Codec[URI, Did[A]]]
  type DocCodec[A] = Has[internal.Codec[String, DidDoc[A]]]

  def create[A: Tag](key: Array[Byte]): URIO[DidOps[A], DidDoc[A]] =
    ZIO.accessM(_.get.create(key))

  def resolve[A: Tag](did: Did[A]): RIO[DidOps[A], DidDoc[A]] =
    ZIO.accessM(_.get.resolve(did))

  def extract[A: Tag](doc: DidDoc[A]): RIO[DidOps[A], Array[Byte]] =
    ZIO.accessM(_.get.extract(doc))



  def toDid[A: Tag](value: URI): RIO[DidCodec[A], Did[A]] =
    ZIO.accessM(_.get.from(value))

  def fromDid[A: Tag](value: Did[A]): RIO[DidCodec[A], URI] =
    ZIO.accessM(_.get.to(value))



  def toDoc[A: Tag](value: String): RIO[DocCodec[A], DidDoc[A]] =
    ZIO.accessM(_.get.from(value))

  def fromDoc[A: Tag](value: DidDoc[A]): RIO[DocCodec[A], String] =
    ZIO.accessM(_.get.to(value))
}
