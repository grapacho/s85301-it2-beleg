import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FecPacketTest {
private static FecPacket fecPacket;

  @BeforeAll
  public static void initRTP() {
    fecPacket = new FecPacket(26, 42, 0, 48, 0);
  }
  @AfterAll
  static void afterAll() {
  }

  @Test
  void setFecHeader() {}

  @Test
  void setUlpLevelHeader() {}

  @Test
  void getRtpList() {}

  @Test
  void addRtp() {}




}