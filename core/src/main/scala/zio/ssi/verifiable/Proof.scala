package zio.ssi.verifiable

opaque type Proof = Map[String, Any]

object Proof:
  def apply(values: Map[String, Any]): Proof = values
  def apply(values: (String, Any)*): Proof = Map.from(values)
