/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *                             <info@movsim.org>
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
package org.movsim.simulator.vehicles.longmodel.impl;

import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longmodel.TrafficLightApproaching;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightApproachingImpl.
 */
public class TrafficLightApproachingImpl implements TrafficLightApproaching {

	/** The consider traffic light. */
	private boolean considerTrafficLight;
	
	/** The acc traffic light. */
	private double accTrafficLight;
	
	private double distanceToTrafficlight;

	
	/**
	 * Instantiates a new traffic light approaching impl.
	 */
	public TrafficLightApproachingImpl(){
		considerTrafficLight = false;
	}
	

    /* (non-Javadoc)
	 * @see org.movsim.simulator.vehicles.longmodel.impl.TrafficLightApproaching#updateTrafficLight(org.movsim.simulator.vehicles.Vehicle, double, org.movsim.simulator.roadSection.TrafficLight, org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel)
	 */
    public void update(Vehicle me, double time, TrafficLight trafficLight, AccelerationModel longModel) {
        accTrafficLight = 0;
        considerTrafficLight = false;

        distanceToTrafficlight = trafficLight.position() - me.getPosition() - 0.5 * me.length();

        if (distanceToTrafficlight < 0) {
            distanceToTrafficlight = Constants.GAP_INFINITY; // not relevant
        } else if (!trafficLight.isGreen()) {
            final double maxDistanceToReact = 1000; // TODO Parameter ... ?!
            if (distanceToTrafficlight < maxDistanceToReact) {
            	final double speed = me.getSpeed();
                accTrafficLight = Math.min(0, longModel.accSimple(distanceToTrafficlight, speed, speed));

                if (accTrafficLight < 0) {
                    considerTrafficLight = true;
                    // logger.debug("distance to trafficLight = {}, accTL = {}",
                    // distanceToTrafficlight, accTrafficLight);
                }

                // TODO: decision logic while approaching yellow traffic light
                // ...
                // ignoriere TL falls bei Gelb die (zweifache) komfortable
                // Bremsverzoegerung ueberschritten wird
                // ODER wenn ich kinematisch nicht mehr bremsen koennte!!!
                final double bKinMax = 6; // unterhalb von bMax !!!
                final double comfortBrakeDecel = 4;
                final double brakeDist = (speed * speed) / (2 * bKinMax);
                if (trafficLight.isGreenRed()
                        && (accTrafficLight <= -comfortBrakeDecel || brakeDist >= Math.abs(trafficLight.position()
                                - me.getPosition()))) {
                    // ignore traffic light
                    considerTrafficLight = false;
                }
                // System.out.printf("considerTrafficLight=%s, dx=%.2f, accTrafficLight=%.2f  %n",
                // considerTrafficLight, trafficLight.position()-position,
                // accTrafficLight, brakeDist );
            }
        }
    }


    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.longmodel.impl.TrafficLightApproaching#considerTrafficLight()
     */
    public boolean considerTrafficLight(){
        return considerTrafficLight;
    }
    
    
    /* (non-Javadoc)
     * @see org.movsim.simulator.vehicles.longmodel.TrafficLightApproaching#accApproaching()
     */
    public double accApproaching(){
        return accTrafficLight;
    }
    
    

    public double getDistanceToTrafficlight() {
        return distanceToTrafficlight;
    }
    


}
