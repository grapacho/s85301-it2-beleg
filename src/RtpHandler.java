import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for RTP packets.
 *
 * Processes all RTP packets and provides JPEG images for displaying
 *
 * @author Emanuel Günther
 */
public class RtpHandler {
    public enum EncryptionMode {
        NONE,
        SRTP,
        JPEG,
        JPEG_ATTACK
    }

    public static final int RTP_PAYLOAD_FEC = 127; // assumed as in RFC 5109, 10.1
    public static final int RTP_PAYLOAD_JPEG = 26;
    private static byte[] defaultKey = new byte[]{
        (byte)0xE1, (byte)0xF9, (byte)0x7A, (byte)0x0D, (byte)0x3E, (byte)0x01, (byte)0x8B, (byte)0xE0,
        (byte)0xD6, (byte)0x4F, (byte)0xA3, (byte)0x2C, (byte)0x06, (byte)0xDE, (byte)0x41, (byte)0x39};
    private static byte[] defaultSalt = new byte[]{
        (byte)0x0E, (byte)0xC6, (byte)0x75, (byte)0xAD, (byte)0x49, (byte)0x8A, (byte)0xFE,
        (byte)0xEB, (byte)0xB6, (byte)0x96, (byte)0x0B, (byte)0x3A, (byte)0xAB, (byte)0xE6};

    private EncryptionMode encryptionMode;
    private FecHandler fecHandler = null;
    private JpegEncryptionHandler jpegEncryptionHandler = null;
    private SrtpHandler srtpHandler = null;

    // server side
    private int currentSeqNb = 0; // sequence number of current frame
    private int currentTS = 0;    // timestamp of a set of frames
    private boolean fecEncodingEnabled = false; // server side
    Random random = new Random(123456); // Channel loss - fixed seed for debugging
    DatagramSocket RTPsocket; // socket to be used to send and receive UDP packets

    // client side
    private boolean fecDecodingEnabled = false; // client side
    private HashMap<Integer, RTPpacket> mediaPackets = null;
    private int playbackIndex = -1;
    private HashMap<Integer, List<Integer>> sameTimestamps = null;
    private ReceptionStatistic statistics = null;
    private Boolean isServer = false;
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /**
     * Create a new RtpHandler as server.
     *
     * @param fecGroupSize Group size for FEC packets. If the value is 0, FEC will be disabled.
     */
    public RtpHandler(int fecGroupSize) {
        isServer = true;
        if (fecGroupSize > 0) {
            fecEncodingEnabled = true;
            fecHandler = new FecHandler(fecGroupSize);
        }
        try {
            RTPsocket = new DatagramSocket();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception caught: " + e);
        }

    }

    /**
     * Create a new RtpHandler as client.
     *
     * @param useFec Use FEC correction or not
     */
    public RtpHandler(boolean useFec) {
        isServer = false;
        fecDecodingEnabled = useFec;
        fecHandler = new FecHandler(useFec);
        mediaPackets = new HashMap<>();
        sameTimestamps = new HashMap<>();
        statistics = new ReceptionStatistic();
    }

    public void reset() {
        currentSeqNb = 0;
        //fecHandler.reset();
        playbackIndex = -1;

        if (!isServer) {
            //mediaPackets.clear();
            //sameTimestamps.clear();
            statistics = new ReceptionStatistic();
        }
    }

    // ********************************** Server side ***************************************


    /**
     * Retrieve the current FEC packet, if it is available.
     *
     * @return FEC packet as byte array, null if no such packet available
     */
    public byte[] createFecPacket() {
        if (!isFecPacketAvailable()) {
            return null;
        }

        byte[] fecPacket = fecHandler.getPacket();
        byte[] encryptedPacket = null;

        switch (encryptionMode) {
        case SRTP:
            encryptedPacket = srtpHandler.transformToSrtp(new RTPpacket(fecPacket, fecPacket.length));
            if (encryptedPacket != null) {
                fecPacket = encryptedPacket;
            }
            break;
        case JPEG:
        case JPEG_ATTACK:
        default:
            break;
        }

        return fecPacket;
    }

