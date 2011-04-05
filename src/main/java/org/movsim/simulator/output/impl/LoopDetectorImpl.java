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
package org.movsim.simulator.output.impl;

import java.io.PrintWriter;

import org.movsim.simulator.Constants;
import org.movsim.simulator.output.LoopDetector;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.simulator.vehicles.VehicleContainer;
import org.movsim.utilities.FileUtils;



public class LoopDetectorImpl implements LoopDetector{

    // final static Logger logger = LoggerFactory.getLogger(LoopDetectorImpl.class);
    
    private double dtSample;
    
    private double detPosition;
    
    private double timeOffset;

    private PrintWriter printWriter = null;
    

    // internal state variables
    private int vehCount;
    private double vSum;
    private double occTime;
    private double sumInvV;
    private double sumInvQ; 
    
    // aggregated variables
    private double meanSpeed;
    private double rhoArithmetic;
    private double flow;
    private double occupancy;
    private int vehCountOutput;
    private double meanHarmSpeed;
    private double meanHarmTimegap;

	private boolean writeOutput;
    
    
    
    public LoopDetectorImpl(String projectName, boolean writeOutput, double detPosition, double dtSample) {
    	this.writeOutput = writeOutput;
        this.detPosition = detPosition;
        this.dtSample = dtSample;
        
        timeOffset = 0;
        reset();
        
		if (writeOutput) {
			final int xDetectorInt = (int) detPosition;
			final String filename = projectName + ".x" + xDetectorInt + "_det";
			printWriter = initFile(filename);
			writeAggregatedData(0);
		}
    }
    
    private void reset(){
        vehCount=0;
        vSum=0;
        occTime=0;
        sumInvQ = 0;
        sumInvV = 0;
    }
        
    // call in every simulation update
    public void update(double time, VehicleContainer vehicleContainer) {
        
        // brute force search: 
        
        for(Vehicle veh : vehicleContainer.getVehicles()){
            if ((veh.oldPosition() < detPosition) && (veh.position() >= detPosition) ) {
                // new vehicle crossed detector
                vehCount++;
                final double speedVeh = veh.speed();
                vSum += speedVeh;
                occTime += veh.length() / speedVeh;
                sumInvV += (speedVeh > 0) ? 1./speedVeh : 0;
                // calculate brut timegap not from local detector data: 
                final Vehicle vehFront = vehicleContainer.getLeader(veh);
                final double brutTimegap = (vehFront==null) ? 0 : (vehFront.position()-veh.position())/vehFront.speed();
                sumInvQ += (brutTimegap > 0) ? 1./brutTimegap : 0; // microscopic flow 
            }
        }

        if ((time - timeOffset + Constants.SMALL_VALUE) >= dtSample) {
            calculateAverages();
            if(writeOutput){
            	writeAggregatedData(time);
            }
            timeOffset = time;
        }
    }

    // ############################################

    private void calculateAverages() {
        flow = vehCount/dtSample;
        meanSpeed = (vehCount==0) ? 0 : vSum/vehCount;
        rhoArithmetic = (vehCount==0) ? 0 : flow / meanSpeed;
        occupancy = occTime/dtSample;
        vehCountOutput = vehCount;
        meanHarmSpeed = (vehCount==0) ? 0 : 1./(sumInvV/vehCount);
        meanHarmTimegap = (vehCount==0) ? 0 : sumInvQ/vehCount;
        reset();
    }


    private PrintWriter initFile(String filename){
        printWriter = FileUtils.getWriter(filename);
        printWriter.printf(Constants.COMMENT_CHAR + " dtSample in s = %-8.4f %n", dtSample);
        printWriter.printf(Constants.COMMENT_CHAR + " position xDet = %-8.4f %n", detPosition);
        printWriter.printf(Constants.COMMENT_CHAR + " arithmetic average for density rho %n");
        printWriter.printf(Constants.COMMENT_CHAR + " %-9s  %-9s  %-9s  %-9s  %-9s   %-9s  %-9s %-9s%n", 
                "t[min]", "<v>[km/h]", "rho[1/km]", "Q[1/h]", "nVeh[1]", "Occup[1]", "1/<1/v>(km/h)", "<1/Tbrutto>(1/s)");
        printWriter.flush();
        return printWriter;
    }
    
    private void writeAggregatedData(double time) {
        if(printWriter==null) return;
        printWriter.printf("%8.1f  %8.3f  %8.3f  %8.1f  %5d  %8.7f  %8.3f  %8.5f%n", 
                time/60., 3.6*meanSpeed, 1000*rhoArithmetic, 3600*flow, vehCountOutput, occupancy, 3.6*meanHarmSpeed, meanHarmTimegap);
        printWriter.flush();
    }
    
    
    public void closeFiles(){
        if(printWriter!=null){
            printWriter.close();
        }
    }

    // public getters
    
    public double position() { return detPosition;  }


    public double flow() {
        return flow;
    }

    public double meanSpeed() {
        return meanSpeed;
    }

    public double occupancy() {
        return occupancy;
    }

    public double rhoArithmetic() {
        return rhoArithmetic;
    }


}
