package akka.io.spdy

import java.nio.ByteOrder
import akka.io._
import akka.util.{ByteStringBuilder, ByteString}
import akka.io.spdy.SpdyManager.{SynStream, ControlFrame, Frame}
import scala.collection.mutable

object SpdyPipelines {

  def getSimplePipelinePorts() = {
    val ctx = new SpdyByteOrder {}

    val stages =
      new SpdyPipelines.ControlFrameStage

    PipelineFactory.buildFunctionTriple(ctx, stages)
  }

  trait SpdyByteOrder extends PipelineContext {
    /* AKA Network Byte Order */
    implicit val byteOrder = ByteOrder.BIG_ENDIAN
  }

  class ControlFrameStage extends SymmetricPipelineStage[SpdyByteOrder, ControlFrame, ByteString] {
    def apply(ctx: SpdyByteOrder) = new SymmetricPipePair[ControlFrame, ByteString] {
      implicit val byteOrder = ctx.byteOrder

      override val commandPipeline = { msg: ControlFrame =>

        def putString(builder: ByteStringBuilder, str: String): Unit = {
          val bs = ByteString(str, "UTF-8")
          builder putInt bs.length
          builder ++= bs
        }

        val bs = ByteString.newBuilder
//        val C_Version_Type = new mutable.BitSet
//        C_Version_Type += msg.version
//        C_Version_Type += msg.commandType
//        bs.putInt(C_Version_Type.firstKey)

        bs.putByte(1)

        bs.putShort(msg.version)

        bs.putShort(msg.commandType)

        bs.putByte(msg.flags)

        bs.putInt(msg.length)

        ctx.singleCommand(bs.result)

      }

      override val eventPipeline = { bs: ByteString =>
        val iter = bs.iterator

        val frameType = iter.getByte
        require(frameType == 1, s"expected ControlFrame marker == 1, but was $frameType")

        val version = iter.getShort

        val commandType = iter.getShort

        val flags = iter.getByte

        val length = iter.getInt

        ctx.singleEvent(new SynStream(version, flags, length, 0, 0, Nil))
      }
    }
  }

}
