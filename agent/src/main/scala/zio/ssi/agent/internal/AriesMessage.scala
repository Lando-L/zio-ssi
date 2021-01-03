package zio.ssi.agent.internal

import java.net.URI
import java.util.UUID

abstract class AriesMessage(
  val `type`: URI,
  val id: UUID,
  val optThread: Option[AriesMessage.Thread]
)

object AriesMessage {
  final case class Thread(parentThreadId: UUID, threadId: UUID)
}
