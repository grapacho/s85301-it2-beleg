import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import rtp.RtpPacket;

class RtpPacketTest {

  private static RtpPacket rtpPacket;

  @BeforeAll
  public static void initRTP() {
    rtpPacket = new RtpPacket(26, 42, 0, 1, new byte[0], 0);
  }

  @Test
  public void testSetRtpHeader() {
    assertEquals(42, rtpPacket.getsequencenumber());
  }

  @AfterAll
  static void afterAll() {

  }
}