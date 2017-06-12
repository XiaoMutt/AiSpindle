/*
 * GPLv3
 */
package AiSpindle;

import features.TubenessProcessor;
import ij.ImagePlus;
import ij.measure.CurveFitter;
import ij.plugin.filter.Convolver;
import ij.process.FloatProcessor;

/**
 *
 * @author Johannes Schindelin, Jan Eglinger, Mark Hiner see
 * https://imagej.net/Directionality edited by Xiao Zhou
 *
 */
public class DirectionDetector {

    private static final TubenessProcessor TP = new TubenessProcessor(0.9, true);

    private static final int NBINS = 90;//number of bins in the direction histogram;

    public static DirecionAnalysisResult calculateDirection(ImagePlus imagePlus) {
        ImagePlus imp = TP.generateImage(imagePlus);
        //ImagePlus imp=imagePlus.duplicate();
        //imp.show();
        double[] dir = calculateLocalGradientOrientation(imp.getProcessor().convertToFloatProcessor());
        return fitGaussian(dir);

    }

    private static double[] calculateLocalGradientOrientation(final FloatProcessor ip) {
        final double[] norm_dir = new double[NBINS]; // histo from -pi to pi;
        final FloatProcessor grad_x = (FloatProcessor) ip.duplicate();
        final FloatProcessor grad_y = (FloatProcessor) ip.duplicate();
        final Convolver convolver = new Convolver();
        final float[] kernel_y = new float[]{
            -2f, -1f, 0f, 1f, 2f,
            -3f, -2f, 0f, 2f, 3f,
            -4f, -3f, 0f, 3f, 4f,
            -3f, -2f, 0f, 2f, 3f,
            -2f, -1f, 0f, 1f, 2f}; // That's gx, but we want to have a 90º shift, to comply to the rest of the plugin
        final float[] kernel_x = new float[]{
            2f, 3f, 4f, 3f, 2f,
            1f, 2f, 3f, 2f, 1f,
            0, 0, 0, 0, 0,
            -1f, -2f, -3f, -2f, -1f,
            -2f, -3f, -4f, -3f, -2f};

        convolver.convolveFloat(grad_x, kernel_x, 5, 5);
        convolver.convolveFloat(grad_y, kernel_y, 5, 5);

        final float[] pixels_gx = (float[]) grad_x.getPixels();
        final float[] pixels_gy = (float[]) grad_y.getPixels();

        double norm;
        double angle;
        int histo_index;
        float dx, dy;
        for (int i = 0; i < pixels_gx.length; i++) {
            dx = pixels_gx[i];
            dy = -pixels_gy[i]; // upright orientation
            norm = Math.sqrt(dx * dx + dy * dy); // We keep the square so as to have the same dimension that Fourier components analysis
            angle = Math.atan(dy / dx);
            histo_index = (int) ((NBINS / 2.0) * (1 + angle / (Math.PI / 2))); // where to put it
            if (histo_index == NBINS) {
                histo_index = 0; // circular shift in case of exact vertical orientation
            }
            norm_dir[histo_index] += norm; // we put the norm, the stronger the better
        }
        return norm_dir;
    }

    /**
     * This method tries to fit a Gaussian to the highest peak of each
     * directionality histogram.
     *
     * @param dir curve data to be processed.
     */
    private static DirecionAnalysisResult fitGaussian(double[] dir) {

        double[] params;
        double[] padded_dir;
        double[] padded_bins;

        // Shift found peak to the center (periodic issue) and add to fitter        
        int imax, shift_index, current_index;
        double ymax = Double.NEGATIVE_INFINITY;
        imax = 0;//the index of the maxmium value
        for (int j = 0; j < dir.length; j++) {
            if (dir[j] > ymax) {
                ymax = dir[j];
                imax = j;
            }

        }
        double[] bins = prepareBins();
        padded_dir = new double[bins.length];
        padded_bins = new double[bins.length];
        shift_index = bins.length / 2 - imax;
        for (int j = 0; j < bins.length; j++) {
            current_index = j - shift_index;
            if (current_index < 0) {
                current_index += bins.length;
            }
            if (current_index >= bins.length) {
                current_index -= bins.length;
            }
            padded_dir[j] = dir[current_index];
            padded_bins[j] = bins[j];
        }

        // Do fit
        CurveFitter fitter = new CurveFitter(padded_bins, padded_dir);
        fitter.doFit(CurveFitter.GAUSSIAN);

        //Get fitting parameters;
        params = fitter.getParams();
        if (shift_index < 0) { // back into orig coordinates
            params[2] += (bins[-shift_index] - bins[0]);
        } else {
            params[2] -= (bins[shift_index] - bins[0]);
        }
        params[3] = Math.abs(params[3]); // std is positive

        double[] analysis = new double[3];

        /* no use for amount;
        double amount = 0;
        for (int j = 0; j < dir.length; j++) {
            amount += dir[j];
        }
         */
        analysis[0] = params[2];//center
        analysis[1] = params[3];//standard derivation
        analysis[2] = fitter.getFitGoodness();//goodness of fit

        return new DirecionAnalysisResult(analysis);
    }

    /**
     * Generate a bin array of angle in degrees, from -pi/2 to pi/2.
     *
     * @return a double array of n elements, the angles in radians
     */
    private static double[] prepareBins() {
        final double[] bins = new double[NBINS];
        for (int i = 0; i < NBINS; i++) {
            bins[i] = i * Math.PI / NBINS - Math.PI / 2;
        }
        return bins;
    }
}
