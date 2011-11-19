package org.movsim.simulator.roadSection;


public class SpeedLimit {

    private double position;
    
    private double speed;
    
    public SpeedLimit(double pos, double speed){
        this.position = pos;
        this.speed = speed;
    }
    
    public double getPosition() {
        return position;
    }

    public double getSpeedLimitKmh() {
        return speed*3.6;
    }
}
