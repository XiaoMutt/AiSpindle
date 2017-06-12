/*
 * GPLv3
 */
package AiSpindle;

/**
 *
 * @author Xiao Zhou
 */
    public class DirecionAnalysisResult{
        public final double angle;
        public final double std;
        public final double goodness;
        
        public DirecionAnalysisResult(double[] params){
            angle=params[0];
            std=params[1];
            goodness=params[2];
        }
    }