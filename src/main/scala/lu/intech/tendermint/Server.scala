package lu.intech.tendermint

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString
import com.google.protobuf.CodedInputStream
import com.trueaccord.scalapb.GeneratedMessage
import org.slf4j.LoggerFactory
import types.types._

import scala.concurrent._

class Server(
  private val host: String = "127.0.0.1",
  private val port: Int = 46658,
  private val parallelism: Int = 4,
  private val consensusHandler: ConsensusHandler = ConsensusHandler.empty,
  private val mempoolHandler: MempoolHandler = MempoolHandler.empty,
  private val queryHandler: QueryHandler = QueryHandler.empty
  )(implicit materializer: Materializer, system: ActorSystem, ec:ExecutionContext) {

  private val loggerMsg = LoggerFactory.getLogger("tsp.messages")
  private val loggerSrv = LoggerFactory.getLogger("tsp.server")

  private val parseProtobufRequests = Flow[ByteString]
    .map(data => Request.defaultInstance.mergeFrom(CodedInputStream.newInstance(data.toByteBuffer)))

  private val processing = Flow[ByteString]
    .via(new TSPFraming())
    .via(parseProtobufRequests)
    .mapAsync(parallelism)(req => {
      val result = processRequest(req)
      result onComplete { res =>
        loggerMsg.debug(s"REQUEST: $req")
        res match {
          case scala.util.Success(response) => loggerMsg.debug(s"RESPONSE: $response")
          case scala.util.Failure(ex) => loggerMsg.error(s"RESPONSE:ERROR ${ex.getMessage}", ex)
        }
      }
      result
    })
    .map(response => {
      val message = ByteString(response.toByteArray)
      val length = ByteString(BigInt(message.size).toByteArray)
      val lengthOfLength = ByteString(BigInt(length.size).toByteArray)
      lengthOfLength ++ length ++ message
    })

  private def processRequest(req: Request)(implicit ec: ExecutionContext): Future[GeneratedMessage] =
    req.value match {
      // Empty requests
      case Request.Value.Empty =>
        loggerSrv.warn(s"Received empty request $req")
        Future.successful(Response())

      // Basic requests / responses
      case Request.Value.Flush(value) => Future.successful(Response().withFlush(ResponseFlush()))
      case Request.Value.Echo(value) => Future.successful(Response().withEcho(ResponseEcho(value.message)))

      // Mempool requests / responses
      case Request.Value.CheckTx(value) => mempoolHandler.checkTx(value) map Response().withCheckTx

      // Consensus requests / responses
      case Request.Value.InitChain(value) => consensusHandler.initChain(value) map Response().withInitChain
      case Request.Value.BeginBlock(value) => consensusHandler.beginBlock(value) map Response().withBeginBlock
      case Request.Value.EndBlock(value) => consensusHandler.endBlock(value) map Response().withEndBlock
      case Request.Value.DeliverTx(value) => consensusHandler.deliverTx(value) map Response().withDeliverTx
      case Request.Value.Commit(value) => consensusHandler.commit(value) map Response().withCommit

      // Query requests / responses
      case Request.Value.Info(value) => queryHandler.info(value) map Response().withInfo
      case Request.Value.Query(value) => queryHandler.query(value) map Response().withQuery

      case _ =>
        loggerSrv.warn(s"Received handled request ${req.value.getClass.getName}")
        Future.successful(Response().withException(ResponseException(error = "This type of request is not yet handled")))
    }

  private val binding: Source[IncomingConnection, Future[ServerBinding]] = Tcp().bind(host, port)

  def start(): Unit = {
    binding runForeach { connection =>
      loggerSrv.info(s"New incoming connection: ${connection.remoteAddress}")
      connection.handleWith(processing)
    }
  }
}