package zio.ssi.did

final case class Did[T](method: String, methodSpecificIdentifier: T)

object Did {
  val URI_SCHEME: String = "did"

  private[did] sealed trait Transform

  private[did] object Transform {
    final case object Base58BTC extends Transform
  }
}
