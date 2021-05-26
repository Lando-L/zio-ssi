package zio.ssi.verifiable

import java.net.URI

opaque type Id = URI

object Id:
  def apply(value: URI): Id = value
