/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package colortograyscale;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author kmhasan
 */
public class ColorToGrayscale {

    /**
     * @param args the command line arguments
     */
    /*
    HOMEWORK:
    1. Flip the image (both horizontally and vertically)
    2. Rotate the image by multiples of 90 degrees
    */
    public static void main(String[] args) {
        try {
            BufferedImage image = ImageIO.read(new File("bmw.bmp"));
            int height = image.getHeight();
            int width = image.getWidth();
            System.out.printf("[%d x %d]\n", width, height);
            
            for (int c = 0; c < width; c++)
                for (int r = 0; r < height; r++) {
                    int rgb = image.getRGB(c, r);
                    int red   = (rgb >> 16) & 0xFF;
                    int green = (rgb >>  8) & 0xFF;
                    int blue  = (rgb >>  0) & 0xFF;
                    
                    int average = (int) (0.21 * red + 0.72 * green + 0.07 * blue);
                    int rr = average;
                    int gg = average;
                    int bb = average;
                    if (red > 200)
                        rr = (int) (red * 1.5);
                    if (rr > 255)
                        rr = 255;
                    rgb = (rr << 16) | (gg << 8) | bb;
                    
                    image.setRGB(c, r, rgb);
                }
            
            ImageIO.write(image, "jpg", new File("output2.jpg"));
        } catch (IOException ex) {
            Logger.getLogger(ColorToGrayscale.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
