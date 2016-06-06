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
}