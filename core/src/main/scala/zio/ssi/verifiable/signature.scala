package zio.ssi.verifiable.signature

import com.nimbusds.jose.{JOSEObjectType, JWSAlgorithm, JWSHeader, JWSSigner}
import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
import zio.ssi.verifiable.{Credential, Id, Issuer, Presentation, Subject, Timestamp}
import zio.ssi.MapCodec
import zio.{Has, RIO, Tag, Task, URLayer, ZIO, ZLayer}

trait Signature[A]:
  def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(credential: Credential[A]): Task[SignedJWT]
  def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(presentation: Presentation[A]): Task[SignedJWT]

object Signature:
  def live[A](using Tag[MapCodec[A]], Tag[Signature[A]]): URLayer[Has[MapCodec[A]], Has[Signature[A]]] =
    ZLayer.fromService[MapCodec[A], Signature[A]] { (codec: MapCodec[A]) =>
      new Signature[A]:
        import Id.*
        import Issuer.*
        import Subject.*
        import Timestamp.*

        override def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(credential: Credential[A]) =
          val header =
            JWSHeader
              .Builder(algorithm)
              .keyID(kid.toString)
              .`type`(JOSEObjectType.JWT)
              .build

          val payload =
            JWTClaimsSet
              .Builder()
              .jwtID(credential.id.toString)
              .issuer(credential.issuer.toString)
              .issueTime(credential.issued.toDate)
              .claim("nonce", nonce)
              .build

          ZIO.effect {
            val token = SignedJWT(header, payload)
            token.sign(signer)
            token
          }

        override def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(presentation: Presentation[A]) =
          ZIO.fail(RuntimeException())
    }

def sign[A](algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(credential: Credential[A])(using Tag[Signature[A]]): RIO[Has[Signature[A]], SignedJWT] =
  ZIO.serviceWith(_.sign(algorithm)(kid)(signer)(nonce)(credential))

def sign[A](algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(presentation: Presentation[A])(using Tag[Signature[A]]): RIO[Has[Signature[A]], SignedJWT] =
  ZIO.serviceWith(_.sign(algorithm)(kid)(signer)(nonce)(presentation))
