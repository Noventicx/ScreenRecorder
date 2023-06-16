import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Tim on 09.04.2016.
 */
public class ScreenCaptureGui extends JFrame implements ActionListener {
    private JButton jbStart = new JButton("start");
    private JButton jbStop = new JButton("stop");
    private JButton jbPause = new JButton("pause");

    private CaptureScreen capture = new CaptureScreen("a.mp4", 1920, 1080);

    public ScreenCaptureGui() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        JPanel jbButtons = new JPanel(new GridLayout(1, 3));
        jbButtons.add(jbStart);
        jbButtons.add(jbStop);
        jbButtons.add(jbPause);

        jbStart.addActionListener((ActionListener) this);
        jbStop.addActionListener((ActionListener) this);
        jbPause.addActionListener((ActionListener) this);

        jbStop.setEnabled(false);
        jbPause.setEnabled(false);

        add(jbButtons);
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==jbStart) {
            try {
                capture.startCapture();
            } catch (AWTException e1) {
                e1.printStackTrace();
            }
            jbStart.setEnabled(false);
            jbStop.setEnabled(true);
            jbPause.setEnabled(true);
        }

        if(e.getSource()==jbStop) {

                capture.stopCapture();

            jbStart.setEnabled(true);
            jbStop.setEnabled(false);
            jbPause.setEnabled(false);
        }


        if(e.getSource()==jbPause) {
            if(capture.isPaused()) {
                jbPause.setText("pause");
                capture.continueCapture();
            }else{
                jbPause.setText("resume");
                capture.pauseCapture();
            }

            jbStart.setEnabled(false);
            jbStop.setEnabled(true);
            jbPause.setEnabled(true);

        }

    }

    public static void main(String[] args) {
        new ScreenCaptureGui();
    }

}
