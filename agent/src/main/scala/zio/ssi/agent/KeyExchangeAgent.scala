package zio.ssi.agent

import co.libly.hydride.Hydrogen
import zio.actors.Actor.Stateful
import zio.actors.{ActorRef, Context}
import zio.ssi.hydrogen.exchange.nvariant
import zio.{RIO, UIO}

object KeyExchangeAgent {
  object Requester {
    sealed trait State
    object State {
      final case object Start extends State
      final case class Waiting(session: Hydrogen.HydroKxSessionKeyPair) extends State
      final case class Aborted(error: Throwable) extends State
      final case class Completed(session: Hydrogen.HydroKxSessionKeyPair) extends State
    }

    sealed trait Message[+_]
    object Message {
      final case class Initiate(replyTo: ActorRef[Responder.Message]) extends Message[Unit]
      final case class Abort(error: Throwable) extends Message[Unit]
      final case object Complete extends Message[Unit]
    }

    def apply(sender: Array[Byte]): Stateful[nvariant.NVariant, State, Message] =
      new Stateful[nvariant.NVariant, State, Message] {
        override def receive[A](state: State, msg: Message[A], context: Context): RIO[nvariant.NVariant, (State, A)] =
          (state, msg) match {
            case (State.Start, Message.Initiate(replyTo)) =>
              context.self[Message].flatMap { ref =>
                nvariant.request(sender).flatMap { case (state, packet) =>
                  (replyTo ! Responder.Message.Initiate(packet, ref)).map { _ =>
                    (State.Waiting(state), ())
                  }
                }
              }

            case (State.Waiting(session), Message.Complete) =>
              UIO((State.Completed(session), ()))

            case (State.Waiting(_), Message.Abort(error)) =>
              UIO((State.Aborted(error), ()))

            case (_, _: Message[Unit]) =>
              UIO((state, ()))
          }
      }
  }

  object Responder {
    sealed trait State
    object State {
      final case object Start extends State
      final case class Aborted(error: Throwable) extends State
      final case class Completed(session: Hydrogen.HydroKxSessionKeyPair) extends State
    }

    sealed trait Message[+_]
    object Message {
      final case class Initiate(packet: Array[Byte], replyTo: ActorRef[Requester.Message]) extends Message[Unit]
    }

    def apply(keyPair: Hydrogen.HydroKxKeyPair)(requester: Array[Byte]): Stateful[nvariant.NVariant, State, Message] =
      new Stateful[nvariant.NVariant, State, Message] {
        override def receive[A](state: State, msg: Message[A], context: Context): RIO[nvariant.NVariant, (State, A)] = {
          (state, msg) match {
            case (State.Start, Message.Initiate(packet, replyTo)) =>
              nvariant.response(keyPair)(packet).either.flatMap {
                case Left(error) =>
                  (replyTo ! Requester.Message.Abort(error)).map { _ =>
                    (State.Aborted(error), ())
                  }

                case Right(session) =>
                  (replyTo ! Requester.Message.Complete).map { _ =>
                    (State.Completed(session), ())
                  }
              }

            case (_, _: Message[Unit]) =>
              UIO((state, ()))
          }
        }
      }
  }
}
