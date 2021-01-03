package zio.ssi.hydrogen

import co.libly.hydride.Hydrogen
import zio.{Has, Task, UIO, URIO, URLayer, ZIO, ZLayer}

package object signature {
  type Signature = Has[Signature.Service]

  object Signature {
    trait Service {
      def generateKeyPair: UIO[Hydrogen.HydroSignKeyPair]
      def sign(context: Array[Byte])(secretKey: Array[Byte])(message: Array[Byte]): UIO[Array[Byte]]
      def verify(context: Array[Byte])(publicKey: Array[Byte])(message: Array[Byte])(signature: Array[Byte]): UIO[Boolean]
    }

    val live: URLayer[Has[Hydrogen], Signature] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def generateKeyPair: UIO[Hydrogen.HydroSignKeyPair] =
            UIO.effectTotal {
              val keyPair = new Hydrogen.HydroSignKeyPair()
              ctx.get.hydro_sign_keygen(keyPair)
              keyPair
            }

          override def sign(context: Array[Byte])(secretKey: Array[Byte])(message: Array[Byte]): UIO[Array[Byte]] =
            UIO.effectTotal {
              val signature = Array.ofDim[Byte](Hydrogen.HYDRO_SIGN_BYTES)
              ctx.get.hydro_sign_create(signature, message, message.length, context, secretKey)
              signature
            }

          override def verify(context: Array[Byte])(publicKey: Array[Byte])(message: Array[Byte])(signature: Array[Byte]): UIO[Boolean] =
            Task.effectTotal {
              ctx.get.hydro_sign_verify(signature, message, message.length, context, publicKey) match {
                case 0 => true
                case 1 => false
              }
            }
        }
      }
  }

  def generateKeyPair: ZIO[Signature, Nothing, Hydrogen.HydroSignKeyPair] =
    ZIO.accessM(_.get.generateKeyPair)

  def sign(context: Array[Byte])(secretKey: Array[Byte])(message: Array[Byte]): URIO[Signature, Array[Byte]] =
    ZIO.accessM(_.get.sign(context)(secretKey)(message))

  def verify(context: Array[Byte])(publicKey: Array[Byte])(message: Array[Byte])(signature: Array[Byte]): URIO[Signature, Boolean] =
    ZIO.accessM(_.get.verify(context)(publicKey)(message)(signature))
}
