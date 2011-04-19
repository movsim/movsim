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
package org.movsim.input.model.output.impl;

import org.jdom.Element;
import org.movsim.input.model.output.TrafficLightRecorderInput;

// TODO: Auto-generated Javadoc
/**
 * The Class TrafficLightRecorderInputImpl.
 */
public class TrafficLightRecorderInputImpl implements TrafficLightRecorderInput {

    private int nDt;

    private boolean isInitialized;

    /**
     * Instantiates a new traffic light recorder input impl.
     * 
     * @param elem
     *            the elem
     */
    public TrafficLightRecorderInputImpl(Element elem) {
        if (elem == null) {
            isInitialized = false;
            return;
        }

        this.nDt = Integer.parseInt(elem.getAttributeValue("n_dt"));

        isInitialized = true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.movsim.input.model.output.impl.TrafficLightRecorderInput#getnDt()
     */
    @Override
    public int getnDt() {
        return nDt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.output.TrafficLightRecorderInput#
     * isWithTrafficLightRecorder()
     */
    @Override
    public boolean isWithTrafficLightRecorder() {
        return isInitialized;
    }

}
