package lu.intech.tendermint

import org.slf4j.LoggerFactory
import types.types._

import scala.concurrent.Future

trait QueryHandler {

  def info(request: RequestInfo): Future[ResponseInfo]
  def query(request:RequestQuery): Future[ResponseQuery]

}


object QueryHandler {
  val empty: QueryHandler = new QueryHandler {

    private val logger = LoggerFactory.getLogger("handler.query")

    override def query(request: RequestQuery): Future[ResponseQuery] = {
      logger.debug("Query:query" + request)
      Future.successful(ResponseQuery())
    }

    override def info(request: RequestInfo): Future[ResponseInfo] = {
      logger.debug("Query:info" + request)
      Future.successful(ResponseInfo(data = "tendermint-server"))
    }
  }
}