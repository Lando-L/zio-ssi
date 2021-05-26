package zio.ssi.verifiable

class MapRecord(private val fields: Map[String, Any]) extends Selectable:
  def selectDynamic(name: String): Any = fields(name)

object MapRecord:
  def apply(elems: (String, Any)*): MapRecord =
    new MapRecord(elems.toMap)
