/**
 * Class for statistic values of the RTP packet reception.
 *
 * @author Emanuel Günther
 */
public class ReceptionStatistic {
    public int receivedPackets = 0;     // received media packets (jpegs)

    // FEC
    public int packetsLost = 0;         // number of lost media packets before correction
    public int correctedPackets = 0;    // successfully corrected media packets
    public int notCorrectedPackets = 0; // unsuccessfully corrected media packets

    // JPEGs
    public int requestedFrames = 0;     // number of requested frames from player (jpegs)
    public int framesLost = 0;          // number of lost frames (despite correction)
    public int framesPartLost = 0;      // number of fragmented frames


    // Index
    public int playbackIndex = -1;      // index (RTP-SNR) of the next image to be played
    public int latestSequenceNumber = -1; // sequence number of the last received media packet
    public int jitterBufferSize = 1;      // number of packets in the puffer
}

