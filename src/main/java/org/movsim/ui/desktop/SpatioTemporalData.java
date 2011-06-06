package org.movsim.ui.desktop;


public class SpatioTemporalData {
    
    private static double dx;
    
    public static void setDx(double dx) {
        SpatioTemporalData.dx = dx;
    }

    public static double getDx() {
        return dx;
    }

    private double time;
    private double averageSpeed;
    private double x;
    
    public SpatioTemporalData(double time, double x, double d) {
        this.time = time;
        this.x = x;
        this.averageSpeed = d;
    }

    
    public double getAverageSpeed() {
        return averageSpeed;
    }

    public double getTime() {
        return time;
    }
    
    public double getX() {
        return x;
    }}
