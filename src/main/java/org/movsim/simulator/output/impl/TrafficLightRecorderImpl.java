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
import java.util.List;

import org.movsim.input.model.output.TrafficLightRecorderInput;
import org.movsim.simulator.Constants;
import org.movsim.simulator.output.TrafficLightRecorder;
import org.movsim.simulator.roadSection.TrafficLight;
import org.movsim.utilities.FileUtils;



public class TrafficLightRecorderImpl implements TrafficLightRecorder {

        private PrintWriter fstr = null;
        
        private int nDt;
        
        public TrafficLightRecorderImpl(String projectName, boolean writeOutput, TrafficLightRecorderInput input, List<TrafficLight> trafficLights){

            nDt = input.getnDt();

            if(writeOutput){
                final String filename = projectName + ".trafficlights_log";
                fstr = FileUtils.getWriter(filename);
                writeHeader(trafficLights);
            }
            
        }

        
        
        /* (non-Javadoc)
         * @see org.movsim.simulator.output.impl.TrafficLightRecorder#update(int, double, java.util.List)
         */
        public void update(int itime, double time, List<TrafficLight> trafficLights){
            
            if(itime % nDt != 0){
                //no update; nothing to do
                return;
            }
            
            // write data:
            if(fstr != null){
                fstr.printf("%8.2f   ", time);
                for(TrafficLight trafficLight : trafficLights){
                    fstr.printf("%d  %.1f  ", trafficLight.status(), trafficLight.position());
                }
                fstr.printf("%n");
                fstr.flush();
            }
        } 
        
        
        private void writeHeader(List<TrafficLight> trafficLights){
            //write header:
            fstr.printf(Constants.COMMENT_CHAR + " number codes for traffic lights status: %n");
            fstr.printf(Constants.COMMENT_CHAR + " green         %d %n", TrafficLight.GREEN_LIGHT);
            fstr.printf(Constants.COMMENT_CHAR + " green --> red %d %n", TrafficLight.GREEN_RED_LIGHT);
            fstr.printf(Constants.COMMENT_CHAR + " red           %d %n", TrafficLight.RED_LIGHT);
            fstr.printf(Constants.COMMENT_CHAR + " red --> green %d %n", TrafficLight.RED_GREEN_LIGHT);
            
            int counter=1;
            for(TrafficLight trafficLight : trafficLights){
                fstr.printf(Constants.COMMENT_CHAR + " position of traffic light no. %d: %5.2f m%n", counter, trafficLight.position());
                counter++;
            }
            fstr.printf(Constants.COMMENT_CHAR + " %-8s  %-8s  %-8s  %-8s %n", "time[s]", "status[1]_TL1", "position[m]_TL1", " etc. ");
            fstr.flush();
        }

}


