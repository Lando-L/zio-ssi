package zio.ssi.agent.exchange

import co.libly.hydride.Hydrogen
import zio.ssi.agent.internal.AriesMessage
import zio.ssi.hydrogen.exchange.nvariant
import zio.{Has, RIO, Task, UIO, URIO, URLayer, ZIO, ZLayer}

import java.net.URI
import java.util.UUID

package object key {
  type KeyExchangeOps = Has[KeyExchangeOps.Service]

  object KeyExchangeOps {
    trait Service {
      def request(key: Array[Byte])(parentThreadId: UUID): Task[(Hydrogen.HydroKxSessionKeyPair, KeyExchange.Request)]
      def response(keyPair: Hydrogen.HydroKxKeyPair)(request: KeyExchange.Request): UIO[Either[KeyExchange.ProblemReport, (Hydrogen.HydroKxSessionKeyPair, KeyExchange.Complete)]]
    }

    val live: URLayer[nvariant.NVariant, KeyExchangeOps] =
      ZLayer.fromFunction { ctx =>
        new Service {
          override def request(receiver: Array[Byte])(parentThreadId: UUID): Task[(Hydrogen.HydroKxSessionKeyPair, KeyExchange.Request)] =
            ctx.get.request(receiver).map { case (session, packet) =>
              val id =
                UUID.randomUUID()

              val request =
                KeyExchange.Request(
                  new URI("https://didcomm.org/keyexchange/1.0/request"),
                  id,
                  AriesMessage.Thread(parentThreadId, id),
                  packet
                )

              (session, request)
            }

          override def response(keyPair: Hydrogen.HydroKxKeyPair)(request: KeyExchange.Request): UIO[Either[KeyExchange.ProblemReport, (Hydrogen.HydroKxSessionKeyPair, KeyExchange.Complete)]] =
            ctx.get.response(keyPair)(request.packet).either.map {
              case Left(error) =>
                Left(
                  KeyExchange.ProblemReport(
                  new URI("https://didcomm.org/keyexchange/1.0/problem_report"),
                  UUID.randomUUID(),
                  request.thread,
                  error
                )
              )

              case Right(value) =>
                Right(
                  (
                    value,
                    KeyExchange.Complete(
                      new URI("https://didcomm.org/keyexchange/1.0/response"),
                      UUID.randomUUID(),
                      request.thread
                    )
                  )
                )
            }
        }
      }
  }

  def request(key: Array[Byte])(parentThreadId: UUID): RIO[KeyExchangeOps, (Hydrogen.HydroKxSessionKeyPair, KeyExchange.Request)] =
    ZIO.accessM(_.get.request(key)(parentThreadId))

  def response(keyPair: Hydrogen.HydroKxKeyPair)(request: KeyExchange.Request): URIO[KeyExchangeOps, Either[KeyExchange.ProblemReport, (Hydrogen.HydroKxSessionKeyPair, KeyExchange.Complete)]] =
    ZIO.accessM(_.get.response(keyPair)(request))
}