    /**
     * Get statistic values of the reception of the packets.
     *
     * @return Object with statistic values
     */
    public ReceptionStatistic getReceptionStatistic() {
        // update values which are used internally and that are not just statistic
        statistics.playbackIndex = playbackIndex;

        return statistics;
    }

    /**
     * Check for the availability of an FEC packet.
     *
     * @return bolean value if FEC packet available
     */
    public boolean isFecPacketAvailable() {
        if (fecEncodingEnabled) {
            return fecHandler.isReady();
        } else {
            return false;
        }
    }

    /**
     * Transform a JPEG image to an RTP packet.
     *
     * Takes care of all steps inbetween.
     *
     * @param jpegImage JPEG image as byte array
     * @return RTP packet as byte array
     */
    public List<RTPpacket> jpegToRtpPackets(final byte[] jpegImage, int framerate) {
        byte[] image = null;

        switch (encryptionMode) {
            case JPEG:
                image = jpegEncryptionHandler.encrypt(jpegImage);
                break;
            case JPEG_ATTACK:
            case SRTP:
            default:
                image = jpegImage;
                break;
        }
        JpegFrame frame = JpegFrame.getFromJpegBytes(image); // convert JPEG to RTP payload

        List<RTPpacket> rtpPackets = new ArrayList<>();
        currentTS += (90000 / framerate); // TS is the same for all fragments
        int Mark = 0; // Marker bit is 0 for all fragments except the last one

        // iterieren über die JPEG-Fragmente und RTPs bauen
        ListIterator<byte[]> iter = frame.getRtpPayload().listIterator();
        while (iter.hasNext()) {
            byte[] frag = iter.next();
            if (!iter.hasNext()) {
                Mark = 1; // last segment -> set Marker bit
            }
            currentSeqNb++;

            // Build an RTPpacket object containing the image
            // time has to be in scale with 90000 Hz (RFC 2435, 3.)
            RTPpacket packet = new RTPpacket(
                RTP_PAYLOAD_JPEG, currentSeqNb, currentTS, Mark, frag, frag.length);

            rtpPackets.add(packet);
        }

        return rtpPackets;
    }

    public void sendJpeg(final byte[] jpegImage, int framerate, InetAddress clientIp, int clientPort, double lossRate) {
        List<RTPpacket> rtpPackets;    // fragmented RTP packets
        DatagramPacket sendDp;      // UDP packet containing the video frames

        rtpPackets = jpegToRtpPackets(jpegImage, framerate); // gets the fragmented RTP packets

        for (RTPpacket rtpPacket : rtpPackets) {    // Liste der RTP-Pakete

            byte[] packetData;
            if (encryptionMode == EncryptionMode.SRTP) {
                packetData = srtpHandler.transformToSrtp(rtpPacket);
            } else {
                packetData = rtpPacket.getpacket();
            }

            sendDp = new DatagramPacket(packetData, packetData.length, clientIp, clientPort);
            sendPacketWithError(sendDp,lossRate, false); // Send with packet loss

            if (fecEncodingEnabled) fecHandler.setRtp(rtpPacket);
            if (isFecPacketAvailable()) {
                logger.log(Level.FINE, "FEC-Encoder ready...");
                byte[] fecPacket = createFecPacket();
                // send to the FEC dest_port
                sendDp = new DatagramPacket(fecPacket, fecPacket.length, clientIp, clientPort);
                sendPacketWithError(sendDp, lossRate, true);
            }
        }
    }


