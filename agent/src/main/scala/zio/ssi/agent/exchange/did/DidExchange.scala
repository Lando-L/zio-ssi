package zio.ssi.agent.exchange.did

import zio.ssi.agent.internal.AriesMessage
import zio.ssi.did.DidDoc

import java.net.URI
import java.util.UUID
import scala.util.chaining._

sealed abstract class DidExchange[T](
  override val `type`: URI,
  override val id: UUID,
  thread: AriesMessage.Thread,
) extends AriesMessage(`type`, id, Some(thread))

object DidExchange {
  def request[T](parentThreadId: UUID)(didDoc: DidDoc[T]): Request[T] =
    UUID
      .randomUUID()
      .pipe { id =>
        Request(
          new URI("https://didcomm.org/didexchange/1.0/request"),
          id,
          AriesMessage.Thread(
            parentThreadId,
            id
          ),
          didDoc
        )
      }

  def response[T](parentThreadId: UUID, threadId: UUID)(didDoc: DidDoc[T]): DidExchange[T] =
    UUID
      .randomUUID()
      .pipe { id =>
        Response(
          new URI("https://didcomm.org/didexchange/1.0/response"),
          id,
          AriesMessage.Thread(
            parentThreadId,
            threadId
          ),
          didDoc
        )
      }

  def complete(parentThreadId: UUID, threadId: UUID): Complete =
    Complete(
      new URI("https://didcomm.org/didexchange/1.0/complete"),
      UUID.randomUUID(),
      AriesMessage.Thread(
        parentThreadId,
        threadId
      )
    )

  final case class Request[T] private[did] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread,
    didDoc: DidDoc[T]
  ) extends DidExchange[T](`type`, id, thread)

  final case class Response[T] private[did] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread,
    didDoc: DidDoc[T]
  ) extends DidExchange[T](`type`, id, thread)

  final case class Complete private[did] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread
  ) extends DidExchange[Nothing](`type`, id, thread)

  final case class ProblemReport private[did] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread,
    error: Throwable
  ) extends DidExchange[Nothing](`type`, id, thread)
}
