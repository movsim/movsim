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
package org.movsim.input.model.output;

import org.jdom.Element;


public class SpatioTemporalInput {

    /** The dt. */
    private double dt;

    /** The dx. */
    private double dx;

    /** The is initialized. */
    private final boolean isInitialized;

    /**
     * Instantiates a new macro input impl.
     * 
     * @param elem
     *            the elem
     */
    public SpatioTemporalInput(Element elem) {
        if (elem == null) {
            isInitialized = false;
            return;
        }
        this.dt = Double.parseDouble(elem.getAttributeValue("dt"));
        this.dx = Double.parseDouble(elem.getAttributeValue("dx"));
        isInitialized = true;

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.output.impl.MacroInput#getDt()
     */
    public double getDt() {
        return dt;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.output.impl.MacroInput#getDx()
     */
    public double getDx() {
        return dx;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.movsim.input.model.output.impl.MacroInput#isWithMacro()
     */
    public boolean isWithMacro() {
        return isInitialized;
    }

}
