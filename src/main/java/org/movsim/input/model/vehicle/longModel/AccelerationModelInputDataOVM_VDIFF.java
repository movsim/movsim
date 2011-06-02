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
package org.movsim.input.model.vehicle.longModel;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccelerationModelInputDataOVM_VDIFF.
 */
public interface AccelerationModelInputDataOVM_VDIFF extends AccelerationModelInputData{

    /**
     * Gets the s0.
     * 
     * @return the s0
     */
    double getS0();

    /**
     * Gets the v0.
     * 
     * @return the v0
     */
    double getV0();

    /**
     * Gets the tau.
     * 
     * @return the tau
     */
    double getTau();

    /**
     * Gets the len interaction.
     * 
     * @return the len interaction
     */
    double getLenInteraction();

    /**
     * Gets the beta.
     * 
     * @return the beta
     */
    double getBeta();

    /**
     * Gets the lambda.
     * 
     * @return the lambda
     */
    double getLambda();

    /**
     * Gets the variant.
     * 
     * @return the variant
     */
    int getVariant();
    

    double getV0Default();

    double getTauDefault();

    double getLenInteractionDefault();

    double getBetaDefault();

    double getLambdaDefault();

    double getS0Default();

    int getVariantDefault();

    void setV0(double v0);

    void setTau(double tau);


    void setLenInteraction(double lenInteraction);

    void setBeta(double beta);

    void setLambda(double lambda);

    void setS0(double s0);

    void setVariant(int variant);

}