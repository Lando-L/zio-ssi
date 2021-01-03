package zio.ssi.agent.exchange

import zio.{UIO, ZIO}
import zio.ssi.agent.exchange.outofband.OutOfBand.Service
import zio.ssi.did.key.KeyDid

import java.net.{URI, URL}
import java.util.UUID
import scala.util.chaining._

package object outofband {
  def invite(recipients: List[KeyDid]): UIO[OutOfBand] =
    OutOfBand(
      new URI("https://didcomm.org/out-of-band/1.0/invitation"),
      UUID.randomUUID(),
      recipients.map { recipient =>
        Service(
          new URI("#inline"),
          recipient :: Nil,
          new URL("http://localhost:1337")
        )
      }
    ).pipe(ZIO.succeed(_))
}
