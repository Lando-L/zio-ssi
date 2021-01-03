package zio.ssi.did.peer

import zio.ZIO
import zio.ssi.did
import zio.ssi.did.peer
import zio.test.Assertion._
import zio.test._

import java.net.URI

object DidSpec extends DefaultRunnableSpec {
  private def codec(value: URI): ZIO[PeerDidCodec, Throwable, URI] =
    for {
      encoded <- did.toDid[peer.Identifier](value)
      decoded <- did.fromDid[peer.Identifier](encoded)
    } yield decoded

  private val didSpec =
    suite("Did")(
      testM("transforms valid values into a Did and back"){
        val valid = List(
          new URI("did:peer:1zQmZMygzYqNwU6Uhmewx5Xepf2VLp5S4HLSwwgf2aiKZuwa")
        )

        ZIO
          .foreach(valid)(codec)
          .map(assert(_)(equalTo(valid)))
      }
    ).provideLayer(PeerDidCodec.live)

  override def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("Did")(
      didSpec
    )
}
