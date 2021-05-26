package zio.ssi.verifiable

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.{ECDSASigner, ECDSAVerifier}
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import zio.ssi.verifiable.{signature, verification}
import zio.ssi.MapCodec
import zio.test.Assertion.isTrue
import zio.test.environment.*
import zio.test.*
import zio.{Has, ULayer, URLayer}

import java.time.Instant
import java.net.URI

object VerifiableSpec extends DefaultRunnableSpec {
  def spec = suite("VerifierSpec")(
    testM("Credentials are verfiable") {
      val credential = Credential[Map[String, Any]](
        Context.BASE_CONTEXT :: Nil,
        Id(URI.create("http://example.edu/credentials/3732")),
        Type.BASE_CREDENTIAL_TYPE :: Nil,
        Subject(Id(URI.create("did:example:ebfeb1f712ebc6f1c276e12ec21")))(Map("educationalCredentialAwarded" -> "Bachelor of Science in Mechanical Engineering")),
        Issuer(URI.create("did:example:abfe13f712120431c276e12ecab")),
        Timestamp(Instant.parse("2019-03-09T13:25:51Z")),
        Some(Timestamp(Instant.parse("2019-03-09T14:04:07Z"))),
        Proof(Map.empty)
      )

      val ecJWK = new ECKeyGenerator(Curve.P_256).generate()
      val ecPublicJWK = ecJWK.toPublicJWK()

      val signer = ECDSASigner(ecJWK)
      val verifier = ECDSAVerifier(ecPublicJWK)

      val algorithm = JWSAlgorithm.ES256
      val kid = Id(URI.create("kid"))
      val nonce = "nonce"

      val effect =
        for
          jwt <- signature.sign(algorithm)(kid)(signer)(nonce)(credential)
          verified <- verification.verify(verifier)(jwt)
        yield
          assert(verified)(isTrue)

      effect.provideCustomLayer(
        (MapCodec.identity >+> signature.Signature.live[Map[String, Any]]) +!+ verification.Verification.live
      )
    }
  )
}
