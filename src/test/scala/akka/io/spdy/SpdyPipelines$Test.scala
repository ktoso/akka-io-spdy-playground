package akka.io.spdy

import org.scalatest.{Matchers, FlatSpec, FunSuite}
import akka.io.spdy.SpdyPipelines.SpdyByteOrder
import akka.io.{PipelinePorts, PipelineFactory}
import akka.io.spdy.SpdyManager.{ControlFrame, SynStream}
import akka.util.ByteString

class SpdyPipelines$Test extends FlatSpec with Matchers {

  val ctx = new SpdyByteOrder {}

  val stages =
    new SpdyPipelines.ControlFrameStage

  it should "get back the same thing" in {
    // given
    val PipelinePorts(cmd, evt, mgmt) = PipelineFactory.buildFunctionTriple(ctx, stages)

    val msg = SynStream(3, 2, 12, 0, 0, Nil)

    // when
    val encoded: (Iterable[ControlFrame], Iterable[ByteString]) = cmd(msg)

    println("encoded._2.head = " + encoded._2.headOption)

    val decoded: (Iterable[ControlFrame], Iterable[ByteString]) = evt(encoded._2.head)

    println("decoded._1.headOption = " + decoded._1.headOption)

    // then
    decoded._1.head should equal (msg)
  }

}
