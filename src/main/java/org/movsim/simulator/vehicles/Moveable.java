package org.movsim.simulator.vehicles;

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
    double length();

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
    double oldPosition();

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
    double speedlimit();

    

    /**
     * Id.
     * 
     * @return the int
     */
    int id();

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

    int getLane();

   

    

    /**
     * Net distance.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double netDistance(Moveable vehFront);

    /**
     * Rel speed.
     * 
     * @param vehFront
     *            the veh front
     * @return the double
     */
    double relSpeed(Moveable vehFront);

    /**
     * Distance to trafficlight.
     * 
     * @return the double
     */
    double distanceToTrafficlight();

}
