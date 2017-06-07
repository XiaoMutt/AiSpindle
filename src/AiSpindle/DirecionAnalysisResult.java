/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AiSpindle;

/**
 *
 * @author Mutt
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