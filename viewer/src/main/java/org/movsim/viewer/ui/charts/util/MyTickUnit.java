package org.movsim.viewer.ui.charts.util;
///**
// * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
// *                             Ralph Germ, Martin Budden
// *                             <movsim@akesting.de>
// * ----------------------------------------------------------------------
// * 
// *  This file is part of 
// *  
// *  MovSim - the multi-model open-source vehicular-traffic simulator 
// *
// *  MovSim is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  MovSim is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
// *  <http://www.movsim.org>.
// *  
// * ----------------------------------------------------------------------
// */
//package org.movsim.viewer.graphics.charts.util;
//
//import org.jfree.chart.axis.TickUnit;
//import org.jfree.chart.axis.ValueAxis;
//
///**
// * Base class representing a tick unit. This determines the spacing of the tick marks on an axis.
// * <P>
// * This class (and any subclasses) should be immutable, the reason being that ORDERED collections of tick units are maintained and if one
// * instance can be changed, it may destroy the order of the collection that it belongs to. In addition, if the implementations are
// * immutable, they can belong to multiple collections.
// * 
// * @see ValueAxis
// */
//public class MyTickUnit extends TickUnit {
//
//    /**
//     * @param size
//     */
//    public MyTickUnit(double size) {
//        super(size);
//        // TODO Auto-generated constructor stub
//    }
//
//    /**
//     * Converts the supplied value to a string.
//     * <P>
//     * Subclasses may implement special formatting by overriding this method.
//     * 
//     * @param value
//     *            the data value.
//     * 
//     * @return Value as string.
//     */
//    @Override
//    public String valueToString(double value) {
//        return String.valueOf(value / 10);
//    }
//
//}
