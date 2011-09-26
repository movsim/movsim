package org.movsim.utilities.impl;

import java.util.List;


public class ExponentialMovingAverage {

    private double tau;
    
    public ExponentialMovingAverage(double tau) {
        this.tau = tau;
    } 
        
    
    public double calcEMA(double time, final List<XYDataPoint> timeSeries){
        if(timeSeries.isEmpty() ){
            return 0;
        }
        double norm = 0;
        double result = 0;
        for(XYDataPoint dp : timeSeries){
            final double phi = weight(time, dp.getX());
            norm += phi;
            result += phi*dp.getY();
        }
        return result/norm;
    }
    
    private double weight(double t1, double t2){
        return Math.exp(-Math.abs( (t1-t2)/tau ));
    }
    

    
}
