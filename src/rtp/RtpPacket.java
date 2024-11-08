package rtp;

public class RtpPacket extends RtpPacketDemo{
    @Override
    void setRtpHeader() {
        // Erste Byte: Version, Padding, Extension und CC
        header[0] = (byte) ((Version << 6) & 0xC0); // V=2 (RTP-Version)
        header[0] |= (Padding << 5) & 0x20;         // Padding-Flag
        header[0] |= (Extension << 4) & 0x10;       // Extension-Flag
        header[0] |= CC & 0x0F;                     // CSRC Count (normalerweise 0)

        // Zweite Byte: Marker und Payload Type
        header[1] = (byte) ((Marker << 7) & 0x80);  // Marker-Bit
        header[1] |= PayloadType & 0x7F;            // Payload-Typ (z.B., MJPEG)

        // Drittes und viertes Byte: Sequence Number
        header[2] = (byte) ((SequenceNumber >> 8) & 0xFF);
        header[3] = (byte) (SequenceNumber & 0xFF);

        // Fünftes bis achtes Byte: Timestamp
        header[4] = (byte) ((TimeStamp >> 24) & 0xFF);
        header[5] = (byte) ((TimeStamp >> 16) & 0xFF);
        header[6] = (byte) ((TimeStamp >> 8) & 0xFF);
        header[7] = (byte) (TimeStamp & 0xFF);

        // Neuntes bis zwölftes Byte: SSRC
        //header[8] = (byte) ((Ssrc >> 24) & 0xFF);
        //header[9] = (byte) ((Ssrc >> 16) & 0xFF);
        //header[10] = (byte) ((Ssrc >> 8) & 0xFF);
        //header[11] = (byte) (Ssrc & 0xFF);
    }


    public RtpPacket(int PType, int Framenb, int Time, int Mar, byte[] data, int data_length) {
        super(PType, Framenb, Time, Mar, data, data_length);
    }

    public RtpPacket(byte[] packet, int packet_size) {
        super(packet, packet_size);
    }
}
