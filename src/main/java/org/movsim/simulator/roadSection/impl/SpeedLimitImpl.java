package org.movsim.simulator.roadSection.impl;

import org.movsim.simulator.roadSection.SpeedLimit;

public class SpeedLimitImpl implements SpeedLimit {

    private double position;
    
    private double speed;
    
    public SpeedLimitImpl(double pos, double speed){
        this.position = pos;
        this.speed = speed;
    }
    
    @Override
    public double getPosition() {
        return position;
    }

    @Override
    public double getSpeedLimitKmh() {
        return speed*3.6;
    }

}
