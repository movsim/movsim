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
package org.movsim.simulator.vehicles;

import org.movsim.simulator.roadSection.TrafficLight;

public interface Vehicle {
    
    //final double vMax = 200/3.6;
    final double GAP_INFINITY = 10000;
    
    double length();
    
    double position();
    double posFrontBumper();
    double posReadBumper();
    double oldPosition();
    
    boolean hasReactionTime();
    double getDesiredSpeedParameter();
    
    double speed();
    double acc();
    double accModel();
    
    double speedlimit();
    void setSpeedlimit(double speedlimit);
    
    int id();

    int getVehNumber();
    
    void setVehNumber(int vehNumber);
    
    boolean isFromOnramp();
    
    double getLane();
    int getIntLane();
    
    
    void init(double pos, double v, int lane);
    
    double netDistance(Vehicle vehFront);
    
    double relSpeed(Vehicle vehFront);
    
    double distanceToTrafficlight();
    
    void updatePostionAndSpeed(double dt);
    
    void calcAcceleration(double dt, VehicleContainer vehContainer, double alphaT, double alphaV0);
    
    void updateTrafficLight(double time, TrafficLight trafficLight);
    
    
}
