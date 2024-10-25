package rtsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.URI;
import java.util.logging.Level;

public class Rtsp extends RtspDemo {
    @Override
    public boolean play() {
        if (state != State.READY) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }
        RTSPSeqNb++;  // increase RTSP sequence number for every RTSP request sent
        send_RTSP_request("PLAY");
        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.PLAYING;
            logger.log(Level.INFO, "New RTSP state: PLAYING\n");
            return true;
        }
    }

    @Override
    public boolean pause() {

        if (state != State.PLAYING) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }
        RTSPSeqNb++;  // increase RTSP sequence number for every RTSP request sent
        send_RTSP_request("PAUSE");
        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.READY;
            logger.log(Level.INFO, "New RTSP state: READY\n");
            return true;
        }

    }

    @Override
    public boolean teardown() {

        if (state != State.INIT && (state == State.READY || state == State.PLAYING)) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return false;
        }
        RTSPSeqNb++;  // increase RTSP sequence number for every RTSP request sent
        send_RTSP_request("SETUP");
        // Wait for the response
        logger.log(Level.INFO, "Wait for response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response");
            return false;
        } else {
            state = State.READY;
            logger.log(Level.INFO, "New RTSP state: READY\n");
            return true;
        }
    }

    @Override
    public void describe() {

    }

    @Override
    public void options() {

    }

    @Override
    public void send_RTSP_request(String request_type) {

    }

    public Rtsp(URI url, int rtpRcvPort) {
        super(url, rtpRcvPort);

    }

    public Rtsp(BufferedReader RTSPBufferedReader, BufferedWriter RTSPBufferedWriter) {
        super(RTSPBufferedReader, RTSPBufferedWriter);
    }
}
