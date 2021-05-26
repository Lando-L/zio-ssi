package zio.ssi.verifiable

case class Presentation[A](
  context: List[Context],
  id: Option[Id],
  types: List[Type],
  credential: Credential[A],
  proof: Proof
)
