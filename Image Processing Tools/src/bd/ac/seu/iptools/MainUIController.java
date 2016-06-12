/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bd.ac.seu.iptools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;

/**
 *
 * @author kmhasan
 */
public class MainUIController implements Initializable {

    @FXML
    private Label statusLabel;
    @FXML
    private AnchorPane leftPane;
    @FXML
    private AnchorPane rightPane;
    private BufferedImage inputImage;
    private BufferedImage outputImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        inputImage = null;
        outputImage = null;
    }

    @FXML
    private void handleFileOpenAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(null);
            BufferedImage bufferedImage = ImageIO.read(file);
            inputImage = bufferedImage;
            if (bufferedImage != null) {
                outputImage = null;
            }
            statusLabel.setText("Opened " + file.getName() + " [" + bufferedImage.getWidth() + "x" + bufferedImage.getHeight() + "]");

            displayImage(inputImage, leftPane);
        } catch (IOException ex) {
            Logger.getLogger(MainUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleFileSaveAction(ActionEvent event) {
        try {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(null);
            String format = file.getName().substring(file.getName().indexOf(".") + 1);
            ImageIO.write(outputImage, format, file);
            statusLabel.setText("Saving to " + file.getName() + " format: " + format);
        } catch (IOException ex) {
            Logger.getLogger(MainUIController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void handleRGBtoGrayscaleAction(ActionEvent event) {
        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        for (int c = 0; c < inputImage.getWidth(); c++) {
            for (int r = 0; r < inputImage.getHeight(); r++) {
                int rgb = inputImage.getRGB(c, r);
                int rr = (rgb >> 16) & 0xFF;
                int gg = (rgb >> 8) & 0xFF;
                int bb = (rgb >> 0) & 0xFF;

                int intensity = (int) (rr * 0.72 + gg * 0.21 + bb * 0.07);

                rgb = (intensity << 16) | (intensity << 8) | intensity;
                outputImage.setRGB(c, r, rgb);
            }
        }
        displayImage(outputImage, rightPane);
    }

    @FXML
    private void handleRotateClockwiseAction(ActionEvent event) {
        outputImage = new BufferedImage(inputImage.getHeight(), inputImage.getWidth(), inputImage.getType());
        for (int c = 0; c < inputImage.getWidth(); c++) {
            for (int r = 0; r < inputImage.getHeight(); r++) {
                int rgb = inputImage.getRGB(c, r);
                outputImage.setRGB(inputImage.getHeight() - 1 - r, c, rgb);
            }
        }
        displayImage(outputImage, rightPane);
    }

    private void displayImage(BufferedImage bufferedImage, AnchorPane anchorPane) {
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        ImageView imageView = new ImageView(image);
        anchorPane.getChildren().removeAll();
        anchorPane.getChildren().add(imageView);
    }

    @FXML
    private void handleBoxBlurAction(ActionEvent event) {
        // Implemented by Kazi Fazle Azim Rabi
        System.out.println("inside");

        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());

        int blurKernel[][] = {
/*
            {-1, 0, +1},
            {-1, 0, +1},
            {-1, 0, +1}
*/
            {-1, -1, -1},
            {0, 0, 0},
            {+1, +1, +1}
                
        };

        int offset = blurKernel.length / 2;

        for (int c = offset; c < inputImage.getWidth() - offset; c++) {
            for (int r = offset; r < inputImage.getHeight() - offset; r++) {
                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;
                for (int dx = -offset; dx <= +offset; dx++) {
                    for (int dy = -offset; dy <= +offset; dy++) {
                        int newc = c + dx;
                        int newr = r + dy;

                        int newrgb = inputImage.getRGB(newc, newr);
                        int newrr = (newrgb >> 16) & 0xFF;
                        int newgg = (newrgb >> 8) & 0xFF;
                        int newbb = (newrgb >> 0) & 0xFF;

                        int multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newrr;
                        sumRed += multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newgg;
                        sumGreen += multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newbb;
                        sumBlue += multipliedValue;
                    }
                }
/*
                sumRed /= 982;
                sumGreen /= 982;
                sumBlue /= 982;
*/
                int rgb = (sumRed << 16) | (sumGreen << 8) | (sumBlue);
                outputImage.setRGB(c, r, rgb);
                // DIVIDE the sum values by 9
                // pack the new RGBs into one integer
                // put the integer in the new image
            }
        }
        System.out.println("done");
        displayImage(outputImage, rightPane);
    }

    /*
    @FXML
    private void handleBoxBlurAction(ActionEvent event) {
        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(),
                inputImage.getType());

        int blurKernel[][] = {
            {1, 1, 1},
            {1, 1, 1},
            {1, 1, 1}
        };

        int offset = blurKernel.length / 2;

        for (int c = offset; c < inputImage.getWidth() - offset; c++) {
            for (int r = offset; r < inputImage.getHeight() - offset; r++) {
                int sumRed = 0;
                int sumGreen = 0;
                int sumBlue = 0;
                for (int dx = -offset; dx <= +offset; dx++) {
                    for (int dy = -offset; dy <= +offset; dy++) {
                        int newc = c + dx;
                        int newr = r + dy;

                        int newrgb = inputImage.getRGB(newc, newr);
                        int newrr = (newrgb >> 16) & 0xFF;
                        int newgg = (newrgb >> 8) & 0xFF;
                        int newbb = (newrgb >> 0) & 0xFF;
                        
                        int multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newrr;
                        sumRed += multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newgg;
                        sumGreen += multipliedValue;
                        multipliedValue = blurKernel[dy + offset][dx + offset] * newbb;
                        sumBlue += multipliedValue;
                    }
                }
                // DIVIDE the sum values by 9
                // pack the new RGBs into one integer
                // put the integer in the new image
            }
        }
        displayImage(outputImage, rightPane);
    }
     */
}
