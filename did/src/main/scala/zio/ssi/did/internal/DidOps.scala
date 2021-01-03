package zio.ssi.did.internal

import org.bitcoinj.core.Base58
import zio.{Task, UIO, ZIO}
import zio.ssi.did.{Did, DidDoc}

import scala.util.chaining._

private[did] trait DidOps[T] {
  def create(key: Array[Byte]): UIO[DidDoc[T]]
  def resolve(did: Did[T]): Task[DidDoc[T]]

  def extract(doc: DidDoc[T]): Task[Array[Byte]] = {
    import DidDoc.PublicKey.KeyValue._

    ZIO
      .fromOption(doc.publicKey.values.headOption)
      .orElseFail(new IllegalArgumentException("No publicKey found"))
      .map(_.keyValue)
      .flatMap {
        case PublicKeyBase58(underlying) =>
          underlying
            .pipe(Base58.decode)
            .pipe(ZIO.succeed(_))

        case _: PublicKeyHex =>
          ZIO.fail(new IllegalArgumentException("Hex encoding is not supported yet"))

        case _: PublicKeyPem =>
          ZIO.fail(new IllegalArgumentException("Pem encoding is not supported yet"))
      }
  }
}

private[did] object DidOps {
  def apply[T](implicit ops: DidOps[T]): DidOps[T] = ops

  def create[T](key: Array[Byte])(implicit ops: DidOps[T]): UIO[DidDoc[T]] =
    ops.create(key)

  def resolve[T](did: Did[T])(implicit ops: DidOps[T]): Task[DidDoc[T]] =
    ops.resolve(did)
}
