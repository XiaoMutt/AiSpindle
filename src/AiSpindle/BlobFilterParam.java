/*
 * GPLv3
 */
package AiSpindle;

/**
 *
 * @author Xiao Zhou
 *
 */
public class BlobFilterParam {

    public final String FilterMethod;
    public final double Min;
    public final double Max;

    public BlobFilterParam(String filterMethods, double min, double max) {
        FilterMethod = filterMethods;
        Min = min;
        Max = max;
    }
}
