package zio.ssi.hydrogen

import co.libly.hydride.Hydrogen
import zio.{Has, UIO, URIO, URLayer, ZIO, ZLayer}

package object exchange {
  type Exchange = Has[Exchange.Service]

  object Exchange {
    trait Service {
      def generateKeyPair: UIO[Hydrogen.HydroKxKeyPair]
    }

    val live: URLayer[Has[Hydrogen], Exchange] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def generateKeyPair: UIO[Hydrogen.HydroKxKeyPair] =
            UIO.effectTotal {
              val keyPair = new Hydrogen.HydroKxKeyPair()
              ctx.get.hydro_kx_keygen(keyPair)
              keyPair
            }
        }
      }
  }

  def generateKeyPair: URIO[Exchange, Hydrogen.HydroKxKeyPair] =
    ZIO.accessM(_.get.generateKeyPair)
}
