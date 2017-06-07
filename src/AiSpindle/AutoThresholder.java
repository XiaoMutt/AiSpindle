package AiSpindle;

import ij.ImagePlus;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;

/**
 *
 * @author ImageJ Autothresholder modified by Xiao Zhou from ImageJ source code
 * Convert the image to 8-bit gray scale and apply Yen Autothresholder to an
 * ImagePlus object
 *
 */
public class AutoThresholder {

    public static void autoTheshold(ImagePlus imp) {
        if (imp == null||imp.getBitDepth()!=8) {
            return;
        }
        ImageProcessor ip = imp.getProcessor();
        int xe = ip.getWidth();
        int ye = ip.getHeight();
        int background = 0;
        int foreground = 255;
        int[] data = ip.getHistogram();

        int minBin = 0, maxBin = data.length-1;
        while (minBin < data.length) {
            if (data[minBin] == 0) {
                minBin++;
            } else {
                break;
            }
        }

        while (maxBin > -1) {
            if (data[maxBin] == 0) {
                maxBin--;
            } else {
                break;
            }
        }
        int threshold = minBin;
        if(minBin<maxBin){
            int[] data2 = new int[maxBin - minBin + 1];
            for (int i = minBin; i <= maxBin; i++) {
                data2[i - minBin] = data[i];
            }
            if (data2.length > 1) {
                threshold += Yen(data2);
            }
        }
        //threshold it
        for (int y = 0; y < ye; y++) {
            for (int x = 0; x < xe; x++) {
                if (ip.getPixel(x, y) > threshold) {
                    ip.putPixel(x, y, foreground);
                } else {
                    ip.putPixel(x, y, background);
                }
            }
        }

    }

    /**
     * Credit: Implements Yen thresholding method 1) Yen J.C., Chang F.J., and
     * Chang S. (1995) "A New Criterion for Automatic Multilevel Thresholding"
     * IEEE Trans. on Image Processing, 4(3): 370-378 2) Sezgin M. and Sankur B.
     * (2004) "Survey over Image Thresholding Techniques and Quantitative
     * Performance Evaluation" Journal of Electronic Imaging, 13(1): 146-165
     * http://citeseer.ist.psu.edu/sezgin04survey.htmlM. Emre Celebi *
     * 06.15.2007Ported to ImageJ plugin by G.Landini from E Celebi's
     * fourier_0.8 routines
     *
     *
     * @param data histogram data
     * @return threshold
     */
    private static int Yen(int[] data) {

        int threshold;
        int ih, it;
        double crit;
        double max_crit;
        double[] norm_histo = new double[data.length];
        /* normalized histogram */

        double[] P1 = new double[data.length];
        /* cumulative normalized histogram */

        double[] P1_sq = new double[data.length];
        double[] P2_sq = new double[data.length];

        int total = 0;
        for (ih = 0; ih < data.length; ih++) {
            total += data[ih];
        }

        for (ih = 0; ih < data.length; ih++) {
            norm_histo[ih] = (double) data[ih] / total;
        }

        P1[0] = norm_histo[0];
        for (ih = 1; ih < data.length; ih++) {
            P1[ih] = P1[ih - 1] + norm_histo[ih];
        }

        P1_sq[0] = norm_histo[0] * norm_histo[0];
        for (ih = 1; ih < data.length; ih++) {
            P1_sq[ih] = P1_sq[ih - 1] + norm_histo[ih] * norm_histo[ih];
        }

        P2_sq[data.length - 1] = 0.0;
        for (ih = data.length - 2; ih >= 0; ih--) {
            P2_sq[ih] = P2_sq[ih + 1] + norm_histo[ih + 1] * norm_histo[ih + 1];
        }

        /* Find the threshold that maximizes the criterion */
        threshold = -1;
        max_crit = Double.MIN_VALUE;
        for (it = 0; it < data.length; it++) {
            crit = -1.0 * ((P1_sq[it] * P2_sq[it]) > 0.0 ? Math.log(P1_sq[it] * P2_sq[it]) : 0.0) + 2 * ((P1[it] * (1.0 - P1[it])) > 0.0 ? Math.log(P1[it] * (1.0 - P1[it])) : 0.0);
            if (crit > max_crit) {
                max_crit = crit;
                threshold = it;
            }
        }
        return threshold;
    }
}
