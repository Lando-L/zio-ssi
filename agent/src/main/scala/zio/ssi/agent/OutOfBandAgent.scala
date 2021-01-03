package zio.ssi.agent

//import co.libly.hydride.Hydrogen
//import org.bitcoinj.core.Base58
//import zio.actors.Actor.Stateful
//import zio.actors.{ActorRef, Context}
//import zio.ssi.did.key.KeyDid
//import zio.ssi.hydrogen.exchange.nvariant
//import zio.{RIO, ZIO, random}

//object OutOfBandAgent {
//  object Sender {
//    sealed trait State
//    object State {
//      final case object Initial extends State
//      final case object Waiting extends State
//      final case class Done(session: Hydrogen.HydroKxSessionKeyPair) extends State
//      final case class Aborted(problem: String) extends State
//    }
//
//    sealed trait Message[+_]
//    object Message {
//      final case class SendInvite(ref: ActorRef[Receiver.Message]) extends Message[Unit]
//      final case class Response(packet: Array[Byte]) extends Message[Unit]
//      final case class Error(problem: String) extends Message[Unit]
//    }
//
//    def apply(sender: Hydrogen.HydroKxKeyPair)(did: KeyDid): Stateful[random.Random, State, Message] =
//      new Stateful[random.Random, State, Message] {
//        override def receive[A](state: State, msg: Message[A], context: Context): RIO[nvariant.NVariant with random.Random, (State, A)] =
//          (state, msg) match {
//            case (State.Initial, Message.SendInvite(ref)) =>
//              context.self[Message].flatMap { self =>
//                random.nextLong.flatMap { id =>
//                  Receiver.Message.Invite(OutOfBand(OutOfBand.Id(id), OutOfBand.Service(did) :: Nil), self)
//                    .pipe(ref.!)
//                    .map(_ => (State.Waiting, ()))
//                }
//              }
//
//            case (State.Waiting, Message.Response(packet)) =>
//              nvariant.response(sender)(packet).either.map {
//                case Left(error) =>
//                  (State.Aborted(error.getMessage), ())
//
//                case Right(session) =>
//                  (State.Done(session), ())
//              }
//          }
//      }
//  }
//
//  object Receiver {
//    sealed trait Message[+_]
//    object Message {
//      final case class Invite(outOfBand: OutOfBand, replyTo: ActorRef[Sender.Message]) extends Message[Unit]
//    }
//
//    def apply: Stateful[nvariant.NVariant, Unit, Message] =
//      new Stateful[nvariant.NVariant, Unit, Message] {
//        override def receive[A](state: Unit, msg: Message[A], context: Context): RIO[nvariant.NVariant, (Unit, A)] =
//          msg match {
//            case Message.Invite(outOfBand, replyTo) =>
//              ZIO
//                .fromOption(outOfBand.services.headOption)
//                .orElseFail(new IllegalArgumentException("Key exchange failed"))
//                .map(service => Base58.decode(service.key.encKey.underlying))
//                .flatMap(nvariant.request)
//                .either
//                .flatMap {
//                  case Left(error) =>
//                    (replyTo ! Sender.Message.Error(error.getMessage)).map { _ =>
//                      ((), ())
//                    }
//
//                  case Right((session, packet)) =>
//                    (replyTo ! Sender.Message.Response(packet)).map { _ =>
//                      ((), ())
//                    }
//                }
//          }
//      }
//  }
//}
