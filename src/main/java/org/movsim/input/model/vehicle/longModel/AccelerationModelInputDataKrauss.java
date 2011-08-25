/**
 * 
 * Copyright (C) 2010 by Ralph Germ (http://www.ralphgerm.de)
 * 
 */
package org.movsim.input.model.vehicle.longModel;

/**
 * @author ralph
 *
 */
public interface AccelerationModelInputDataKrauss extends AccelerationModelInputData, AccelerationModelInputDataGipps {

    double getEpsilon();

    /**
     * @param bChangeValue
     */
    void setEpsilon(double bChangeValue);
}
