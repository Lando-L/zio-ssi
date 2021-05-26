package zio.ssi.verifiable

import java.net.URI

opaque type Context = URI

object Context:
  val BASE_CONTEXT: Context = URI.create("https://www.w3.org/2018/credentials/v1")
  def apply(value: URI): Context = value
