import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class JpegDisplay extends JpegDisplayDemo{
    public JpegDisplay() {
    }

    @Override
    BufferedImage setTransparency(BufferedImage back, BufferedImage foreground, List<Integer> list) {
        // Aktuelles Bild zurückgeben wenn keine Segmente fehlen
        if (list.isEmpty()) {
            return foreground;
        }

        //  BufferedImage mit Alpha-Kanal erstellen
        BufferedImage combined = new BufferedImage(
                foreground.getWidth(),
                foreground.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        // Berechnen der Höhe eines Segments
        int sliceHeight = foreground.getHeight() / 22;

        Graphics2D g2d = combined.createGraphics();
        try {
            // Bild vollständig transparent machen
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, combined.getWidth(), combined.getHeight());

            // Auf den Standard-Composite zurücksetzen
            g2d.setComposite(AlphaComposite.SrcOver);

            // Hintergrundbild zeichnen
            g2d.drawImage(back, 0, 0, null);


            // Jedes Segment ohne Fehler aus dem Vordergrund zeichnen
            for (int i = 0; i < 22; i++) {
                if (!list.contains(i)) {
                    int y = i * sliceHeight;
                    g2d.drawImage(foreground,
                            0, y,                                    // destination x, y
                            foreground.getWidth(), y + sliceHeight, // destestination width, height
                            0, y,                                        // source x, y
                            foreground.getWidth(), y + sliceHeight, // source width, height
                            null);
                }
            }
        } finally {
            g2d.dispose();
        }

        return combined;
    }



}
