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
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.RoadInput;
import org.movsim.input.model.SimulationInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimulationInputImpl.
 */
public class SimulationInputImpl implements SimulationInput {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulationInputImpl.class);

    /** The timestep. */
    private final double timestep;

    /** The duration of the simulation. */
    private double maxSimTime;

    /** The with fixed seed. */
    private boolean withFixedSeed;

    private boolean withCrashExit;

    /** The random seed. */
    private final int randomSeed;

    /** The road input. */
    ArrayList<RoadInput> roadInput;

    /** The output input. */
    private OutputInput outputInput;

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
        if (elem.getAttributeValue("fixed_seed").equalsIgnoreCase("true")) {
            withFixedSeed = true;
        } else {
            withFixedSeed = false;
        }
        if (elem.getAttributeValue("crash_exit").equalsIgnoreCase("true")) {
            withCrashExit = true;
        } else {
            withCrashExit = false;
        }

        final List<Element> roadElems = elem.getChildren(XmlElementNames.Road);
        roadInput = new ArrayList<RoadInput>();
        for (final Element roadElem : roadElems) {
            roadInput.add(new RoadInputImpl(roadElem));
        }

        // -------------------------------------------------------
        // Output
        outputInput = new OutputInputImpl(elem.getChild(XmlElementNames.RoadOutput));

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
    public double getMaxSimTime() {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.SimulationInput#getSingleRoadInput()
     */
    @Override
    public RoadInput getSingleRoadInput() {
        // Quick hack: assume only one single main road !!!
        return roadInput.get(0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.SimulationInput#getOutputInput()
     */
    @Override
    public OutputInput getOutputInput() {
        return outputInput;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.SimulationInput#isWithCrashExit()
     */
    @Override
    public boolean isWithCrashExit() {
        return withCrashExit;
    }
}
