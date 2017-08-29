package lu.intech.tendermint

import org.slf4j.LoggerFactory
import types.types.{CodeType, RequestCheckTx, ResponseCheckTx}

import scala.concurrent.Future

trait MempoolHandler {

  def checkTx(request: RequestCheckTx): Future[ResponseCheckTx]

}

object MempoolHandler {
  val empty: MempoolHandler = new MempoolHandler {

    private val logger = LoggerFactory.getLogger("handler.mempool")

    override def checkTx(request: RequestCheckTx): Future[ResponseCheckTx] = {
      logger.debug("Mempool:checkTx " + request)
      Future.successful(ResponseCheckTx(code = CodeType.OK))
    }
  }
}