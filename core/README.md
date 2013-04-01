MovSim Core
===========

MovSim = **M**ulti-model **o**pen-source **v**ehicular-traffic **Sim**ulator.

http://www.movsim.org

Demonstration: www.verkehrsdynamik.de 


Description
-----------

MovSim is a microscopic traffic simulator with xml-based configuration and csv text output. 

The `core` submodule provides the following features:

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

To build the MovSim core with Maven, type `mvn install` from the MovSim `core/` directory.

MovSim core can be run directly from the command-line. To see the MovSim options, invoke the target by typing e.g.:

    java -jar target/MovsimCore-1.5.0-SNAPSHOT-jar-with-dependencies.jar -h

There are a number of predefined simulation scenarios defined in the [_sim_ directory](https://github.com/movsim/movsim/tree/master/sim). The `runmovsim` script can be used to run the simulator and gnuplot for plot these scenarios, for example:

    cd ../sim/bookScenarioStartStop/
    ../../core/runmovsim -f startStop_IDM.xml
    gnuplot startStop_IDM.gpl

The `.csv` output is put in the directory from which the simulator is called and the graphical output is put in `.eps` (Encapsulated PostScript) files.


Logging output
--------------

MovSim's logging output is controlled by the `/config/log4j.properties` properties file.

Logging levels are: `DEBUG < INFO < WARN < ERROR`


Commercial use
--------------

For commercial use, please contact the copyright holders at movsim@akesting.de


Copyright
---------

MovSim is Copyright (C) 2010, 2011, 2012, 2013 by Arne Kesting, Martin Treiber, Ralph Germ, and Martin Budden.

MovSim is licensed under [GPL version 3](https://github.com/movsim/movsim/blob/master/COPYING).

