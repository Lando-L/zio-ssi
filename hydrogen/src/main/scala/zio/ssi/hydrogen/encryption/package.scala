package zio.ssi.hydrogen

import co.libly.hydride.Hydrogen
import zio.{Has, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.nio.charset.StandardCharsets
import scala.util.chaining._

package object encryption {
  type Encryption = Has[Encryption.Service]

  object Encryption {
    trait Service {
      def generateKey: UIO[Array[Byte]]
      def encrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(message: Array[Byte]): UIO[Array[Byte]]
      def decrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(cipher: Array[Byte]): Task[Array[Byte]]
    }

    val live: URLayer[Has[Hydrogen], Encryption] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def generateKey: UIO[Array[Byte]] =
            UIO.effectTotal {
              val key = Array.ofDim[Byte](Hydrogen.HYDRO_SECRETBOX_KEYBYTES)
              ctx.get.hydro_secretbox_keygen(key)
              key
            }

          override def encrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(message: Array[Byte]): UIO[Array[Byte]] =
            UIO.effectTotal {
              val cipher = Array.ofDim[Byte](message.length + Hydrogen.HYDRO_SECRETBOX_HEADERBYTES)
              ctx.get.hydro_secretbox_encrypt(cipher, message, message.length, id, context, key)
              cipher
            }

          override def decrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(cipher: Array[Byte]): Task[Array[Byte]] =
            Task
              .effectTotal {
                val message = Array.ofDim[Byte](cipher.length - Hydrogen.HYDRO_SECRETBOX_HEADERBYTES)
                ctx.get.hydro_secretbox_decrypt(message, cipher, cipher.length, id, context, key) match {
                  case 0 => Right(message)
                  case _ => Left(new IllegalArgumentException("Decryption failed"))
                }
              }
              .pipe(ZIO.absolve)
        }
      }
  }

  val context: Array[Byte] = "__auth__".getBytes(StandardCharsets.UTF_8)

  def generateKey: URIO[Encryption, Array[Byte]] =
    ZIO.accessM(_.get.generateKey)

  def encrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(message: Array[Byte]): URIO[Encryption, Array[Byte]] =
    ZIO.accessM(_.get.encrypt(context)(key)(id)(message))

  def decrypt(context: Array[Byte])(key: Array[Byte])(id: Long)(cipher: Array[Byte]): ZIO[Encryption, Throwable, Array[Byte]] =
    ZIO.accessM(_.get.decrypt(context)(key)(id)(cipher))
}
