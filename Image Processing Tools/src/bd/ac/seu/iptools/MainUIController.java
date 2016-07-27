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

    private BufferedImage toBufferedImage(double grayPixels[][]) {
        BufferedImage image = new BufferedImage(grayPixels[0].length, grayPixels.length, BufferedImage.TYPE_3BYTE_BGR);
        for (int r = 0; r < grayPixels.length; r++) {
            for (int c = 0; c < grayPixels[r].length; c++) {
                int value = (int) grayPixels[r][c];
                value = (value << 0) | (value << 8) | (value << 16);
                image.setRGB(c, r, value);
            }
        }
        return image;
    }

    private BufferedImage toBufferedImage(int grayPixels[][]) {
        BufferedImage image = new BufferedImage(grayPixels[0].length, grayPixels.length, BufferedImage.TYPE_3BYTE_BGR);
        for (int r = 0; r < grayPixels.length; r++) {
            for (int c = 0; c < grayPixels[r].length; c++) {
                int value = grayPixels[r][c];
                value = (value << 0) | (value << 8) | (value << 16);
                image.setRGB(c, r, value);
            }
        }
        return image;
    }

    private BufferedImage toGrayScale(BufferedImage rgbImage) {
        BufferedImage grayImage = new BufferedImage(rgbImage.getWidth(), rgbImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int c = 0; c < rgbImage.getWidth(); c++) {
            for (int r = 0; r < rgbImage.getHeight(); r++) {
                int rgb = rgbImage.getRGB(c, r);
                int rr = (rgb >> 16) & 0xFF;
                int gg = (rgb >> 8) & 0xFF;
                int bb = (rgb >> 0) & 0xFF;

                int intensity = (int) (rr * 0.72 + gg * 0.21 + bb * 0.07);

                rgb = (intensity << 16) | (intensity << 8) | intensity;
                grayImage.setRGB(c, r, rgb);
            }
        }

        return grayImage;
    }

    private double[][] toGrayScaleArray(BufferedImage rgbImage) {
        double pixels[][] = new double[rgbImage.getHeight()][rgbImage.getWidth()];
        for (int c = 0; c < rgbImage.getWidth(); c++) {
            for (int r = 0; r < rgbImage.getHeight(); r++) {
                int rgb = rgbImage.getRGB(c, r);
                int rr = (rgb >> 16) & 0xFF;
                int gg = (rgb >> 8) & 0xFF;
                int bb = (rgb >> 0) & 0xFF;

                double intensity = rr * 0.72 + gg * 0.21 + bb * 0.07;
                pixels[r][c] = intensity;
            }
        }

        return pixels;
    }

    public BufferedImage otsusThreshold(BufferedImage grayscaleImage) {
        BufferedImage binaryImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        int frequency[] = new int[256];
        double p[] = new double[frequency.length];
        double P1[] = new double[frequency.length];
        double m[] = new double[frequency.length];
        double sB[] = new double[frequency.length]; // sigma square B 10.3-17
        double mG;
        int M = binaryImage.getWidth();
        int N = binaryImage.getHeight();

        for (int r = 0; r < grayscaleImage.getHeight(); r++) {
            for (int c = 0; c < grayscaleImage.getWidth(); c++) {
                int intensity = grayscaleImage.getRGB(c, r) & 0xFF;
                frequency[intensity]++;
            }
        }
        System.out.println("Histogram done");

        // Step 1, 2 and 3
        for (int i = 0; i < frequency.length; i++) {
            p[i] = ((double) frequency[i]) / (M * N);
            if (i == 0) {
                P1[i] = p[i];
                m[i] = i * p[i];
            } else {
                P1[i] = P1[i - 1] + p[i];
                m[i] = m[i - 1] + i * p[i];
            }
        }
        System.out.println("Cumulative sums done");

        // step 4
        mG = m[m.length - 1];
        double highestVariance = Double.MIN_VALUE;
        int bestK = -1;
        for (int k = 0; k < frequency.length; k++) {
            double numerator = mG * P1[k] - m[k];
            double denominator = P1[k] * (1 - P1[k]);
            sB[k] = numerator * numerator / denominator;
            if (sB[k] > highestVariance) {
                highestVariance = sB[k];
                bestK = k;
            }
        }
        System.out.println("Calculated best K");

        int threshold = bestK;
        for (int r = 0; r < grayscaleImage.getHeight(); r++) {
            for (int c = 0; c < grayscaleImage.getWidth(); c++) {
                int intensity = grayscaleImage.getRGB(c, r) & 0xFF;
                if (intensity < threshold) {
                    binaryImage.setRGB(c, r, 0);
                } else {
                    binaryImage.setRGB(c, r, (0xFF << 16 | 0xFF << 8 | 0xFF));
                }
            }
        }
        System.out.println("Applied threshold");
        return binaryImage;
    }

    private BufferedImage myThreshold(BufferedImage grayscaleImage, int threshold) {
        BufferedImage binaryImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        int M = binaryImage.getWidth();
        int N = binaryImage.getHeight();

        for (int r = 0; r < grayscaleImage.getHeight(); r++) {
            for (int c = 0; c < grayscaleImage.getWidth(); c++) {
                int intensity = grayscaleImage.getRGB(c, r) & 0xFF;
                if (intensity < threshold) {
                    binaryImage.setRGB(c, r, 0);
                } else {
                    binaryImage.setRGB(c, r, (0xFF << 16 | 0xFF << 8 | 0xFF));
                }
            }
        }
        return binaryImage;
    }

    @FXML
    private void handleRGBtoGrayscaleAction(ActionEvent event) {
        outputImage = toGrayScale(inputImage);
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

    private double[][] applyKernelArray(double grayPixels[][], int kernel[][]) {
        double outputPixels[][] = new double[grayPixels.length][grayPixels[0].length];

        int offset = kernel.length / 2;
        int kernelSum = 0;
        for (int r = 0; r < kernel.length; r++) {
            for (int c = 0; c < kernel[r].length; c++) {
                kernelSum += Math.abs(kernel[r][c]);
            }
        }

        for (int c = offset; c < grayPixels[0].length - offset; c++) {
            for (int r = offset; r < grayPixels.length - offset; r++) {
                double sum = 0;
                for (int dx = -offset; dx <= +offset; dx++) {
                    for (int dy = -offset; dy <= +offset; dy++) {
                        sum += kernel[dy + offset][dx + offset] * grayPixels[r + dy][c + dx];
                    }
                }

                sum /= kernelSum;
                outputPixels[r][c] = sum;
            }
        }

        return outputPixels;
    }

    private BufferedImage applyKernel(BufferedImage image, int kernel[][]) {
        BufferedImage outputImage;
        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());

        int offset = kernel.length / 2;
        int kernelSum = 0;
        for (int r = 0; r < kernel.length; r++) {
            for (int c = 0; c < kernel[r].length; c++) {
                kernelSum += Math.abs(kernel[r][c]);
            }
        }

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
                        multipliedValue = kernel[dy + offset][dx + offset] * newrr;
                        sumRed += multipliedValue;
                        multipliedValue = kernel[dy + offset][dx + offset] * newgg;
                        sumGreen += multipliedValue;
                        multipliedValue = kernel[dy + offset][dx + offset] * newbb;
                        sumBlue += multipliedValue;
                    }
                }

                sumRed /= kernelSum;
                sumGreen /= kernelSum;
                sumBlue /= kernelSum;

                if (sumRed < 0) {
                    sumRed = 0;
                }
                if (sumRed > 255) {
                    sumRed = 255;
                }
                if (sumGreen < 0) {
                    sumGreen = 0;
                }
                if (sumGreen > 255) {
                    sumGreen = 255;
                }
                if (sumBlue < 0) {
                    sumBlue = 0;
                }
                if (sumBlue > 255) {
                    sumBlue = 255;
                }

                int rgb = (sumRed << 16) | (sumGreen << 8) | (sumBlue);
                outputImage.setRGB(c, r, rgb);
            }
        }

        return outputImage;
    }

    @FXML
    private void handleBoxBlurAction(ActionEvent event) {
        // Implemented by Kazi Fazle Azim Rabi
        System.out.println("inside");

        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());

        int blurKernel[][] = {
            {+1, +2, +1},
            {+2, +4, +2},
            {+1, +2, +1}
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

    @FXML
    private void handleCannyAction(ActionEvent event) {
        // kernels
        int gaussianKernel[][] = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
        };
        int sobelGx[][] = {
            {-1, 0, +1},
            {-2, 0, +2},
            {-1, 0, +1}
        };
        int sobelGy[][] = {
            {-1, -2, -1},
            {0, 0, 0},
            {+1, +2, +1}
        };

        double grayPixels[][] = null;
        double blurPixels[][] = null;
        double gx[][] = null;
        double gy[][] = null;
        double gn[][] = null;
        int gnh[][] = null;
        int gnl[][] = null;
        int output[][] = null;
        double M[][] = null;
        double alpha[][] = null;
        double tl; // low threshold
        double th; // high threshold

        // step 0
        // RGB -> Gray Scale
        grayPixels = toGrayScaleArray(inputImage);

        // step 1
        // Smooth the input image with a Gaussian filter
        blurPixels = applyKernelArray(grayPixels, gaussianKernel);

        // step 2
        // Calculate gradient vector components
        gx = applyKernelArray(blurPixels, sobelGx);
        gy = applyKernelArray(blurPixels, sobelGy);

        // step 2 and 3
        // Calculate non maxima suppressed image
        M = new double[gx.length][gx[0].length];
        alpha = new double[gx.length][gx[0].length];
        gn = new double[gx.length][gx[0].length];
        gnl = new int[gx.length][gx[0].length];
        gnh = new int[gx.length][gx[0].length];
        output = new int[gx.length][gx[0].length];
        double minM = Double.MAX_VALUE;
        double maxM = Double.MIN_VALUE;
        for (int r = 1; r < gx.length - 1; r++) {
            for (int c = 1; c < gx[0].length - 1; c++) {
                M[r][c] = Math.sqrt(gx[r][c] * gx[r][c] + gy[r][c] * gy[r][c]);
                alpha[r][c] = Math.atan(gy[r][c] / gx[r][c]);
                if (gx[r][c] != 0.0) {
                    if (alpha[r][c] < 0) {
                        alpha[r][c] += Math.PI / 2;
                    }
                } else {
                    alpha[r][c] = Math.PI / 2;
                }
                minM = Math.min(minM, M[r][c]);
                maxM = Math.max(maxM, M[r][c]);
                if ((alpha[r][c] >= -Math.PI / 8 * 1 && alpha[r][c] < Math.PI / 8 * 1)
                        || (alpha[r][c] >= Math.PI / 8 * 7 && alpha[r][c] < Math.PI / 8 * 9)) {
                    if (M[r][c] < M[r][c - 1] || M[r][c] < M[r][c + 1]) {
                        gn[r][c] = 0;
                    } else {
                        gn[r][c] = M[r][c];
                    }
                } else if (alpha[r][c] >= Math.PI / 8 * 1 && alpha[r][c] < Math.PI / 8 * 3) {
                    if (M[r][c] < M[r - 1][c + 1] || M[r][c] < M[r + 1][c - 1]) {
                        gn[r][c] = 0;
                    } else {
                        gn[r][c] = M[r][c];
                    }
                } else if (alpha[r][c] >= Math.PI / 8 * 3 && alpha[r][c] < Math.PI / 8 * 5) {
                    if (M[r][c] < M[r - 1][c] || M[r][c] < M[r + 1][c]) {
                        gn[r][c] = 0;
                    } else {
                        gn[r][c] = M[r][c];
                    }
                } else if (alpha[r][c] >= Math.PI / 8 * 5 && alpha[r][c] < Math.PI / 8 * 7) {
                    if (M[r][c] < M[r - 1][c - 1] || M[r][c] < M[r + 1][c + 1]) {
                        gn[r][c] = 0;
                    } else {
                        gn[r][c] = M[r][c];
                    }
                } else {
                    ;
                }
            }
        }

//        tl = minM + (maxM - minM) / 4.0;
//        th = minM + (maxM - minM) / 4.0 * 3.0;
        // need to add slider so that users can pick tl and th values
        tl = 5;
        th = 20;

        for (int r = 1; r < gn.length - 1; r++) {
            for (int c = 1; c < gn[0].length - 1; c++) {
                if (gn[r][c] > tl) {
                    gnl[r][c] = 0xFF;
                } else {
                    gnl[r][c] = 0;
                }
                if (gn[r][c] > th) {
                    gnh[r][c] = 0xFF;
                } else {
                    gnh[r][c] = 0;
                }

                // connectivity analysis
                for (int dr = -1; dr <= +1; dr++) {
                    for (int dc = -1; dc <= +1; dc++) {
                        if (gnh[r][c] > 0) {
                            output[r][c] = 255;
                            if (gnl[r + dr][c + dc] > 0)
                                output[r][c] = 255;
                        }
                    }
                }
            }
        }

        outputImage = toBufferedImage(output);
        statusLabel.setText(String.format("M %.2f %.2f Thresholds %.2f %.2f", minM, maxM, tl, th));
        /*
        BufferedImage gx = applyKernel(outputImage, sobelGx);
        BufferedImage gy = applyKernel(outputImage, sobelGy);

        calculateMagnitude(M, alpha, gx, gy);
         */
        //displayImage(inputImage, leftPane);
        displayImage(outputImage, rightPane);
    }

    /*
    @FXML
    private void handleCannyAction(ActionEvent event) {
        // kernels
        int gaussianKernel[][] = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
        };
        int sobelGx[][] = {
            {-1, 0, +1},
            {-2, 0, +2},
            {-1, 0, +1}
        };
        int sobelGy[][] = {
            {-1, -2, -1},
            {0, 0, 0},
            {+1, +2, +1}
        };
        BufferedImage M = null;
        double alpha[][] = null;

        outputImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        // step 0
        // RGB -> Gray Scale
        outputImage = toGrayScale(outputImage);

        // step 1
        // Smooth the input image with a Gaussian filter
        outputImage = applyKernel(outputImage, gaussianKernel);

        BufferedImage gx = applyKernel(outputImage, sobelGx);
        BufferedImage gy = applyKernel(outputImage, sobelGy);

        calculateMagnitude(M, alpha, gx, gy);
    }
     */
    private void calculateMagnitude(BufferedImage M, double[][] alpha, BufferedImage gx, BufferedImage gy) {
        M = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        alpha = new double[inputImage.getHeight()][inputImage.getWidth()];

        for (int c = 0; c < inputImage.getWidth(); c++) {
            for (int r = 0; r < inputImage.getHeight(); r++) {
                int valueGx = gx.getRGB(c, r) & 0xFF;
                int valueGy = gy.getRGB(c, r) & 0xFF;
                int valueM = (int) (Math.sqrt(valueGx * valueGx + valueGy * valueGy));
                if (valueM > 255) {
                    valueM = 255;
                }
                valueM = (valueM << 16) | (valueM << 8) | (valueM << 0);
                M.setRGB(c, r, valueM);
            }
        }
        M = myThreshold(M, 1);
        System.out.println("Displaying M");
        displayImage(M, rightPane);
    }

    @FXML
    private void handleTestOpAction(ActionEvent event) {
        double pixels[][] = toGrayScaleArray(inputImage);
        BufferedImage image = toBufferedImage(pixels);
        displayImage(image, rightPane);
    }
}
