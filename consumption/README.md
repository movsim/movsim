MovSim Consumption/emission model
=================================

Load-based consumption/emission model used in MovSim.

http://www.movsim.org and http://www.traffic-flow-dynamics.org

Description
-----------


Installation
------------

For installation see the [README.md](https://github.com/movsim/movsim/blob/master/README.md) in the main MovSim directory.


Usage
-----

To build the MovSim core, type `mvn install` from the MovSim core directory.

MovSim core can be run directly from the command-line. To see the MovSim options, type:

    java -jar target/movsim-1.2-jar-with-dependencies.jar -h

There are a number of predefined simulation scenarios defined in the [_sim_ directory](https://github.com/movsim/movsim/tree/master/sim). The `runmovsim` script can be used to run the simulator and gnuplot for plot these scenarios, for example:

    cd ../sim/bookScenarioStartStop/
    ../../core/runmovsim -f startStop_IDM.xml
    gnuplot startStop_IDM.gpl

The `.csv` output is put in the directory from which the simulator is called and the graphical output is put in `.eps` (Encapsulated PostScript) files.

The script `cleanmovsim` can be used to delete all simulation output files in the current directory.


Logging output
--------------

MovSim's logging output is controlled the `/config/log4j.properties` properties file.

Logging levels are: `DEBUG < INFO < WARN < ERROR`


Commercial use
--------------

For commercial use, please contact the copyright holders at movsim@akesting.de


Copyright
---------

MovSim is Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, and Martin Budden.

MovSim is licensed under [GPL version 3](https://github.com/movsim/movsim/blob/master/COPYING).

