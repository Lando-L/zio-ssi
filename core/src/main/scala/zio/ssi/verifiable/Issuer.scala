package zio.ssi.verifiable

import java.net.URI

opaque type Issuer = URI

object Issuer:
  def apply(value: URI): Issuer = value
