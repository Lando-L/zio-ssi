package zio.ssi.hydrogen.exchange

import co.libly.hydride.Hydrogen
import zio.{Has, RIO, Task, URLayer, ZIO, ZLayer}

import scala.util.chaining._

package object nvariant {
  type NVariant = Has[NVariant.Service]

  object NVariant {
    trait Service {
      def request(receiver: Array[Byte]): Task[(Hydrogen.HydroKxSessionKeyPair, Array[Byte])]
      def response(sender: Hydrogen.HydroKxKeyPair)(packet: Array[Byte]): Task[Hydrogen.HydroKxSessionKeyPair]
    }

    val live: URLayer[Has[Hydrogen], NVariant] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def request(receiver: Array[Byte]): Task[(Hydrogen.HydroKxSessionKeyPair, Array[Byte])] =
            Task
              .effectTotal {
                val session = new Hydrogen.HydroKxSessionKeyPair()
                val packet = Array.ofDim[Byte](Hydrogen.HYDRO_KX_N_PACKET1BYTES)
                ctx.get.hydro_kx_n_1(session, packet, null, receiver) match {
                  case 0 => Right((session, packet))
                  case _ => Left(new IllegalArgumentException("Request Failed"))
                }
              }.pipe(ZIO.absolve)

          override def response(sender: Hydrogen.HydroKxKeyPair)(packet: Array[Byte]): Task[Hydrogen.HydroKxSessionKeyPair] =
            Task
              .effectTotal {
                val session = new Hydrogen.HydroKxSessionKeyPair()
                ctx.get.hydro_kx_n_2(session, packet, null, sender) match {
                  case 0 => Right(session)
                  case _ => Left(new IllegalArgumentException("Key Exchange failed"))
                }
              }
              .pipe(ZIO.absolve)
        }
      }
  }

  def request(receiver: Array[Byte]): RIO[NVariant, (Hydrogen.HydroKxSessionKeyPair, Array[Byte])] =
    ZIO.accessM(_.get.request(receiver))

  def response(sender: Hydrogen.HydroKxKeyPair)(packet: Array[Byte]): RIO[NVariant, Hydrogen.HydroKxSessionKeyPair] =
    ZIO.accessM(_.get.response(sender)(packet))
}
