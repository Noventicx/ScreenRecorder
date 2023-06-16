import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Tim on 09.04.2016.
 */
public class CaptureScreen implements ActionListener {

    private Robot robot;
    private Rectangle capBounds;
    private IMediaWriter writer;

    private String videoName;

    private int delayBetweenFrames = 40;
    private Timer timer = new Timer(delayBetweenFrames, this);
    private long start = 0;

    private long pause;

    private CaptureAudio audio = new CaptureAudio();
    private long audioLastFrame;
    private long frameCount;

    public CaptureScreen(String videoName, int w, int h) {

        this.videoName = videoName;
        capBounds = new Rectangle(w, h);

    }


    public void startCapture() throws AWTException {
        robot = new Robot();
        writer = ToolFactory.makeWriter(videoName);
        writer.addVideoStream(0, 0, IRational.make(25, 1), capBounds.width, capBounds.height);
        writer.addAudioStream(1, 0, 1, 44100);
        start = System.currentTimeMillis();

        frameCount = 0;
        audioLastFrame = 0;
        Thread t = new Thread(audio);

        t.start();
        timer.start();
    }

    public void stopCapture() {
        audio.stop();
        addAudioData();
        timer.stop();
        try {
            Thread.sleep(100);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        writer.close();
        start=0;
    }

    public void pauseCapture() {
        if(!timer.isRunning()) return;
        pause = System.currentTimeMillis();
        timer.stop();
        audio.setPause(true);
    }

    public void continueCapture() {
        if(timer.isRunning()) return;
        start+=(System.currentTimeMillis()-pause);
        timer.start();
        audio.setPause(false);
    }

    public boolean isPaused() {
        return !timer.isRunning() && start!=0;
    }

    public void actionPerformed(ActionEvent e) {
            if(e.getSource()==timer) {
                // 1.) Bild holen
                BufferedImage screen = getCaptureImage();
                //2.) Bild dem Video hinzufügem
                writer.encodeVideo(0, screen, System.currentTimeMillis()-start, TimeUnit.MILLISECONDS);
                addAudioData();
                frameCount++;
            }
    }

    private void addAudioData() {
        byte[] audioData = audio.getData();
        if(audioData.length>0) {
            writer.encodeAudio(1, audioByteToShort(audioData), audioLastFrame*delayBetweenFrames, TimeUnit.MILLISECONDS);
            audioLastFrame = frameCount;
        }

    }

    private short[] audioByteToShort(byte[] b) {
        short[] saudio = new short[b.length/2];
        for(int i = 0; i < saudio.length; i++)
            saudio[i] = (short)(((b[2*i]<<8)&0xFF00) | (b[2*i+1]&0xFF) & 0xFFFF);
        return saudio;
    }

    private BufferedImage getCaptureImage() {
        BufferedImage screen = robot.createScreenCapture(capBounds);
        if(screen.getType()!=BufferedImage.TYPE_3BYTE_BGR){
            BufferedImage image = new BufferedImage(screen.getWidth(), screen.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            Graphics2D g = image.createGraphics();
            g.drawImage(screen, 0, 0, null);
            
            screen = image;
            g.dispose();
        }
        return screen;
    }

    public static void main(String[] args) throws Exception {
        CaptureScreen cs = new CaptureScreen("a.mp4", 1920, 1080);
        cs.startCapture();
        Thread.sleep(2000);
        System.err.println("pause");
        cs.pauseCapture();
        Thread.sleep(2000);
        System.err.println("pause ende");
        cs.continueCapture();
        Thread.sleep(2000);
        cs.stopCapture();
        Thread.sleep(200);
    }

}
