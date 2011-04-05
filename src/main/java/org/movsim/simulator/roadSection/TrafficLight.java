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
package org.movsim.simulator.roadSection;



public interface TrafficLight {
    
    //cycle is GREEN --> GREEN_RED --> RED --> RED_GREEN --> GREEN
    final int GREEN_LIGHT=0;
    final int GREEN_RED_LIGHT=1;
    final int RED_LIGHT=2;
    final int RED_GREEN_LIGHT=3;
    
    double position();

    boolean isGreen();
    boolean isGreenRed();
    boolean isRed();
    boolean isRedGreen();
    
    int status();
    
    // relativ, fuer Fahrzeug
    double getTimeForNextGreen(double alpha);
    double getTimeForNextRed(double alpha);
    
    // absolut, fuer plots
    double getTimeForNextGreen();
    double getTimeForNextRed();
    double getCurrentCycleTime();
    double getCycleTime();
    
    double getCritTimeForNextMainPhase(double alpha);
    
    void update(double time);
   
}
