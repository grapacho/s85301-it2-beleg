import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import rtp.ReceptionStatistic;

public class JpegDisplay {

  static BufferedImage lastImage, b_img;
//static byte[] payload;

  public static ImageIcon nextPlaybackImage(byte[] payload, ReceptionStatistic rs) {
    Graphics2D g;
    BufferedImage bi, test;

    InputStream is = new ByteArrayInputStream(payload);
    try {
      bi = ImageIO.read(is);
      //test = ImageIO.read(new File("videos/htw-restart.jpeg"));
      //test = ImageIO.read(new File("videos/htw-restart-1-error.jpeg"));
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    if (lastImage == null) {
      lastImage = bi;
    }

    // create new empty image
    b_img = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_INT_ARGB);

    //Graphics g = lastImage.getGraphics();
    g = b_img.createGraphics();

    //g.fillRect ( 0, 0, b_img.getWidth(), b_img.getHeight() );

    //g.drawImage(lastImage, 0, 0, null);
    g.drawImage(bi, 0, 0, null);

    // set Text
    g.setPaint ( new Color( 150, 0, 0 ) );
    g.setFont(new Font("Arial", Font.PLAIN, 14));
    g.drawString(String.valueOf(rs.requestedFrames), 10, 30);

    // set transparency
    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
    //g.fillRect(0, 200, 320, 100);
    //reset composite
    //g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
    //draw

    //bi.setRGB(0,0, b_img.getWidth(), b_img.getHeight(),null,0,0);
    lastImage = bi;
    g.dispose();

    return new ImageIcon(b_img);
  }


}
