package zio.ssi.hydrogen.exchange

import co.libly.hydride.Hydrogen
import zio.{Has, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import scala.util.chaining._

package object kkvariant {
  type KKVariant = Has[KKVariant.Service]

  object KKVariant {
    trait Service {
      def request(sender: Hydrogen.HydroKxKeyPair)(receiver: Array[Byte]): UIO[(Hydrogen.HydroKxState, Array[Byte])]
      def response(sender: Hydrogen.HydroKxKeyPair)(requester: Array[Byte])(packet: Array[Byte]): Task[(Hydrogen.HydroKxSessionKeyPair, Array[Byte])]
      def complete(sender: Hydrogen.HydroKxKeyPair)(state: Hydrogen.HydroKxState)(packet: Array[Byte]): Task[Hydrogen.HydroKxSessionKeyPair]
    }

    val live: URLayer[Has[Hydrogen], KKVariant] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def request(sender: Hydrogen.HydroKxKeyPair)(receiver: Array[Byte]): UIO[(Hydrogen.HydroKxState, Array[Byte])] =
            UIO.effectTotal {
              val state = new Hydrogen.HydroKxState()
              val packet = Array.ofDim[Byte](Hydrogen.HYDRO_KX_KK_PACKET1BYTES)
              ctx.get.hydro_kx_kk_1(state, packet, receiver, sender)
              (state, packet)
            }

          override def response(sender: Hydrogen.HydroKxKeyPair)(requester: Array[Byte])(packet: Array[Byte]): Task[(Hydrogen.HydroKxSessionKeyPair, Array[Byte])] =
            Task
              .effectTotal {
                val session = new Hydrogen.HydroKxSessionKeyPair()
                val nextPacket = Array.ofDim[Byte](Hydrogen.HYDRO_KX_KK_PACKET2BYTES)
                ctx.get.hydro_kx_kk_2(session, nextPacket, packet, requester, sender) match {
                  case 0 => Right((session, nextPacket))
                  case _ => Left(new IllegalArgumentException("Key Exchange Failed"))
                }
              }
              .pipe(ZIO.absolve)

          override def complete(sender: Hydrogen.HydroKxKeyPair)(state: Hydrogen.HydroKxState)(packet: Array[Byte]): Task[Hydrogen.HydroKxSessionKeyPair] =
            Task
              .effectTotal {
                val session = new Hydrogen.HydroKxSessionKeyPair()
                ctx.get.hydro_kx_kk_3(state, session, packet, sender) match {
                  case 0 => Right(session)
                  case _ => Left(new IllegalArgumentException("Key Exchange Failed"))
                }
              }
              .pipe(ZIO.absolve)
        }
      }
  }

  def request(sender: Hydrogen.HydroKxKeyPair)(receiver: Array[Byte]): URIO[KKVariant, (Hydrogen.HydroKxState, Array[Byte])] =
    ZIO.accessM(_.get.request(sender)(receiver))

  def response(sender: Hydrogen.HydroKxKeyPair)(requester: Array[Byte])(packet: Array[Byte]): ZIO[KKVariant, Throwable, (Hydrogen.HydroKxSessionKeyPair, Array[Byte])] =
    ZIO.accessM(_.get.response(sender)(requester)(packet))

  def complete(sender: Hydrogen.HydroKxKeyPair)(state: Hydrogen.HydroKxState)(packet: Array[Byte]): ZIO[KKVariant, Throwable, Hydrogen.HydroKxSessionKeyPair] =
    ZIO.accessM(_.get.complete(sender)(state)(packet))
}
