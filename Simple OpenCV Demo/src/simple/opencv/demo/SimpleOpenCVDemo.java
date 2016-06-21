/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simple.opencv.demo;

import java.util.Arrays;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 *
 * @author kmhasan
 */
public class SimpleOpenCVDemo {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println("OpenCV Version " + Core.VERSION);
        
        Mat m = new Mat(4, 5, CvType.CV_8UC1);
        double array[] = m.get(0, 0);
        m.put(0, 0, 32, 32, 43);
        System.out.print(Arrays.toString(array));
        System.out.println(m.dump());
    }
    
}
