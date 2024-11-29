package rtp;

import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

public class FecHandler extends FecHandlerDemo {
    public FecHandler(int size) {
        super(size);
    }

    public FecHandler(boolean useFec) {
        super(useFec);
    }

    @Override
    boolean checkCorrection(int nr, HashMap<Integer, RtpPacket> mediaPackets) {
        List<Integer> relatedMediaPackets = this.fecList.get(nr);
        if( relatedMediaPackets == null) {
            logger.log(Level.WARNING,"Kein Mediapaket mit Nummer " + nr + " gefunden.");
            return false;
        }

        for(Integer relatedMediaPacketNr: relatedMediaPackets) {
            if(relatedMediaPacketNr != nr && !mediaPackets.containsKey(relatedMediaPacketNr)) {
                logger.log(Level.WARNING, "Fehlendes Mediapaket: " + relatedMediaPacketNr + "für die RTP-Nummer: " + nr);
                return false;
            }
        }

        logger.log(Level.INFO, "Mediapaket mit Nummer " + nr + " wurde gefunden");
        return true;
    }

    @Override
    RtpPacket correctRtp(int rtpNr, HashMap<Integer, RtpPacket> mediaPackets) {
        //1. FEC Paket aus FecStack holen
        FecPacket fecPacket = this.fecStack.get(this.fecNr.get(rtpNr));

        if (fecPacket == null) {
            logger.log(Level.SEVERE, "Kein FEC-Paket mit RTP-Nummer " + rtpNr + " gefunden!");
            return null;
        }

        //2. wenn richtiger Stack gefunden, betroffene RTP Pakete aus Liste rausholen
        List<Integer> relatedMediaPackets = this.fecList.get(rtpNr);

        //3. alle RTP Pakete der Liste mit dem Fec Paket XORn mittels add
        for(Integer relatedMediaPacketNr: relatedMediaPackets) {
            if (!relatedMediaPacketNr.equals(rtpNr) && mediaPackets.containsKey(relatedMediaPacketNr)) {
                fecPacket.addRtp(mediaPackets.get(relatedMediaPacketNr));
            }
        }

        //4. getLostRtp(rtpNr)
        return fecPacket.getLostRtp(rtpNr);
    }
}
