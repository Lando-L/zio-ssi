package zio.ssi.verifiable

import java.net.URI

opaque type Type = URI

object Type:
  val BASE_CREDENTIAL_TYPE: Type = URI.create("VerifiableCredential")
  val BASE_PRESENTATION_TYPE: Type = URI.create("VerifiablePresentation")
  def apply(value: URI): Type = value
