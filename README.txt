=========================================================================
MovSim

MovSim = multi-model open-source vehicular-traffic simulator

http://www.movsim.org

Demonstration: www.verkehrsdynamik.de 

=========================================================================

Description:

microscopic traffic simulator with xml-based configuration and csv text output 

features:

- one-lane simulator of one mainroad
- onramp and flow-conserving bottlenecks
- traffic-lights
- multiple models and different model classes (continuous models like Intelligent Driver Model, 
  coupled-map models like Gipps and cellular automata like Nagel-Schreckenberg)
- text-file output of detectors, spatiotemporal fields, floating-car data etc. 
 

-------------------------------------------------------------------------

Usage:

build with maven from command line: 
mvn install

run predefined simulation scenarios in sim/ (*.xml) and 
plot simulation results with gnuplot (/sim/*.gpl) with runapp-script:

./runapp startStop_IDM

etc.

command-line options:

java -jar target/movsim-1.0-jar-with-dependencies.jar -h
 


  

-------------------------------------------------------------------------

For commercial use, please contact the copyright holders at


-------------------------------------------------------------------------
/**
 * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
 *                             Ralph Germ, Martin Budden
 *
 *                             <info@movsim.org>
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