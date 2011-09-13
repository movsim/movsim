/*
 * Copyright by Ralph Germ (http://www.ralphgerm.de)
 */
package org.movsim.simulator.vehicles;

// TODO: Auto-generated Javadoc
/**
 * The Interface Moveable.
 */
public interface Moveable {

    /**
     * Gets the label.
     * 
     * @return the label
     */
    String getLabel();

    /**
     * Length.
     * 
     * @return the double
     */
    double getLength();

    /**
     * Gets the position of the floating car.
     * 
     * @return the double
     */
    double getPosition();

    /**
     * Pos front bumper.
     * 
     * @return the double
     */
    double posFrontBumper();

    /**
     * Pos read bumper.
     * 
     * @return the double
     */
    double posReadBumper();

    /**
     * Old position.
     * 
     * @return the double
     */
    double getOldPosition();

    /**
     * Checks for reaction time.
     * 
     * @return true, if successful
     */
    boolean hasReactionTime();

    /**
     * Gets the desired speed parameter.
     * 
     * @return the desired speed parameter
     */
    double getDesiredSpeedParameter();

    /**
     * GetS the speed of the floating car.
     * 
     * @return the double
     */
    double getSpeed();

    /**
     * Gets The Accelation of the floating car.
     * 
     * @return the double
     */
    double getAcc();

    /**
     * Acc model.
     * 
     * @return the double
     */
    double accModel();

    /**
     * Speedlimit.
     * 
     * @return the double
     */
    double getSpeedlimit();

    /**
     * Id.
     * 
     * @return the int
     */
    int getId();

    /**
     * Gets the veh number.
     * 
     * @return the veh number
     */
    int getVehNumber();

    /**
     * Checks if is from onramp.
     * 
     * @return true, if is from onramp
     */
    boolean isFromOnramp();

    /**
     * Gets the lane.
     * 
     * @return the lane
     */
    int getLane();

    /**
     * Net distance.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double getNetDistance(Moveable vehFront);

    /**
     * Rel speed.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double getRelSpeed(Moveable vehFront);

    /**
     * Distance to trafficlight.
     * 
     * @return the double
     */
    double getDistanceToTrafficlight();

    boolean isBrakeLightOn();
}
