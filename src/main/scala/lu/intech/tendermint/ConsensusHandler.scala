package lu.intech.tendermint

import org.slf4j.LoggerFactory
import types.types._

import scala.concurrent.Future

trait ConsensusHandler {

  def initChain(request: RequestInitChain): Future[ResponseInitChain]
  def beginBlock(request: RequestBeginBlock): Future[ResponseBeginBlock]
  def endBlock(request: RequestEndBlock): Future[ResponseEndBlock]
  def deliverTx(request: RequestDeliverTx): Future[ResponseDeliverTx]
  def commit(request: RequestCommit): Future[ResponseCommit]

}

object ConsensusHandler {
  val empty: ConsensusHandler = new ConsensusHandler {

    private val logger = LoggerFactory.getLogger("handler.consensus")

    override def initChain(request: RequestInitChain): Future[ResponseInitChain] = {
      logger.debug("Consensus:initChain " + request)
      Future.successful(ResponseInitChain())
    }
    override def deliverTx(request: RequestDeliverTx): Future[ResponseDeliverTx] = {
      logger.debug("Consensus:deliverTx " + request)
      Future.successful(ResponseDeliverTx())
    }

    override def commit(request: RequestCommit): Future[ResponseCommit] = {
      logger.debug("Consensus:commit " + request)
      Future.successful(ResponseCommit())
    }

    override def beginBlock(request: RequestBeginBlock): Future[ResponseBeginBlock] = {
      logger.debug("Consensus:beginBlock " + request)
      Future.successful(ResponseBeginBlock())
    }

    override def endBlock(request: RequestEndBlock): Future[ResponseEndBlock] = {
      logger.debug("Consensus:endBlock " + request)
      Future.successful(ResponseEndBlock())
    }
  }
}