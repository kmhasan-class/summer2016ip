/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opencv.camera.test;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

/**
 *
 * @author kmhasan
 */
public class FXMLDocumentController implements Initializable {

    private static boolean applicationShouldClose = false;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @FXML
    private ImageView cameraView;
    private VideoCapture videoCapture;
    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        videoCapture = new VideoCapture();
    }

    @FXML
    private void handleStartCameraAction(ActionEvent event) {
        videoCapture.open(0);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> cameraView.setImage(grabFrame()), 0, 10, TimeUnit.MILLISECONDS);
    }

    private Image grabFrame() {
        if (applicationShouldClose) {
            if (videoCapture.isOpened()) {
                videoCapture.release();
            }
            scheduledExecutorService.shutdown();
        }
        
        Image imageToShow = null;
        Mat frame = new Mat();
        int frameNum = 0;
        if (videoCapture.isOpened()) {
            try {
                videoCapture.read(frame);

                if (!frame.empty()) {
                    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
                    
                    for (int r = 300; r < 400; r++)
                        for (int c = 300; c < 400; c++) {
                            double value[] = frame.get(r, c);
                            double newValue = value[0] * 1.25;
                            if (newValue > 255)
                                newValue = 255;
                            frame.put(r, c, newValue);
                        }
                    
                    MatOfByte buffer = new MatOfByte();
                    Imgcodecs.imencode(".png", frame, buffer);
                    imageToShow = new Image(new ByteArrayInputStream(buffer.toArray()));
                }

            } catch (Exception e) {
                System.err.println(e);
            }
        }

        return imageToShow;
    }

    @FXML
    private void handleStopCameraAction(ActionEvent event) {
        if (videoCapture.isOpened()) {
            videoCapture.release();
        }
        scheduledExecutorService.shutdown();
    }

    public static void exit() {
        applicationShouldClose = true;
    }
}
