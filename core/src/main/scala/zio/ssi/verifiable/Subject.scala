package zio.ssi.verifiable

case class Subject[A](id: Option[Id], claim: A)

object Subject:
  val ID_KEY: String = "id"
  def apply[A](id: Id)(claim: A): Subject[A] = Subject(Some(id), claim)
  def apply[A](claim: A): Subject[A] = Subject(None, claim)
