package zio.ssi.agent.exchange

import zio.ssi.agent.internal.AriesMessage
import zio.{Has, UIO, ULayer, ZIO, ZLayer}
import zio.ssi.did.DidDoc

import java.net.URI
import java.util.UUID
import scala.util.chaining._

package object did {
  type DidExchangeOps = Has[DidExchangeOps.Service]

  object DidExchangeOps {
    trait Service {
      def request[T](didDoc: DidDoc[T])(parentThreadId: UUID): UIO[DidExchange.Request[T]]
      def response[T](didDoc: DidDoc[T])(request: DidExchange.Request[T]): UIO[Either[DidExchange.ProblemReport, DidExchange.Response[T]]]
      def complete[T](response: DidExchange.Response[T]): UIO[Either[DidExchange.ProblemReport, DidExchange.Complete]]
    }

    val live: ULayer[DidExchangeOps] =
      ZLayer.succeed {
        new Service {
          override def request[T](didDoc: DidDoc[T])(parentThreadId: UUID): UIO[DidExchange.Request[T]] =
            UUID.randomUUID()
              .pipe { id =>
                DidExchange.Request(
                  new URI("https://didcomm.org/didexchange/1.0/request"),
                  id,
                  AriesMessage.Thread(parentThreadId, id),
                  didDoc
                )
              }
              .pipe(ZIO.succeed(_))

          override def response[T](didDoc: DidDoc[T])(request: DidExchange.Request[T]): UIO[Either[DidExchange.ProblemReport, DidExchange.Response[T]]] =


          override def complete[T](response: DidExchange.Response[T]): UIO[Either[DidExchange.ProblemReport, DidExchange.Complete]] = ???
        }
      }
  }
}
