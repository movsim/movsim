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
package org.movsim.input.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;
import org.movsim.input.XmlElementNames;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: extract element names into XmlElementNames Interface to make them symbolic.
/**
 * The Class SimulationInputImpl.
 */
public class SimulationInputImpl implements SimulationInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulationInputImpl.class);

    /** The timestep. */
    private final double timestep;

    /** The duration of the simulation. */
    private final double maxSimTime;

    /** The with fixed seed. */
    private boolean withFixedSeed;
    
    /** The random seed. */
    private final int randomSeed;

    /** The road input. */
    ArrayList<RoadInput> roadInput;

    /**
     * Instantiates a new simulation input impl.
     * 
     * @param elem
     *            the elem
     */
    public SimulationInputImpl(Element elem) {
        timestep = Double.parseDouble(elem.getAttributeValue("dt"));
        maxSimTime = Double.parseDouble(elem.getAttributeValue("duration"));
        randomSeed = Integer.parseInt(elem.getAttributeValue("seed"));
        if (elem.getAttributeValue("with_fixed_seed").equalsIgnoreCase("true")) {
            withFixedSeed = true;
        } else {
            withFixedSeed = false;
        }

        final List<Element> roadElems = elem.getChildren(XmlElementNames.Road);
        roadInput = new ArrayList<RoadInput>();
        for (final Element roadElem : roadElems) {
            roadInput.add(new RoadInputImpl(roadElem));
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getTimestep()
     */
    @Override
    public double getTimestep() {
        return timestep;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getMaxSimulationTime()
     */
    @Override
    public double getMaxSimulationTime() {
        return maxSimTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#isWithFixedSeed()
     */
    @Override
    public boolean isWithFixedSeed() {
        return withFixedSeed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.impl.SimulationInput#getRandomSeed()
     */
    @Override
    public int getRandomSeed() {
        return randomSeed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.SimulationInput#getRoadInput()
     */
    @Override
    public ArrayList<RoadInput> getRoadInput() {
        return roadInput;
    }

    // Quick hack: assume only one single main road !!!

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.SimulationInput#getSingleRoadInput()
     */
    @Override
    public RoadInput getSingleRoadInput() {
        return roadInput.get(0);
    }

}
