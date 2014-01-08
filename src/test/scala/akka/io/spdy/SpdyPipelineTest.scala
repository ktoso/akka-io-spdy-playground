package akka.io.spdy

import org.scalatest.{Matchers, FlatSpec, FunSuite}
import akka.io.spdy.SpdyPipelines.SpdyByteOrder
import akka.io.{PipelinePorts, PipelineFactory}
import akka.io.spdy.SpdyManager.{ControlFrame, SynStream}
import akka.util.ByteString
import com.google.common.io.Files
import java.io.File

class SpdyPipelineTest extends FlatSpec with Matchers {

  private val PipelinePorts(cmd, evt, mgmt) = SpdyPipelines.getSimplePipelinePorts()

  it should "get back the same thing" in {
    // given

    val msg = SynStream(3, 2, 12, 0, 0, Nil)

    // when
    val encoded: (Iterable[ControlFrame], Iterable[ByteString]) = cmd(msg)
    info("encoded = " + encoded._2.head)

    val out = new File("serialized.out")
    Files.write(encoded._2.head.toArray, out)
    info("Stored in: " + out.getAbsolutePath)

    val decoded: (Iterable[ControlFrame], Iterable[ByteString]) = evt(encoded._2.head)
    info("decoded = " + decoded._1.head)

    // then
    decoded._1.head should equal (msg)
  }

}
