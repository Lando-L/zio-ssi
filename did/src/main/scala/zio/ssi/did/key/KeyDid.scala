package zio.ssi.did.key

import zio.ssi.did.Did

object KeyDid {
  def apply(transform: Did.Transform, encKey: String): Did[Identifier] =
    new Did("key", Identifier(transform, encKey))

  private[key] final case class Identifier(transform: Did.Transform, encKey: String)
}
