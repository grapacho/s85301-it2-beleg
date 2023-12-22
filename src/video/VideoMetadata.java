package video;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Structure holding metadata of video files
 *
 * @author Emanuel Günther (s76954)
 */
public class VideoMetadata {
    static int DEFAULT_FRAME_PERIOD = 40; // Frame period of the video to stream, in ms
    //static int DEFAULT_FRAME_PERIOD = 1000; // Debugging
    private int framerate;
    private double duration; // in seconds
    static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public VideoMetadata(int framerate, double duration) {
        this.framerate = framerate;
        this.duration = duration;
    }

    public VideoMetadata(int framerate) {
        this(framerate, 0.0);
    }

    public int getFramerate() {
        return this.framerate;
    }

    public double getDuration() {
        return this.duration;
    }


    /** Get the metadata from a video file.
     *
     *  If no metadata is available, all fields are zero-initialized except for
     *  the framerate. Because the framerate is strongly required,
     *  it is set to a default value.
     *
     *  @param filename Name of the video file
     *  @return metadata structure containing the extracted information
     */
    public static VideoMetadata getVideoMetadata(String dir, String filename) {
        VideoMetadata meta;

        String[] splittedFilename = filename.split("\\.");
        switch (splittedFilename[splittedFilename.length-1]) {
            case "avi":
                meta = AviMetadataParser.parse(dir+filename);
                break;
            case "mov":
                meta = QuickTimeMetadataParser.parse(dir+filename);
                break;
            default:
                logger.log(Level.WARNING, "File extension not recognized: " + filename);
            case "mjpg":
            case "mjpeg":
                logger.log(Level.FINE, "Framerate: " + 1000 / DEFAULT_FRAME_PERIOD);
                meta = new VideoMetadata(1000 / DEFAULT_FRAME_PERIOD);
                break;
        }

        assert meta != null : "video.VideoMetadata of file " + filename + " was not initialized correctly";
        return meta;
    }


}

