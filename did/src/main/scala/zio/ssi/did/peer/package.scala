package zio.ssi.did

import org.bitcoinj.core.Base58
import zio.ssi.did.internal.{Codec => ICodec, DidOps => IDidOps}
import zio.{Task, UIO, ULayer, ZIO, ZLayer}

import java.net.URI
import java.security.MessageDigest
import java.util.UUID
import scala.util.chaining._
import scala.util.matching.Regex

package object peer {
  type Identifier = PeerDid.Identifier
  type PeerDidOps = DidOps[Identifier]
  type PeerDidCodec = DidCodec[Identifier]
  type PeerDid = Did[Identifier]
  type PeerDoc = DidDoc[Identifier]

  object PeerDidOps {
    val uri: ULayer[PeerDidOps] =
      ZLayer.succeed {
        new IDidOps[Identifier] {
          override def create(key: Array[Byte]): UIO[PeerDoc] = {
            val did =
              key
                .pipe(MessageDigest.getInstance("SHA-256").digest)
                .pipe(Array[Byte](0x12, 0x20) ++ _)
                .pipe(Base58.encode)
                .pipe(PeerDid(PeerDid.NumAlgo.Empty, Did.Transform.Base58BTC, _))

            val publicKey =
              key
                .pipe(Base58.encode)
                .pipe(DidDoc.PublicKey.KeyValue.PublicKeyBase58)
                .pipe(DidDoc.PublicKey("X25519", new URI("#id"), _))

              DidDoc(did, Map(UUID.randomUUID() -> publicKey), Map.empty)
                .pipe(ZIO.succeed(_))
          }

          override def resolve(did: PeerDid): Task[PeerDoc] =
            did
              .pipe(DidDoc(_, Map.empty, Map.empty))
              .pipe(ZIO.succeed(_))
        }
      }
  }

  object PeerDidCodec {
    val live: ULayer[PeerDidCodec] =
      ZLayer.succeed {
        new ICodec[URI, PeerDid] {
          private val pattern: Regex = "^did:peer:([01])(z)([1-9a-km-zA-HJ-NP-Z]{46,47})$".r

          override def from(value: URI): Task[PeerDid] =
            value.toString match {
              case pattern("0", _, enc) =>
                enc
                  .pipe(PeerDid(PeerDid.NumAlgo.Empty, Did.Transform.Base58BTC, _))
                  .pipe(ZIO.succeed(_))

              case pattern("1", _, enc) =>
                enc
                  .pipe(PeerDid(PeerDid.NumAlgo.Genesis, Did.Transform.Base58BTC, _))
                  .pipe(ZIO.succeed(_))

              case _ =>
                new IllegalArgumentException(f"'$value' is not a valid DID")
                  .pipe(ZIO.fail(_))
            }

          override def to(value: PeerDid): UIO[URI] =
            value match {
              case Did(method, PeerDid.Identifier(PeerDid.NumAlgo.Empty, _, enc)) =>
                List(Did.URI_SCHEME, method, "0z" + enc)
                  .mkString(":")
                  .pipe(new URI(_))
                  .pipe(ZIO.succeed(_))

              case Did(method, PeerDid.Identifier(PeerDid.NumAlgo.Genesis, _, enc)) =>
                List(Did.URI_SCHEME, method, "1z" + enc)
                  .mkString(":")
                  .pipe(new URI(_))
                  .pipe(ZIO.succeed(_))
            }
        }
      }
  }
}
