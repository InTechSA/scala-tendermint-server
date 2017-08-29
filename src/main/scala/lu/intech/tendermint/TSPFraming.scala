package lu.intech.tendermint

import java.nio.ByteBuffer

import akka.stream.scaladsl.Framing.FramingException
import akka.stream._
import akka.stream.stage._
import akka.util.ByteString

private final class TSPFraming extends GraphStage[FlowShape[ByteString, ByteString]] {

  private val in = Inlet[ByteString]("TSPFraming.in")
  private val out = Outlet[ByteString]("TSPFraming.out")

  override def shape: FlowShape[ByteString, ByteString] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes) =
    new GraphStageLogic(shape) with InHandler with OutHandler {

      private var buffer = ByteString.empty

      private def pushFrame(frame:TSPFrame): Unit = {
        val emit = frame.payload getOrElse {
          failStage(new FramingException(s"Cannot get frame payload"))
          ByteString.empty
        }

        frame.fullFrameSize match {
          case Some(size) => buffer = buffer.drop(size)
          case _ => failStage(new FramingException(s"Cannot get frame size"))
        }

        push(out, emit)
        if (buffer.isEmpty && isClosed(in)) {
          completeStage()
        }
      }

      private def tryPushFrame(): Unit = {
        val frame = TSPFrame(buffer)
        if(frame.isFull)
          pushFrame(frame)
        else
          tryPull()
      }

      private def tryPull(): Unit = {
        if (isClosed(in)) {
          failStage(new FramingException("Stream finished but there was a truncated final frame in the buffer"))
        } else pull(in)
      }

      override def onPush(): Unit = {
        buffer ++= grab(in)
        tryPushFrame()
      }

      override def onPull(): Unit = {
        tryPushFrame()
      }

      override def onUpstreamFinish(): Unit = {
        if (buffer.isEmpty) {
          completeStage()
        }
        else if (isAvailable(out)) {
          tryPushFrame()
        }
      }

      setHandlers(in, out, this)
    }

  private final case class TSPFrame(private val buffer:ByteString) {

    private val sizeFieldLen:Option[Int] = buffer.headOption.map(TSPFrame.decodeInt)

    private val frameSize:Option[Int] = sizeFieldLen match {
      case Some(size) if buffer.size >= 1 + size => {
        val frameSize = TSPFrame.decodeBigInteger(buffer.slice(1, 1 + size), size)
        Some(frameSize.toInt)
      }
      case _ => None
    }

    val fullFrameSize:Option[Int] = for {
      sizeField <- sizeFieldLen
      frame <- frameSize
    } yield 1 + sizeField + frame

    val isFull: Boolean = fullFrameSize exists (_ <= buffer.size)
    val payload: Option[ByteString] =
      if(isFull)
        for {
          sizeField <- sizeFieldLen
          frame <- frameSize
        } yield buffer.slice(1 + sizeField, 1 + sizeField + frame)
      else
        None

  }

  private object TSPFrame {

    def decodeInt(b:Byte): Int = b & 0xff

    def decodeBigInteger(buffer: ByteString, nbBytes: Int): BigInt =
      (0 until nbBytes).foldLeft(BigInt(0)){(acc:BigInt, i:Int) =>
        acc + (TSPFrame.decodeInt(buffer(i)) << (8 * i))
      }

  }

}

