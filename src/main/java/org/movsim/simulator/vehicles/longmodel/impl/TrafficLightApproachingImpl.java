package org.movsim.simulator.vehicles.longmodel.impl;

import org.movsim.simulator.Constants;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.longmodel.TrafficLightApproaching;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel;

public class TrafficLightApproachingImpl implements TrafficLightApproaching {

	private boolean considerTrafficLight;
	
	private double accTrafficLight;

	
	public TrafficLightApproachingImpl(){
		considerTrafficLight = false;
	}
	

	
	/* (non-Javadoc)
	 * @see org.movsim.simulator.vehicles.longmodel.impl.TrafficLightApproaching#considerTrafficLight()
	 */
	public boolean considerTrafficLight(){
		return considerTrafficLight;
	}
	
	
	public double accApproaching(){
		return accTrafficLight;
	}
	
    /* (non-Javadoc)
	 * @see org.movsim.simulator.vehicles.longmodel.impl.TrafficLightApproaching#updateTrafficLight(org.movsim.simulator.vehicles.Vehicle, double, org.movsim.simulator.roadSection.TrafficLight, org.movsim.simulator.vehicles.longmodel.accelerationmodels.AccelerationModel)
	 */
    public void update(Vehicle me, double time, TrafficLight trafficLight, AccelerationModel longModel) {
        accTrafficLight = 0;
        considerTrafficLight = false;

        double distanceToTrafficlight = trafficLight.position() - me.position() - 0.5 * me.length();

        if (distanceToTrafficlight < 0) {
            distanceToTrafficlight = Constants.GAP_INFINITY; // not relevant
        } else if (!trafficLight.isGreen()) {
            final double maxDistanceToReact = 1000; // TODO Parameter ... ?!
            if (distanceToTrafficlight < maxDistanceToReact) {
            	final double speed = me.speed();
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
                                - me.position()))) {
                    // ignore traffic light
                    considerTrafficLight = false;
                }
                // System.out.printf("considerTrafficLight=%s, dx=%.2f, accTrafficLight=%.2f  %n",
                // considerTrafficLight, trafficLight.position()-position,
                // accTrafficLight, brakeDist );
            }
        }
    }

}
