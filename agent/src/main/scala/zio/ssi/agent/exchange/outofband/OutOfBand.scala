package zio.ssi.agent.exchange.outofband

import zio.ssi.agent.internal.AriesMessage
import zio.ssi.did.key.KeyDid

import java.net.{URI, URL}
import java.util.UUID

case class OutOfBand private[outofband] (
  override val `type`: URI,
  override val id: UUID,
  service: List[OutOfBand.Service]
) extends AriesMessage(`type`, id, None)

object OutOfBand {
  final case class Service private[outofband] (
    id: URI,
    recipientKeys: List[KeyDid],
    serviceEndpoint: URL
  )
}
