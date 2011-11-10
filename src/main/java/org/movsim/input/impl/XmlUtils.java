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
package org.movsim.input.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * The Class XmlUtils.
 */
public class XmlUtils {

    /**
     * private constructer.
     */
    private XmlUtils() {

    }

    /**
     * Put all attributes in hash with corresponding values.
     * 
     * @param element
     *            the element
     * @return the map
     */
    public static Map<String, String> putAttributesInHash(Element element) {
        final Map<String, String> map = new HashMap<String, String>();
        final List<Attribute> allAttr = element.getAttributes();
        for (final Attribute currentAttr : allAttr) {
            map.put(currentAttr.getName(), currentAttr.getValue());
        }
        return map;
    }
}
