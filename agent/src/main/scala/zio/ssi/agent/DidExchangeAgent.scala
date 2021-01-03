package zio.ssi.agent

//import zio.{Task, UIO}
//import zio.actors.Actor.Stateful
//import zio.actors.{ActorRef, Context}
//import zio.ssi.agent.exchange.did.DidExchange
//import zio.ssi.did.DidDoc
//
//import java.util.UUID

object DidExchangeAgent {
//  object Requester {
//    sealed trait State
//    object State {
//      final case object Start extends State
//      final case object Waiting extends State
//      final case object Abandoned extends State
//      final case object Completed extends State
//    }
//
//    sealed trait Message[T, +_]
//    object Message {
//      final case class Initiate[T](
//        key: Array[Byte],
//        replyTo: ActorRef[Responder.Message[T, *]]
//      ) extends Message[T, Unit]
//      final case class Response[T](value: DidExchange.Response[T]) extends Message[T, Unit]
//      final case class Abort(error: Throwable) extends Message[Nothing, Unit]
//    }
//
//    def apply[T](parentThreadId: UUID)(doc: DidDoc[T]): Stateful[Any, State, Message[T, *]] =
//      new Stateful[Any, State, Message[T, *]] {
//        override def receive[A](state: State, msg: Message[T, A], context: Context): Task[(State, A)] = {
//          (state, msg) match {
//            case (State.Start, Message.Initiate(key, replyTo)) =>
//              context.self[Message[T, *]].flatMap { self =>
//                (replyTo ! Responder.Message.Request(DidExchange.request[T](parentThreadId)(doc), self)).map { _ =>
//                  (State.Waiting, ())
//                }
//              }
//
//            case (State.Waiting, Message.Response(value)) =>
//
//
//
//            case (State.Waiting, Message.ProblemReport(error)) =>
//              console.putStrLn(error.toString).flatMap { _ =>
//                URIO[(Command[Event], State => B)]((Command.persist(Event.ProblemReportReceived), _ => ()))
//              }
//
//            case (_, _: Message.Invitation) => unsupportedMessage
//            case (_, _: Message.Response) => unsupportedMessage
//            case (_, _: Message.ProblemReport) => unsupportedMessage
//          }
//        }
//      }
//  }
//
//  object Responder {
//    sealed trait State
//    object State {
//      final case object Start extends State
//      final case object Waiting extends State
//      final case object Abandoned extends State
//      final case object Completed extends State
//    }
//
//    sealed trait Message[T, +_]
//    object Message {
//      final case class Request[T](
//        value: DidExchange.Request[T],
//        replyTo: ActorRef[Requester.Message[T, *]]
//      ) extends Message[T, Unit]
//      final case class Abort(error: Throwable) extends Message[Nothing, Unit]
//      final case object Complete extends Message[Nothing, Unit]
//    }
//  }
}
