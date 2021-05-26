//package zio.ssi.verifiable
//
//import com.nimbusds.jose.{JOSEObjectType, JWSAlgorithm, JWSHeader, JWSSigner, JWSVerifier}
//import com.nimbusds.jwt.{JWTClaimsSet, SignedJWT}
//import zio.ssi.verifiable.Credential
//import zio.ssi.MapCodec
//import zio.{Has, RIO, Tag, Task, ULayer, ZIO, ZLayer}
//
//import java.net.URI
//import java.time.Instant
//import java.util.Date
//import scala.jdk.javaapi.CollectionConverters
//import scala.util.Try
//
//trait Verifiable:
//  def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(credential: Credential): Task[SignedJWT]
//  def sign(algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(presentation: Presentation): Task[SignedJWT]
//  def verify(verifier: JWSVerifier)(signedJWT: SignedJWT): Task[Boolean]
//
//object Verifiable:
//  def live: ULayer[Has[Verifiable]] =
//    ZLayer.succeed {
//      new Verifiable:
//        import Id.*
//        import Issuer.*
//        import Subject.*
//        import Timestamp.*
//
//        private def traverse[A, B](f: A => Option[B])(as: List[A]): Option[List[B]] =
//          as.map(f).foldLeft[Option[List[B]]](Some(Nil)) {
//            case (Some(bs), Some(b)) => Some(b :: bs)
//            case _ => None
//          }
//
//        private def toURI(value: String): Option[URI] =
//          Try(URI.create(value)).toOption
//
//        private def toContext(value: String): Option[Context] =
//          toURI(value).map(Context.apply)
//
//        private def toType(value: String): Option[Type] =
//          toURI(value).map(Type.apply)
//
//        private def toList(value: Any): Option[List[String]] =
//          Try(value.asInstanceOf[java.util.Collection[String]])
//            .map(CollectionConverters.asScala)
//            .map(_.toList)
//            .toOption
//
//        private def toMap(value: Any): Option[Map[String, String]] =
//          Try(value.asInstanceOf[java.util.Map[String, String]])
//            .map(CollectionConverters.asScala)
//            .map(_.toMap)
//            .toOption
//
//        private def getClaimSet(signedJWT: SignedJWT): Option[JWTClaimsSet] =
//          Try(signedJWT.getJWTClaimsSet).toOption
//
//        private def getContext(claim: Map[String, Any]): Option[List[Context]] =
//          toList(claim.get("@context")) flatMap traverse(toContext)
//
//        private def getId(claimSet: JWTClaimsSet): Option[Id] =
//          Option(claimSet.getJWTID) flatMap toURI map Id.apply
//
//        private def getType(claim: Map[String, Any]): Option[List[Type]] =
//          toList(claim.get("@context")) flatMap traverse(toType)
//
//        private def getSubject(claimSet: JWTClaimsSet)(claim: Map[String, Any]): Option[Subject] =
//          for
//            id <- Option(claimSet.getSubject) flatMap toURI map Id.apply
//            credentials <- claim.get("credentialSubject") flatMap toMap
//          yield
//            Subject(id)(credentials)
//
//        private def getIssuer(claimSet: JWTClaimsSet): Option[Issuer] =
//          Option(claimSet.getIssuer) flatMap toURI map Issuer.apply
//
//        private def getIssued(claimSet: JWTClaimsSet): Option[Timestamp] =
//          Option(claimSet.getIssueTime) map (_.toInstant) map Timestamp.apply
//
//        private def getExpired(claimSet: JWTClaimsSet): Option[Timestamp] =
//          Option(claimSet.getExpirationTime) map (_.toInstant) map Timestamp.apply
//
//        private def getClaim(field: String)(claimsSet: JWTClaimsSet): Option[Map[String, Any]] =
//          Try(claimsSet.getJSONObjectClaim(field))
//            .toOption
//            .flatMap(Option.apply)
//            .map(CollectionConverters.asScala)
//            .map(_.toMap)
//
//        override def sign[A](algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(credential: Credential[A]) =
//          val header =
//            JWSHeader
//              .Builder(algorithm)
//              .keyID(kid.toString)
//              .`type`(JOSEObjectType.JWT)
//
//          val payload =
//            JWTClaimsSet
//              .Builder()
//              .jwtID(credential.id.toString)
//              .issuer(credential.issuer.toString)
//              .issueTime(credential.issued.toDate)
//              .claim("nonce", nonce)
//
//          ZIO.effect {
//            val token = SignedJWT(header.build, payload.build)
//            token.sign(signer)
//            token
//          }
//
//        override def sign[A](algorithm: JWSAlgorithm)(kid: Id)(signer: JWSSigner)(nonce: String)(presentation: Presentation[A]) =
//          ZIO.fail(RuntimeException())
//
//        override def verify(verifier: JWSVerifier)(signedJWT: SignedJWT) =
//          ZIO.effect {
//            signedJWT.verify(verifier)
//          }
//
//        private def credential(signedJWT: SignedJWT) =
//          val credential =
//            for
//              claimSet  <- getClaimSet(signedJWT)
//              claim     <- getClaim("vc")(claimSet)
//              context   <- getContext(claim)
//              id        <- getId(claimSet)
//              types     <- getType(claim)
//              subject   <- getSubject(claimSet)(claim)
//              issuer    <- getIssuer(claimSet)
//              issued    <- getIssued(claimSet)
//              expired   = getExpired(claimSet)
//            yield
//              Credential(context, id, types, subject, issuer, issued, expired)
//
//          ZIO
//            .fromOption(credential)
//            .orElseFail(RuntimeException())
//    }
