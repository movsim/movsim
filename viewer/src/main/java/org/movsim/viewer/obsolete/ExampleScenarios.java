/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 *                                   <movsim.org@gmail.com>
 * -----------------------------------------------------------------------------------------
 * 
 * This file is part of
 * 
 * MovSim - the multi-model open-source vehicular-traffic simulator.
 * 
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MovSim. If not, see <http://www.gnu.org/licenses/>
 * or <http://www.movsim.org>.
 * 
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.viewer.obsolete;

import org.movsim.facades.MovsimViewerFacade;

/**
 * Collection of example traffic scenarios.
 * 
 */
public class ExampleScenarios {

    public static void addOnrampFile(MovsimViewerFacade movsimViewerFacade) {
        movsimViewerFacade.loadScenarioFromXml("onramp_IDM", "/sim/");        
        //movsimViewerFacade.loadScenarioFromXml("test_speedlimits", "/sim/", "x.xodr", "/roadnetwork/");        
//        final RoadNetwork roadNetwork = movsimViewerFacade.getSimulator().getRoadNetwork();
//        System.out.println("roadNetwork size: " + roadNetwork.size());
    }
    
    public static void addStartStopFile(MovsimViewerFacade movsimViewerFacade) {
        movsimViewerFacade.loadScenarioFromXml("startStop_IDM", "/sim/");        
//        final RoadNetwork roadNetwork = movsimViewerFacade.getSimulator().getRoadNetwork();
//        System.out.println("roadNetwork size: " + roadNetwork.size());
    }
}
