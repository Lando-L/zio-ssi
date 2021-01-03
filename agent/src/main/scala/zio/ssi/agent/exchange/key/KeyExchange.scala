package zio.ssi.agent.exchange.key

import zio.ssi.agent.internal.AriesMessage

import java.net.URI
import java.util.UUID

sealed abstract class KeyExchange(
  override val `type`: URI,
  override val id: UUID,
  thread: AriesMessage.Thread
) extends AriesMessage(`type`, id, Some(thread))

object KeyExchange {
  final case class Request private[key] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread,
    packet: Array[Byte]
  ) extends KeyExchange(`type`, id, thread)

  final case class Complete private[key] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread
  ) extends KeyExchange(`type`, id, thread)

  final case class ProblemReport private[key] (
    override val `type`: URI,
    override val id: UUID,
    thread: AriesMessage.Thread,
    error: Throwable,
  ) extends KeyExchange(`type`, id, thread)
}
