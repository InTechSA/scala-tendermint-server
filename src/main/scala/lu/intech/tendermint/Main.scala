package lu.intech.tendermint

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

object Main {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: Materializer = ActorMaterializer()
    import scala.concurrent.ExecutionContext.Implicits.global
    new Server().start()
  }

}
