package zio.ssi.agent

import co.libly.hydride.Hydrogen
import zio.{clock, ZLayer}
import zio.actors.{ActorSystem, Supervisor}
import zio.test._
import zio.test.Assertion._

import java.io.File

//object KeyExchangeAgentSpec extends DefaultRunnableSpec {
//  private val config = Some(new File("./agent/src/test/resources/application.conf"))
//  private val hydrogen= new Hydrogen()
//  private val layer = ZLayer.succeed(hydrogen) ++ clock.Clock.live ++ (ZLayer.succeed(hydrogen) >>> KeyExchange.live)
//
//  override def spec =
//    suite("KeyExchangeSpec")(
//      suite("Basic KK exchange")(
//        testM("Creating session keys") {
//          val program = for {
//            system <- ActorSystem("system", config)
//
//            keyPairOne <- KeyExchange.generateKeyPair
//            keyPairTwo <- KeyExchange.generateKeyPair
//
//            agentOne <- system.make(
//              "req",
//              Supervisor.none,
//              KeyExchangeAgent.Requester.State.Start,
//              KeyExchangeAgent.Requester("req")(keyPairOne)(keyPairTwo.pk)
//            )
//
//            agentTwo <- system.make(
//              "res",
//              Supervisor.none,
//              KeyExchangeAgent.Responder.State.Start,
//              KeyExchangeAgent.Responder("res")(keyPairTwo)(keyPairOne.pk)
//            )
//
//            _ <- agentOne ! KeyExchangeAgent.Requester.Message.Initiate(agentTwo)
//          } yield assert(true)(equalTo(true))
//
//          program.provideCustomLayer(layer)
//        }
//      )
//    )
//}
