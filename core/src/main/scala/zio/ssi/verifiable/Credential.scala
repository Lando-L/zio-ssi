package zio.ssi.verifiable

case class Credential[A](
  context: List[Context],
  id: Id,
  types: List[Type],
  subject: Subject[A],
  issuer: Issuer,
  issued: Timestamp,
  expired: Option[Timestamp],
  proof: Proof
)
