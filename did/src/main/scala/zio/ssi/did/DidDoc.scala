package zio.ssi.did

import java.net.URI
import java.util.UUID

final case class DidDoc[T](id: Did[T], publicKey: Map[UUID, DidDoc.PublicKey], service: Map[URI, DidDoc.Service])

object DidDoc {
  private[did] final case class PublicKey(
    keyType: String,
    controller: URI,
    keyValue: PublicKey.KeyValue
  )

  private[did] object PublicKey {
    sealed trait KeyValue
    object KeyValue {
      final case class PublicKeyBase58(underlying: String) extends KeyValue
      final case class PublicKeyHex(underlying: String) extends KeyValue
      final case class PublicKeyPem(underlying: String) extends KeyValue
    }
  }

  private[did] final case class Service(
    serviceType: String,
    serviceEndpoint: URI
  )
}
