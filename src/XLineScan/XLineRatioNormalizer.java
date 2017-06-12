/*
 * GPLv3
 */
package XLineScan;

/**
 *
 * @author Xiao Zhou
 */
public class XLineRatioNormalizer {

    private final double[] NData;
    private final double[] DData;
    private final double[] RawRatios;
    private final double[] NormalizedRatios;
    private final double[] NormalizedNumeratorData;
    private final double[] NormalizedDenominatorData;
    private final int NormalizedLength;
    private boolean InfinityWarning;

    /**
     *
     * @param numeratorData Numerator data array
     * @param denominatorData denominator data array
     * @param normalizedLength length the data should be normalized to
     */
    public XLineRatioNormalizer(double[] numeratorData, double[] denominatorData, int normalizedLength) {
        NData = numeratorData;
        DData = denominatorData;
        RawRatios = new double[denominatorData.length];
        NormalizedRatios = new double[normalizedLength];
        NormalizedNumeratorData = new double[normalizedLength];
        NormalizedDenominatorData = new double[normalizedLength];
        NormalizedLength = normalizedLength;
        InfinityWarning = false;

        calculateAndNormalize();
    }

    /**
     * Calculate the ratio of numerator/denominator data and normalize the data
     * to the specified length;
     */
    private void calculateAndNormalize() {
        if (NData.length == DData.length) {
            int n = NData.length;
            for (int i = 0; i < n; i++) {
                if (NData[i] == 0 && DData[i] == 0) {
                    RawRatios[i] = 0;
                } else if (DData[i] == 0) {
                    RawRatios[i] = Double.POSITIVE_INFINITY;
                    InfinityWarning = true;
                } else {
                    RawRatios[i] = NData[i] / DData[i];
                }
            }

            //normalize to standardlength;
            double step = ((double) n - 1.0) / (NormalizedLength - 1.0);
            double x;
            Double ceilCellIndex, floorCellIndex;

            for (int i = 0; i < NormalizedLength; i++) {
                x = i * step;
                if (x > n - 1) {//sometimes decimal roundup will lead to x large than n-1 by a tiny fraction which causes overflow of arrays below;
                    x = n - 1;
                }
                floorCellIndex = Math.floor(x);
                ceilCellIndex = Math.ceil(x);
                NormalizedRatios[i] = (x - floorCellIndex) * (RawRatios[ceilCellIndex.intValue()] - RawRatios[floorCellIndex.intValue()]) + RawRatios[floorCellIndex.intValue()];
                NormalizedNumeratorData[i] = (x - floorCellIndex) * (NData[ceilCellIndex.intValue()] - NData[floorCellIndex.intValue()]) + NData[floorCellIndex.intValue()];
                NormalizedDenominatorData[i] = (x - floorCellIndex) * (DData[ceilCellIndex.intValue()] - DData[floorCellIndex.intValue()]) + DData[floorCellIndex.intValue()];
            }
        }

    }
    
/**
 * 
 * @return true if there are infinity values in the data
 */
    public boolean isThereInfinityWarning() {
        return InfinityWarning;
    }
/**
 * 
 * @return normalized numerator data array
 */
    public double[] getNormalizedNumeratorData() {
        return NormalizedNumeratorData;
    }
/**
 * 
 * @return normalized denominator data array
 */
    public double[] getNormalizedDenominatorData() {
        return NormalizedDenominatorData;
    }
    
    /**
     * 
     * @return an array of normalized ratios
     */

    public double[] getNormalizedRatios() {
        return NormalizedRatios;
    }
    
    /**
     * 
     * @return an array of raw ratios
     */

    public double[] getRawRatios() {
        return RawRatios;
    }
}