    /**
     * @param senddp Datagram to send
     */
    private void sendPacketWithError(DatagramPacket senddp, double lossRate, boolean fec) {
        String label;
        if (fec) label = " fec ";
        else label = " media ";
        if (random.nextDouble() > lossRate) {
            logger.log(Level.FINE, "Send frame: " + label + " size: " + senddp.getLength());
          try {
            RTPsocket.send(senddp);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        } else {
            logger.log(Level.INFO, "Dropped frame: " + label);
            //if (!fec) dropCounter++;
        }
    }




    // ********************************** Client side ***************************************

    /**
     * Get statistic values of the reception of the packets.
     *
     * @return Object with statistic values
     */
    public ReceptionStatistic getReceptionStatistic() {
        // update values which are used internally and that are not just statistic
        statistics.playbackIndex = playbackIndex;

        return statistics;
    }

    /**
     * Get next image for playback.
     *
     * This method is the main interface for continuously getting images
     * for the purpose of displaying them.
     *
     * @return Image as byte array
     */
    public byte[] nextPlaybackImage() {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        statistics.requestedFrames++;
        playbackIndex++;

        ArrayList<RTPpacket> packetList = packetsForNextImage();
        if (packetList == null) {
            return null;
        }

        byte[] image = JpegFrame.combineToOneImage(packetList);
        logger.log(Level.FINER, "Display TS: "
                + (packetList.get(0).gettimestamp() & 0xFFFFFFFFL)
                + " size: " + image.length);

        byte[] decryptedImage = null;
        switch (encryptionMode) {
        case JPEG:
            decryptedImage = jpegEncryptionHandler.decrypt(image);
            if (decryptedImage != null) {
                image = decryptedImage;
            }
            break;
        case JPEG_ATTACK:
            decryptedImage = jpegEncryptionHandler.replaceAttackDecryption(image);
            if (decryptedImage != null) {
                image = decryptedImage;
            }
            break;
        case SRTP:
        default:
            break;
        }

        return image;
    }

    /**
     * Process and store a received RTP packet.
     *
     * @param packetData the received RTP packet as byte array
     */
    public void processRtpPacket(byte[] packetData, int packetLength) {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        RTPpacket packet = new RTPpacket(packetData, packetLength);
        int seqNr = packet.getsequencenumber();

        RTPpacket decryptedPacket = null;
        switch (encryptionMode) {
        case SRTP:
            decryptedPacket = srtpHandler.retrieveFromSrtp(packet.getpacket());
            if (decryptedPacket != null) {
                packet = decryptedPacket;
            }
            break;
        case JPEG:
        case JPEG_ATTACK:
        default:
            break;
        }

        // set the correct index for beginning the playback
        if (playbackIndex == -1) {
            playbackIndex = seqNr - 1;
        }

        int pt = packet.getpayloadtype();

        if (pt == RTP_PAYLOAD_JPEG) {
            statistics.receivedPackets++;
            statistics.latestSequenceNumber = seqNr;
            mediaPackets.put(seqNr, packet);

            int ts = packet.gettimestamp();
            List<Integer> tmpTimestamps = sameTimestamps.get(ts);
            if (tmpTimestamps == null) {
                tmpTimestamps = new ArrayList<>();
            }
            tmpTimestamps.add(seqNr);
            sameTimestamps.put(ts, tmpTimestamps);
            logger.log(Level.FINER, "FEC: set media nr: " + seqNr);
            logger.log(Level.FINER, "FEC: set sameTimestamps: " + (0xFFFFFFFFL & ts)
                    + " " + tmpTimestamps.toString());
        } else if (pt == RTP_PAYLOAD_FEC) {
            fecHandler.rcvFecPacket(packet);
        }
        // else: ignore packet

        logger.log(Level.FINER,
                "---------------- Receiver -----------------------"
                + "\r\n"
                + "Got RTP packet with SeqNum # "
                + packet.getsequencenumber()
                + " TimeStamp: "
                + (0xFFFFFFFFL & packet.gettimestamp()) // cast to long
                + " ms, of type "
                + pt
                + " Size: " + packet.getlength());

        // TASK remove comment for debugging
        // packet.printheader(); // print rtp header bitstream for debugging
    }

    /**
     * Set packet encryption.
     *
     * @param mode The encryption mode.
     * @return true if successful, false otherwise
     */
    public boolean setEncryption(EncryptionMode mode) {
        if (currentSeqNb > 0 || (statistics != null && statistics.latestSequenceNumber > 0)) {
            // Do not change encryption when already started.
            return false;
        }

        encryptionMode = mode;
        switch (encryptionMode) {
        case SRTP:
            /* Use pre-shared key and salt to avoid key management and
             * session initialization with a protocol.
             */
            try {
                srtpHandler = new SrtpHandler(
                        SrtpHandler.EncryptionAlgorithm.AES_CTR,
                        SrtpHandler.MacAlgorithm.NONE,
                        defaultKey, defaultSalt, 0);
            } catch (InvalidKeyException ikex) {
                System.out.println(ikex);
            } catch (InvalidAlgorithmParameterException iapex) {
                System.out.println(iapex);
            }
            if (srtpHandler == null) {
                return false;
            }
            break;
        case JPEG:
        case JPEG_ATTACK:
            /* Use pre-shared key and salt to avoid key management and
             * session initialization with a protocol.
             */
            jpegEncryptionHandler = new JpegEncryptionHandler(
                    defaultKey, defaultSalt);
            break;
        case NONE:
        default:
            break;
        }

        return true;
    }

    /**
     * Set if FEC error correction should be used.
     *
     * @param enabled Use the FEC error correction or not.
     */
    public void setFecDecryptionEnabled(boolean enabled) {
        fecDecodingEnabled = enabled;
    }

    /**
     * Set a new group size for the FEC error handling.
     *
     * @param newGroupSize new group size
     */
    public void setFecGroupSize(int newGroupSize) {
        fecHandler.setFecGroupSize(newGroupSize);
    }

    /**
     * Get the RTP packet with the given sequence number.
     *
     * This is the main method for getting RTP packets. It currently
     * includes error correction via FEC, but can be extended in future.
     *
     * @param number Sequence number of the RTP packet
     * @return RTP packet, null if not available and not correctable
     */
    private RTPpacket obtainMediaPacket(final int number) {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        int index = number % 0x10000; // account overflow of SNr (16 Bit)
        RTPpacket packet = mediaPackets.get(index);
        logger.log(Level.FINER, "FEC: get RTP nu: " + index);

        if (packet == null) {
            statistics.packetsLost++;
            logger.log(Level.WARNING, "FEC: Media lost: " + index);

            boolean fecCorrectable = fecHandler.checkCorrection(index, mediaPackets);
            if (fecDecodingEnabled && fecCorrectable) {
                packet = fecHandler.correctRtp(index, mediaPackets);
                statistics.correctedPackets++;
                logger.log(Level.INFO, "---> FEC: correctable: " + index);
            } else {
                statistics.notCorrectedPackets++;
                logger.log(Level.INFO, "---> FEC: not correctable: " + index);
                return null;
            }
        }

        return packet;
    }

    /**
     * Construct a list of RTP packets which contain the data of one image.
     *
     * @return List of RTP packets for one image
     */
    private ArrayList<RTPpacket> packetsForNextImage() {
        Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        ArrayList<RTPpacket> packetList = new ArrayList<>();
        RTPpacket packet = obtainMediaPacket(playbackIndex);
        if (packet == null) {
            statistics.framesLost++;
            return null;
        }

        packetList.add(packet);
        int timestamp = packet.gettimestamp();
        List<Integer> timestamps = sameTimestamps.get(timestamp);
        if (timestamps == null) {
            return packetList;
        }

        // TODO lost RTPs are not in the list but could perhaps be corrected -> check for snr
        for (int i = 1; i < timestamps.size(); i++) {
            packetList.add(obtainMediaPacket(timestamps.get(i)));
        }

        playbackIndex += timestamps.size() - 1;
        // TODO if list is fragmented return null or implement JPEG error concealment

        logger.log(Level.FINER, "-> Get list of " + packetList.size()
                + " RTPs with TS: " + (0xFFFFFFFFL & timestamp));
        return packetList;
    }
}

