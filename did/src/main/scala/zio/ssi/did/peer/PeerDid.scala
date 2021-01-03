package zio.ssi.did.peer

import zio.ssi.did.Did

object PeerDid {
  def apply(numAlgo: NumAlgo, transform: Did.Transform, encKey: String): Did[Identifier] =
    new Did("peer", Identifier(numAlgo, transform, encKey))

  private[peer] final case class Identifier(numAlgo: NumAlgo, transform: Did.Transform, encNumBasis: String)

  private[peer] sealed trait NumAlgo

  private[peer] object NumAlgo {
    final case object Empty extends NumAlgo
    final case object Genesis extends NumAlgo
  }
}
