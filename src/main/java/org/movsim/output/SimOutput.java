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

import java.util.List;

import org.movsim.input.InputData;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.SpatioTemporalInput;
import org.movsim.input.model.output.TrajectoriesInput;
import org.movsim.output.fileoutput.FileFloatingCars;
import org.movsim.output.fileoutput.FileSpatioTemporal;
import org.movsim.output.fileoutput.FileTrajectories;
import org.movsim.output.impl.FloatingCarsImpl;
import org.movsim.output.impl.SpatioTemporalImpl;
import org.movsim.simulator.roadSection.RoadSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class SimOutput.
 */
public class SimOutput implements SimObservables {
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimOutput.class);

    private SpatioTemporalImpl spatioTemporal = null;
    
    /** The file spatio temporal. */
    private FileSpatioTemporal fileSpatioTemporal;

    /** The floating cars. */
    private FloatingCarsImpl floatingCars = null;
    
    private FileFloatingCars fileFloatingCars;
    
    /** The trajectories. */
    private FileTrajectories trajectories = null;

    /** The write output. */
    private final boolean writeOutput;

    /** The project name. */
    private final String projectName;

    
    private final RoadSection roadSection;

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
    public SimOutput(boolean instantaneousFileOutput, InputData simInput, RoadSection roadSection) {
        projectName = simInput.getProjectName();
        this.roadSection = roadSection;
        
        // more restrictive than in other output classes TODO
        writeOutput = instantaneousFileOutput; // no file output from GUI

        logger.info("Cstr. SimOutput. projectName= {}", projectName);
        
        // SingleRoad quickhack! TODO
        final OutputInput outputInput = simInput.getSimulationInput().getOutputInput();
        final FloatingCarInput floatingCarInput = outputInput.getFloatingCarInput();
        if (floatingCarInput.isWithFCD()) {
            floatingCars = new FloatingCarsImpl(roadSection.vehContainer(), floatingCarInput);
            if(writeOutput){
                fileFloatingCars = new FileFloatingCars(projectName, floatingCars);
            }
        }

        final SpatioTemporalInput spatioTemporalInput = outputInput.getSpatioTemporalInput();
        if (spatioTemporalInput.isWithMacro()) {
            spatioTemporal = new SpatioTemporalImpl(spatioTemporalInput, roadSection);
            if(writeOutput){
                fileSpatioTemporal = new FileSpatioTemporal(projectName, roadSection.id(), spatioTemporal);
            }
        }
        
        final TrajectoriesInput trajInput = outputInput.getTrajectoriesInput();
        if (trajInput.isInitialized()) {
            trajectories = new FileTrajectories(projectName, trajInput, roadSection);
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
    public void update(int itime, double time, double timestep) {
        
        if (floatingCars != null) {
            floatingCars.update(itime, time, timestep);
        }
        if (spatioTemporal != null) {
            spatioTemporal.update(itime, time, roadSection);
        }
        
        if (trajectories != null) {
            trajectories.update(itime, time);
        }
        
    }
    
    public SpatioTemporal getSpatioTemporal(){
        return spatioTemporal;
    }
    
    public FloatingCars getFloatingCars(){
        return floatingCars;
    }

    public List<LoopDetector> getLoopDetectors(){
        return roadSection.getLoopDetectors();
    }
    
    
}
