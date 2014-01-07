package akka.io.spdy

import akka.actor.{ActorLogging, Actor}
import akka.util.ByteString.ByteString1
import scala.collection.BitSet
import akka.util.ByteString
import java.nio.ByteOrder
import akka.io.spdy.SpdyPipelines.SpdyByteOrder

private[spdy] class SpdyManager(httpSettings: SpdyExt#Settings) extends Actor with ActorLogging {

  import SpdyManager._

  def receive = ???
}

object SpdyManager extends SpdyByteOrder {

  /** Common trait for all Control and Data frames */
  sealed trait Frame {
    def isControlFrame: Boolean = this.isInstanceOf[ControlFrame]
    def isDataFrame: Boolean = this.isInstanceOf[DataFrame]

    /** 8 bits */
    def flags: Byte

    /** 24 bits (int is 32) */
    def length: Int

  }

  /**
   * +----------------------------------+
   * |1| Version(15bits) | Type(16bits) |
   * +----------------------------------+
   * | Flags (8)  |  Length (24 bits)   |
   * +----------------------------------+
   * |               Data               |
   * +----------------------------------+
   */
  trait ControlFrame extends Frame {
    /** 15 bits */
    def version: Short

    /** 16 bytes */
    def commandType: Short
  }

  type NameValueHeaderBlock = Seq[(String, String)]

  /**
   * The SYN_STREAM control frame allows the sender to asynchronously create a stream between the endpoints. See Stream Creation (section 2.3.2)
   *
   * +------------------------------------+
   * |1|    version    |         1        |
   * +------------------------------------+
   * |  Flags (8)  |  Length (24 bits)    |
   * +------------------------------------+
   * |X|           Stream-ID (31bits)     |
   * +------------------------------------+
   * |X| Associated-To-Stream-ID (31bits) |
   * +------------------------------------+
   * | Pri|Unused | Slot |                |
   * +-------------------+                |
   * | Number of Name/Value pairs (int32) |   <+
   * +------------------------------------+    |
   * |     Length of name (int32)         |    | This section is the "Name/Value
   * +------------------------------------+    | Header Block", and is compressed.
   * |           Name (string)            |    |
   * +------------------------------------+    |
   * |     Length of value  (int32)       |    |
   * +------------------------------------+    |
   * |          Value   (string)          |    |
   * +------------------------------------+    |
   * |           (repeats)                |   <+
   *
   * Flags: Flags related to this frame. Valid flags are:
   *
   * 0x01 = FLAG_FIN - marks this frame as the last frame to be transmitted on this stream and puts the sender in the half-closed (Section 2.3.6) state.
   * 0x02 = FLAG_UNIDIRECTIONAL - a stream created with this flag puts the recipient in the half-closed (Section 2.3.6) state.
   * Length: The length is the number of bytes which follow the length field in the frame. For SYN_STREAM frames, this is 10 bytes plus the length of the compressed Name/Value block.
   *
   * Stream-ID: The 31-bit identifier for this stream. This stream-id will be used in frames which are part of this stream.
   *
   * Associated-To-Stream-ID: The 31-bit identifier for a stream which this stream is associated to. If this stream is independent of all other streams, it should be 0.
   *
   * Priority: A 3-bit priority (Section 2.3.3) field.
   *
   * Unused: 5 bits of unused space, reserved for future use.
   *
   * Slot: 8 bits of unused space, reserved for future use. Name/Value Header Block: A set of name/value pairs carried as part of the SYN_STREAM. see Name/Value Header Block (Section 2.6.10).
   *
   * If an endpoint receives a SYN_STREAM which is larger than the implementation supports, it MAY send a RST_STREAM with error code FRAME_TOO_LARGE. All implementations MUST support the minimum size limits defined in the Control Frames section (Section 2.2.1).
   */
  case class SynStream(
    version: Short,
    flags: Byte, length: Int, streamId: Int,
    assocToStreamId: Int,
    nameValueHeaderBlock: NameValueHeaderBlock) extends ControlFrame {

    val commandType: Short = 1

    def validFlags = Set(0x01, 0x02)
  }

  /**
   * +----------------------------------+
   * |0|       Stream-ID (31bits)       |
   * +----------------------------------+
   * | Flags (8)  |  Length (24 bits)   |
   * +----------------------------------+
   * |               Data               |
   * +----------------------------------+
   */
  trait DataFrame extends Frame {
    def streamId: Int
  }


  case class KeyValue(key: String, value: String)

}