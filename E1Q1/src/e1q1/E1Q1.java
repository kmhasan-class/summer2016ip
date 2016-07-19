/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package e1q1;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author kmhasan
 */
public class E1Q1 {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public E1Q1() {
        System.out.println("Version " + Core.VERSION);
        
        // Read input.jpg
        Mat mat = Imgcodecs.imread("input.jpg");
        Mat outputMat = new Mat();
        Mat cannyMat = new Mat();
        
        // Convert to grayscale
        Imgproc.cvtColor(mat, outputMat, Imgproc.COLOR_BGR2GRAY);
        
        // Blur the image
        Imgproc.blur(outputMat, outputMat, new Size(3, 3));
        
        // Produce a Canny edge detected version
        Imgproc.Canny(outputMat, cannyMat, 10, 100);
        
        // Blur the detected edges (otherwise there were too many jagged edges
        Imgproc.blur(cannyMat, cannyMat, new Size(3, 3));

        // Increase the brightness of the grayscale image by 25%
        Core.multiply(outputMat, new Scalar(1.25), outputMat);

        // Subtract the detected edges from the grayscale to get the black borders
        Core.subtract(outputMat, cannyMat, outputMat);
        
        // Finally write the output
        Imgcodecs.imwrite("output1.jpg", outputMat);
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new E1Q1();
    }

}
