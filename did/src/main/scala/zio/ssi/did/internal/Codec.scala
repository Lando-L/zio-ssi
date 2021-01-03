package zio.ssi.did.internal

import zio.{Task, UIO}

private[did] trait Codec[A, B] {
  def from(value: A): Task[B]
  def to(value: B): UIO[A]
}

private[did] object Codec {
  def apply[A, B](implicit codec: Codec[A, B]): Codec[A, B] = codec

  def from[A, B](value: A)(implicit codec: Codec[A, B]): Task[B] =
    codec.from(value)

  def to[A, B](value: B)(implicit codec: Codec[A, B]): Task[A] =
    codec.to(value)
}
