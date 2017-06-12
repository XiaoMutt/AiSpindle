/*
 * GPLv3
 */
package XLineScan;

/**
 *
 * @author Xiao Zhou
 *
 */
import ij.gui.Plot;

public class XLineScanResult {

    private double[] NumeratorProfile;
    private double[] NumeratorErrors;
    private double[] DenominatorProfile;
    private double[] DenominatorErrors;
    private double[] RatioProfile;
    private double[] RatioErrors;

    private Plot NumeratorProfilePlot;
    private Plot DenominatorProfilePlot;
    private Plot RatioProfilePlot;
    private final int Length;

    XLineScanResult(int length) {
        Length = length;
    }

    public void setNumeratorProfile(double[] profile, double[] errors) {
        NumeratorProfile = profile;
        NumeratorErrors = errors;
    }

    public void setDenominatorProfile(double[] profile, double[] errors) {
        DenominatorProfile = profile;
        DenominatorErrors = errors;
    }

    public void setRaitoProfile(double[] profile, double[] errors) {
        RatioProfile = profile;
        RatioErrors = errors;
    }

    public void drawPlots() {

        double[] x = new double[Length];
        for (int i = 0; i < Length; i++) {
            x[i] = i;
        }

        double temp;
        
        /* comparing min and max is due to that the imageJ Plot class has bugs;
           Plot class cannot handle data when they are all the same number;
        */
        
        NumeratorProfilePlot = new Plot("Numerator Profile", "Normalized Length", "Normalized Intensity");
        if ((temp = findMin(NumeratorProfile)) == findMax(NumeratorProfile)) {
            NumeratorProfilePlot.setLimits(0, Length, 0, temp * 1.2);
        }
        NumeratorProfilePlot.addPoints(x, NumeratorProfile, NumeratorErrors, Plot.LINE);
        NumeratorProfilePlot.show();

        DenominatorProfilePlot = new Plot("Denominator Profile", "Normalized Length", "Normalized Intensity");        
        if ((temp = findMin(DenominatorProfile)) == findMax(DenominatorProfile)) {
            DenominatorProfilePlot.setLimits(0, Length, 0, temp * 1.2);
        }
        DenominatorProfilePlot.addPoints(x, DenominatorProfile, DenominatorErrors, Plot.LINE);
        DenominatorProfilePlot.show();
        
        RatioProfilePlot = new Plot("Result Raio Profile", "Normalized Length", "Normalized Ratio");
        if ((temp = findMin(RatioProfile)) == findMax(RatioProfile)) {
            RatioProfilePlot.setLimits(0, Length, 0, temp * 1.2);
        }
        RatioProfilePlot.addPoints(x, RatioProfile, RatioErrors, Plot.LINE);
        RatioProfilePlot.show();

    }

    private double findMax(double[] array) {
        double max = array[0];
        for (double d : array) {
            if (d > max) {
                max = d;
            }

        }
        return max;

    }

    private double findMin(double[] array) {
        double min = array[0];
        for (double d : array) {
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

}
