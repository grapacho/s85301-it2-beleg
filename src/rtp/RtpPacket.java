package rtp;

public class RtpPacket extends RtpPacketDemo{
    @Override
    void setRtpHeader() {

    }

    public RtpPacket(int PType, int Framenb, int Time, int Mar, byte[] data, int data_length) {
        super(PType, Framenb, Time, Mar, data, data_length);
    }

    public RtpPacket(byte[] packet, int packet_size) {
        super(packet, packet_size);
    }
}
