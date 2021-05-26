package zio.ssi.verifiable.verification

import com.nimbusds.jose.JWSVerifier
import com.nimbusds.jwt.SignedJWT
import zio.{Has, RIO, Task, ULayer, ZIO, ZLayer}

trait Verification:
  def verify(verifier: JWSVerifier)(signedJWT: SignedJWT): Task[Boolean]

object Verification:
  val live: ULayer[Has[Verification]] =
    ZLayer.succeed {
      new Verification:
        override def verify(verifier: JWSVerifier)(signedJWT: SignedJWT) =
          ZIO.effect {
            signedJWT.verify(verifier)
          }
    }

def verify(verifier: JWSVerifier)(signedJWT: SignedJWT): RIO[Has[Verification], Boolean] =
  ZIO.serviceWith(_.verify(verifier)(signedJWT))
