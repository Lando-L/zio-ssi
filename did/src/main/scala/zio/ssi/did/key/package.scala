package zio.ssi.did

import org.bitcoinj.core.Base58
import zio.ssi.did.internal.{Codec => ICodec, DidOps => IDidOps}
import zio.{Task, UIO, ULayer, ZIO, ZLayer}

import java.net.URI
import java.util.UUID
import scala.util.chaining._
import scala.util.matching.Regex

package object key {
  type Identifier = KeyDid.Identifier
  type KeyDidOps = DidOps[Identifier]
  type KeyDidCodec = DidCodec[Identifier]
  type KeyDid = Did[Identifier]
  type KeyDoc = DidDoc[Identifier]

  object KeyDidOps {
    val live: ULayer[KeyDidOps] =
      ZLayer.succeed {
        new IDidOps[Identifier] {
          override def create(key: Array[Byte]): UIO[KeyDoc] = {
            val did =
              key
                .pipe(0xec.toByte +: _)
                .pipe(Base58.encode)
                .pipe(KeyDid(Did.Transform.Base58BTC, _))

            val publicKey =
              key
                .pipe(Base58.encode)
                .pipe(DidDoc.PublicKey.KeyValue.PublicKeyBase58)
                .pipe(DidDoc.PublicKey("X25519", new URI("#id"), _))

            DidDoc(did, Map(UUID.randomUUID() -> publicKey), Map.empty)
              .pipe(ZIO.succeed(_))
          }

          override def resolve(did: KeyDid): Task[KeyDoc] =
            did match {
              case Did(_, KeyDid.Identifier(Did.Transform.Base58BTC, encKey)) =>
                Base58.decode(encKey)
                  .pipe {
                    case bytes if bytes.headOption.contains(0xec) =>
                      ZIO.succeed(bytes)

                    case _ =>
                      ZIO.fail(new IllegalArgumentException("Unsupported Key Type"))
                  }
                  .map(Base58.encode)
                  .map(DidDoc.PublicKey.KeyValue.PublicKeyBase58)
                  .map(DidDoc.PublicKey("X25519", new URI("#id"), _))
                  .map(key => DidDoc(did, Map(UUID.randomUUID() -> key), Map.empty))
            }
        }
      }
  }

  object KeyDidCodec {
    val live: ULayer[KeyDidCodec] =
      ZLayer.succeed {
        new ICodec[URI, KeyDid] {
          private val pattern: Regex = "^did:key:(z)([1-9a-km-zA-HJ-NP-Z]+)$".r

          override def from(value: URI): Task[KeyDid] =
            value.toString match {
              case pattern("z", enc) =>
                enc
                  .pipe(KeyDid(Did.Transform.Base58BTC, _))
                  .pipe(ZIO.succeed(_))

              case _ =>
                new IllegalArgumentException(s"'$value' is not a valid key-did")
                  .pipe(ZIO.fail(_))
            }

          override def to(value: KeyDid): UIO[URI] =
            value match {
              case Did(method, KeyDid.Identifier(Did.Transform.Base58BTC, encKey)) =>
                List(Did.URI_SCHEME, method, "z" + encKey)
                  .mkString(":")
                  .pipe(new URI(_))
                  .pipe(ZIO.succeed(_))
            }
        }
      }
  }
}
