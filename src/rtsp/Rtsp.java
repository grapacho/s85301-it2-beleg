package rtsp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;

public class Rtsp extends RtspDemo {
    public Rtsp(URI url, int rtpRcvPort) {
        super(url, rtpRcvPort);
    }

    public Rtsp(BufferedReader RTSPBufferedReader, BufferedWriter RTSPBufferedWriter) {
        super(RTSPBufferedReader, RTSPBufferedWriter);
    }

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

        // Prüfen, ob der aktuelle Zustand TEARDOWN erlaubt
        if (state != State.READY && state != State.PLAYING) {
            logger.log(Level.WARNING, "TEARDOWN not allowed in state: " + state);
            return false;
        }

        try {
            // RTSP-Sequenznummer erhöhen
            RTSPSeqNb++;

            // TEARDOWN-Anfrage senden
            logger.log(Level.INFO, "Sending RTSP TEARDOWN request...");
            send_RTSP_request("TEARDOWN");

            // Warten auf die Serverantwort
            logger.log(Level.INFO, "Waiting for server response...");
            int responseCode = parse_server_response();

            if (responseCode != 200) {
                logger.log(Level.WARNING, "TEARDOWN failed. Server responded with: " + responseCode);
                return false;
            }

            // Zustand aktualisieren
            state = State.INIT;
            logger.log(Level.INFO, "TEARDOWN successful. New RTSP state: INIT");
            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "An error occurred during TEARDOWN: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public void describe() {
        if (state != State.READY) {
            logger.log(Level.WARNING, "RTSP state: " + state);
            return;
        }
        RTSPSeqNb++;
        send_RTSP_request("DESCRIBE");
        logger.log(Level.INFO, "Wait for DESCRIBE response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response for DESCRIBE");
        }
    }

    @Override
    public void options() {
        RTSPSeqNb++;
        send_RTSP_request("OPTIONS");
        logger.log(Level.INFO, "Wait for OPTIONS response...");
        if (parse_server_response() != 200) {
            logger.log(Level.WARNING, "Invalid Server Response for OPTIONS");
        }
    }


    @Override
    public void send_RTSP_request(String request_type) {
        try {
            // Grundlegende RTSP-Request-Line
            RTSPBufferedWriter.write(request_type + " " + url + " RTSP/1.0" + CRLF);
            RTSPBufferedWriter.write("CSeq: " + RTSPSeqNb + CRLF);

            // Hinzufügen weiterer Header-Felder je nach Anfrage-Typ
            switch (request_type) {
                case "SETUP":
                    RTSPBufferedWriter.write("Transport: RTP/UDP; client_port=" + RTP_RCV_PORT + CRLF);
                    break;
                case "PLAY":
                case "PAUSE":
                case "TEARDOWN":
                    RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
                    break;
                case "OPTIONS":
                    // OPTIONS benötigt keine zusätzlichen Felder
                    break;
                case "DESCRIBE":
                    RTSPBufferedWriter.write("Accept: application/sdp" + CRLF);
                    break;
            }

            // Request abschließen und senden
            RTSPBufferedWriter.write(CRLF);
            RTSPBufferedWriter.flush();
            logger.log(Level.INFO, "Sent RTSP request: " + request_type);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error sending RTSP request: " + e.getMessage());
        }
    }
}
