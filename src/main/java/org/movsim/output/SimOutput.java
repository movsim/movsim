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
package org.movsim.output;

import org.movsim.input.InputData;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.output.DetectorInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.MacroInput;
import org.movsim.input.model.output.TrafficLightRecorderInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.output.impl.FloatingCarsImpl;
import org.movsim.output.impl.LoopDetectors;
import org.movsim.output.impl.Macro3DImpl;
import org.movsim.output.impl.TrafficLightRecorderImpl;
import org.movsim.output.impl.TrajectoriesImpl;
import org.movsim.simulator.roadSection.RoadSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimOutput.
 */
public class SimOutput {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimOutput.class);

    /** The macro3 d. */
    private Macro3D macro3D = null;
    
    /** The floating cars. */
    private FloatingCars floatingCars = null;
    
    /** The detectors. */
    private LoopDetectors detectors = null;
    
    /** The trajectories. */
    private Trajectories trajectories = null;

    /** The traffic light recorder. */
    private TrafficLightRecorder trafficLightRecorder = null;

    /** The write output. */
    private final boolean writeOutput;

    /** The project name. */
    private final String projectName;

    // TODO: propagate output path information into output modules ...

    /**
     * Instantiates a new sim output.
     * 
     * @param isWithGUI
     *            the is with gui
     * @param simInput
     *            the sim input
     * @param roadSection
     *            the road section
     */
    public SimOutput(boolean isWithGUI, InputData simInput, RoadSection roadSection) {
        projectName = simInput.getProjectName();

        writeOutput = !isWithGUI; // no file output from GUI

        logger.info("Cstr. SimOutput. projectName= {}", projectName);

        final OutputInput outputInput = simInput.getOutputInput();
        final FloatingCarInput floatingCarInput = outputInput.getFloatingCarInput();
        if (floatingCarInput.isWithFCD()) {
            floatingCars = new FloatingCarsImpl(projectName, writeOutput, floatingCarInput);
        }

        final MacroInput macroInput = outputInput.getMacroInput();
        if (macroInput.isWithMacro()) {
            macro3D = new Macro3DImpl(projectName, writeOutput, macroInput, roadSection);
        }
        
        final TrajectoriesInput trajInput = outputInput.getTrajectoriesInput();
        if (trajInput.isInitialized()) {
            trajectories = new TrajectoriesImpl(projectName, trajInput, roadSection);
        }

        final DetectorInput detInput = outputInput.getDetectorInput();
        if (detInput.isWithDetectors()) {
            detectors = new LoopDetectors(projectName, writeOutput, detInput);
        }

        final TrafficLightRecorderInput trafficLightRecInput = outputInput.getTrafficLightRecorderInput();
        if (trafficLightRecInput.isWithTrafficLightRecorder()) {
            trafficLightRecorder = new TrafficLightRecorderImpl(projectName, writeOutput, trafficLightRecInput,
                    roadSection.getTrafficLights());
        }

    }

    /**
     * Update.
     * 
     * @param itime
     *            the itime
     * @param time
     *            the time
     * @param timestep
     *            the timestep
     * @param roadSection
     *            the road section
     */
    public void update(int itime, double time, double timestep, RoadSection roadSection) {
        // System.out.println("SimOutput.update: time="+time);
        if (floatingCars != null) {
            floatingCars.update(itime, time, timestep, roadSection.vehContainer());
        }
        if (macro3D != null) {
            macro3D.update(itime, time, roadSection);
        }
        
        if (trajectories != null) {
            trajectories.update(itime, time, timestep);
        }
        
        if (detectors != null) {
            detectors.update(itime, time, timestep, roadSection.vehContainer());
        }
        if (trafficLightRecorder != null) {
            trafficLightRecorder.update(itime, time, roadSection.getTrafficLights());
        }
    }

    // testweise
    /**
     * Close.
     */
    public void close() {

        if (!writeOutput)
            return;

        logger.info("SimOutput: close all files ... ");
        if (floatingCars != null) {
            floatingCars.closeAllFiles();
        }
        if (detectors != null) {
            detectors.closeFiles();
        }
    }

}
