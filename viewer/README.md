MovSim
======

MovSim = **M**ulti-model **o**pen-source **v**ehicular-traffic **Sim**ulator.

http://www.movsim.org

Demonstration: www.verkehrsdynamik.de 


Description
-----------

MovSim is a microscopic traffic simulator with xml-based configuration and csv text output. 

Features:

- multi-lane simulator including onramps, offrams, "flow-conserving bottlenecks" and traffic-lights
- multiple models of different model classes (car-following models, coupled-map models and cellular automata)
  * Intelligent Driver Model (IDM) [Wikipedia](http://en.wikipedia.org/wiki/Intelligent_driver_model)
  * Enhanced IDM/Adaptive Cruise Control Model [Preprint] (http://arxiv.org/abs/0912.3613)
  * Optimal Velocity or Bando Model 
  * Velocity Difference Model 
  * Gipps Model [Wikipedia] (http://en.wikipedia.org/wiki/Gipps%27_Model)
  * Krauss Model
  * Nagel-Schreckenberg Cellular Automaton [Wikipedia] (http://en.wikipedia.org/wiki/Nagel-Schreckenberg_model)
  * Kerner-Klenov-Wolf Cellular Automaton
- text-file output of detectors, spatiotemporal fields, floating-car data etc. 


Installation
------------

For installation see the [README.md](https://github.com/movsim/movsim/blob/master/README.md) in the main MovSim directory.


Usage
-----

To build the MovSim viewer, type `mvn install` from the MovSim viewer directory.

The MovSim viewer can be run directly from the command-line. To see the MovSim options, type:

    java -jar viewer/target/MovsimViewer-1.3.1-SNAPSHOT-jar-with-dependencies.jar -h


Logging output
--------------

MovSim's logging output is controlled by a properties file `/config/log4j.properties` for the java app and `/config/log4japplet.properties` for the java applet.

Logging levels are: `DEBUG < INFO < WARN < ERROR`


Commercial use
--------------

For commercial use, please contact the copyright holders at movsim.org@gmail.com


Copyright
---------

MovSim is Copyright (C) 2010, 2011, 2012, 2013 by Arne Kesting, Martin Treiber, Ralph Germ, and Martin Budden.

MovSim is licensed under [GPL version 3](https://github.com/movsim/movsim/blob/master/COPYING).

