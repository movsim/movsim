/**
 * Copyright (C) 2010, 2011 by Arne Kesting <movsim@akesting.de>, 
 *                             Martin Treiber <treibi@mtreiber.de>,
 *                             Ralph Germ <germ@ralphgerm.de>,
 *                             Martin Budden <mjbudden@gmail.com>
 *
 * ----------------------------------------------------------------------
 * 
 *  This file is part of 
 *  
 *  MovSim - the multi-model open-source vehicular-traffic simulator 
 *
 *  MovSim is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MovSim is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
 *  <http://www.movsim.org>.
 *  
 * ----------------------------------------------------------------------
 */
package org.movsim.simulator.vehicles.impl;

import org.movsim.input.model.VehicleInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.simulator.vehicles.VehicleGUI;
import org.movsim.simulator.vehicles.longbehavior.Memory;
import org.movsim.simulator.vehicles.longbehavior.Noise;
import org.movsim.simulator.vehicles.longbehavior.impl.MemoryImpl;
import org.movsim.simulator.vehicles.longbehavior.impl.NoiseImpl;
import org.movsim.simulator.vehicles.longmodels.LongitudinalModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VehicleImpl implements Vehicle, VehicleGUI{
    final static Logger logger = LoggerFactory.getLogger(VehicleImpl.class);
    
    private double length;
    private double position;
    private double oldPosition;
    private double speed;
    private double accModel;
    private double acc;
    
    private double reactionTime;
    
    private double maxDecel;
    
    private double distanceToTrafficlight;
    
    private int id;
    
    private int vehNumber;
    
    private double lane;
    private int targetLane;
    
    private boolean considerTrafficLight;
    private double accTrafficLight;

    private double speedlimit; // state variable
    
    
    private LongitudinalModel longModel;
    
    private Memory memory = null;
    
    private Noise noise = null;

    private CyclicBufferImpl cyclicBuffer; // TODO

    public VehicleImpl(int id, LongitudinalModel longModel, VehicleInput vehInput, CyclicBufferImpl cyclicBuffer ){
        this.id = id;
        
        length = vehInput.getLength();
        reactionTime = vehInput.getReactionTime();
        maxDecel = vehInput.getMaxDeceleration();
        
        this.longModel = longModel;
        this.cyclicBuffer = cyclicBuffer;
        
        oldPosition = 0;
        position = 0;
        speed = 0;
        acc = 0;
        
        distanceToTrafficlight = GAP_INFINITY;
        considerTrafficLight = false;
        
        speedlimit = Constants.MAX_VEHICLE_SPEED; 
        // TODO set isFromOnramp
        
        
        // effekt wirkungslos, wenn Modell nicht ueber entsprechende Modell
        // parameter verfuegt. 
        if(vehInput.isWithMemory()){
        	memory = new MemoryImpl(vehInput.getMemoryInputData());
        }
        
        
        if(vehInput.isWithNoise()){
            noise = new NoiseImpl(vehInput.getNoiseInputData());
        }
        
    }

    public void init(double pos, double v, int lane){
        this.position = pos;
        this.oldPosition = pos;
        this.speed = v;
        this.lane = lane;
        this.targetLane = lane;
    }

    public double length() { return length; }
    public double position() { return position; }
    public double posFrontBumper() { return position+0.5*length; }
    public double posReadBumper() { return position-0.5*length; } 


    public double oldPosition() { return oldPosition; }
    
    public void setPosition(double position){ this.position = position; }

    public double speed() { return speed;  }
    public void setSpeed(double speed){ this.speed = speed; }
    
    
    public double speedlimit(){
    	return speedlimit;
    }
    
    // externally given speedlimit
    public void setSpeedlimit(double speedlimit){
    	this.speedlimit = speedlimit; 
    }
    
    
    public double acc(){ return acc; }
    public double accModel(){ return accModel; }

    public double distanceToTrafficlight(){ return distanceToTrafficlight; }
    
    public int id(){ return id; }

    public boolean isFromOnramp() { return (vehNumber < 0); }
    
    public int getVehNumber(){
    	return vehNumber;
    }

    public void setVehNumber(int vehNumber){
    	this.vehNumber = vehNumber;
    }

    public double netDistance(Vehicle vehFront){
        if(vehFront == null){
            return GAP_INFINITY;
        }
        return (vehFront.position() - position - 0.5*(length() + vehFront.length()));
    }
    
    public double relSpeed(Vehicle vehFront){
        if(vehFront == null){
            return 0;
        }
        return ( speed - vehFront.speed() );
    }

    
    
    public void calcAcceleration(double dt, VehicleContainer vehContainer, double alphaT, double alphaV0) {
        
        // new: acceleration noise:
        double accError = 0;
        if (noise != null) {
            noise.update(dt);
            accError = noise.getAccError();
            final Vehicle vehFront = vehContainer.getLeader(this);
            if (netDistance(vehFront) < 2.0){
                accError = Math.min(accError, 0.); // !!!
            }
            // logger.debug("accError = {}", accError);
        }

        
        // TODO extract to super class
        double alphaTLocal = alphaT;
        double alphaV0Local = alphaV0;
        double alphaALocal = 1;
        
        // TODO check concept here: kombination mit alphaV0: man sollte das Referenz-V0 nehmen
        // und NICHT das dynamische, durch Speedlimits beeinflusste v0
        if(memory!=null){
            final double v0 = longModel.parameterV0();
            memory.update(dt, speed, v0);
            alphaTLocal *= memory.alphaT();
            alphaV0Local *= memory.alphaV0();
            alphaALocal *= memory.alphaA();
        }
                
        // TODO gekapseltere Aufruf
        accModel = longModel.acc(this, vehContainer, alphaTLocal, alphaV0Local, alphaALocal); 

        
        //consider red or amber/yellow traffic light:
        if(considerTrafficLight){
            acc = Math.min(accModel, accTrafficLight);
            //logger.debug("accModel = {}, accTrafficLight = {}", accModel, accTrafficLight );
        }
        else{
            acc = accModel;
        }
        
        acc = Math.max(acc+accError, -maxDecel); // limited to maximum deceleration 
        // logger.debug("acc = {}", acc );
    }
    
    
    public void updatePostionAndSpeed(double dt) {

        // logger.debug("dt = {}", dt);
        // increment first s; then increment s with NEW v (2th order: -0.5 a dt^2)

        oldPosition = position;

        if( longModel.isCA() ){
            speed = (int)(speed + dt*acc + 0.5);
            position = (int)(position + dt*speed + 0.5);
        }
        else{
            // continuous micro models and iterated maps
            if(speed < 0) speed = 0;
            double advance = (acc*dt >= -speed) ? speed*dt + 0.5*acc*dt*dt : -0.5*speed*speed/acc;
            position += advance;
            speed += dt*acc;
            if(speed < 0){ 
                speed = 0; 
                acc=0;
            }
        }
    }

    public void updateTrafficLight(double time, TrafficLight trafficLight){
        accTrafficLight=0;
        considerTrafficLight=false;

        distanceToTrafficlight = trafficLight.position() - position - 0.5*length; 

        if( distanceToTrafficlight < 0 ){
            distanceToTrafficlight = GAP_INFINITY; // not relevant 
        }
        else if(!trafficLight.isGreen()){
            final double maxDistanceToReact = 1000;  //TODO Parameter ... ?!
            if( distanceToTrafficlight < maxDistanceToReact ){ 
                accTrafficLight= Math.min(0, longModel.accSimple(distanceToTrafficlight, speed, speed));
                
                if(accTrafficLight<0){
                    considerTrafficLight = true;
                    // logger.debug("distance to trafficLight = {}, accTL = {}", distanceToTrafficlight, accTrafficLight);
                }

                //TODO: decision logic while approaching yellow traffic light ...
                // ignoriere TL falls bei Gelb die (zweifache) komfortable Bremsverzoegerung ueberschritten wird
                // ODER wenn ich kinematisch nicht mehr bremsen koennte!!!
                double bKinMax = 6; // unterhalb von bMax !!!
                double comfortBrakeDecel = 4;
                double brakeDist = (speed*speed)/(2*bKinMax);
                if(trafficLight.isGreenRed() && (accTrafficLight <= -comfortBrakeDecel || brakeDist >= Math.abs(trafficLight.position()-position) ) ){
                    //ignore traffic light
                    considerTrafficLight=false; 
                }
                //System.out.printf("considerTrafficLight=%s, dx=%.2f, accTrafficLight=%.2f  %n", considerTrafficLight, trafficLight.position()-position, accTrafficLight, brakeDist );
            }
        }
    }
    
    
    public double getLane(){
        return lane;
    }
    
    public int getIntLane(){
        return targetLane;
    }
    
    public boolean hasReactionTime(){
        return ( reactionTime+Constants.SMALL_VALUE > 0);
    }


    public double getDesiredSpeedParameter(){
        return longModel.parameterV0();
        
    }
    


}
