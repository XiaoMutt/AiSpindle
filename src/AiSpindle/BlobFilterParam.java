/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
