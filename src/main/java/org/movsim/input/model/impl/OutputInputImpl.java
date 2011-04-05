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
package org.movsim.input.model.impl;

import org.jdom.Element;
import org.movsim.input.model.OutputInput;
import org.movsim.input.model.output.DetectorInput;
import org.movsim.input.model.output.FloatingCarInput;
import org.movsim.input.model.output.MacroInput;
import org.movsim.input.model.output.TrafficLightRecorderInput;
import org.movsim.input.model.output.impl.DetectorInputImpl;
import org.movsim.input.model.output.impl.FloatingCarInputImpl;
import org.movsim.input.model.output.impl.MacroInputImpl;
import org.movsim.input.model.output.impl.TrafficLightRecorderInputImpl;


public class OutputInputImpl implements OutputInput {

    private FloatingCarInput floatingCarInput;
    private MacroInput macroInput;
    private DetectorInput detectorInput;
    private TrafficLightRecorderInput trafficLightRecorderInput;
    
    public OutputInputImpl(Element elem){
        parseElement(elem);
    }

    
    private void parseElement(Element elem) {

        floatingCarInput = new FloatingCarInputImpl(elem.getChild("FLOATING_CAR_DATA"));
        
        macroInput = new MacroInputImpl(elem.getChild("MACRO"));
        
        detectorInput = new DetectorInputImpl(elem.getChild("DETECTORS"));
        
        trafficLightRecorderInput = new TrafficLightRecorderInputImpl( elem.getChild("TRAFFICLIGHT_RECORDER") );
        
        
    }


    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.OutputInput#getFloatingCarInput()
     */
    public FloatingCarInput getFloatingCarInput() {
        return floatingCarInput;
    }


    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.OutputInput#getMacroInput()
     */
    public MacroInput getMacroInput() {
        return macroInput;
    }


    /* (non-Javadoc)
     * @see org.movsim.input.model.impl.OutputInput#getDetectorInput()
     */
    public DetectorInput getDetectorInput() {
        return detectorInput;
    }
    
    public TrafficLightRecorderInput getTrafficLightRecorderInput() {
        return trafficLightRecorderInput;
    }
    

    
}
