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
package org.movsim.simulator.vehicles.longmodels.impl;

import org.movsim.simulator.vehicles.longmodels.LongModelCategory;

public abstract class LongitudinalModelImpl {

	
    private final String modelName;

    private final int modelCategory;
    
    public LongitudinalModelImpl(String modelName, int modelCategory) {
        this.modelName = modelName;
        this.modelCategory = modelCategory;
    }

    public String modelName() {
        return modelName;
    }

    public boolean isCA() {
        return (modelCategory == LongModelCategory.CELLULAR_AUTOMATON);
    }
    
    public boolean isIteratedMap() {
        return (modelCategory == LongModelCategory.INTERATED_MAP_MODEL);
    }
    
    
    public int getModelCategory(){
    	return modelCategory;
    }
    // TODO: fuer alle Modelle ?! analog: T !?
    public abstract double parameterV0();

    public abstract double getRequiredUpdateTime();
    
}
