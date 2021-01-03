package zio.ssi.agent.internal

import zio.{RIO, ZIO}
import zio.ssi.hydrogen.encryption

import java.util.Base64

final case class EncryptionEnvelope(
  `protected`: EncryptionEnvelope.Protected,
  recipients: List[EncryptionEnvelope.Recipient],
  cipher: String
)

object EncryptionEnvelope {
  def pack(keys: List[Array[Byte]])(id: Long)(message: Array[Byte]): RIO[encryption.Encryption, EncryptionEnvelope] =
    for {
      cek <- ZIO.accessM[encryption.Encryption](_.get.generateKey)

      cipher <- encryption
        .encrypt(encryption.context)(cek)(id)(message)
        .map(Base64.getUrlEncoder.encodeToString)

      recipients <- ZIO
        .foreach(keys)(encryption.encrypt(encryption.context)(_)(CEK_ID)(cek))
        .map {
          _
            .map(Base64.getUrlEncoder.encodeToString)
            .map(Recipient(_, Header(DEFAULT_ALG, "exchange")))
        }
    } yield EncryptionEnvelope(Protected(DEFAULT_ENC), recipients, cipher)

  def unpack(key: Array[Byte])(id: Long)(jwe: EncryptionEnvelope): RIO[encryption.Encryption, Array[Byte]] = {
    for {
      cek <- ZIO
        .fromOption(jwe.recipients.headOption)
        .orElseFail(new IllegalArgumentException("No matching recipient found"))
        .map(_.encryptedKey)
        .map(Base64.getUrlDecoder.decode)
        .flatMap(encryption.decrypt(encryption.context)(key)(CEK_ID))

      message <- ZIO
        .succeed(jwe.cipher)
        .map(Base64.getUrlDecoder.decode)
        .flatMap(encryption.decrypt(encryption.context)(cek)(id))

    } yield message
  }

  val DEFAULT_ENC: String = "X25519"
  val DEFAULT_ALG: String = "default"

  private val CEK_ID: Long = 0L

  final case class Header(alg: String, kid: String)
  final case class Recipient(encryptedKey: String, header: Header)
  final case class Protected(enc: String)
}
