MovSim
======

MovSim = **M**ulti-model **o**pen-source **v**ehicular-traffic **Sim**ulator.

http://www.movsim.org

Demonstration: www.verkehrsdynamik.de 


Description
-----------

MovSim is a microscopic traffic simulator with xml-based configuration and csv text output. 

Features:

- one-lane simulator of one mainroad
- onramp and flow-conserving bottlenecks
- traffic-lights
- multiple models and different model classes (continuous models like Intelligent Driver Model, 
  coupled-map models like Gipps and cellular automata like Nagel-Schreckenberg)
- text-file output of detectors, spatiotemporal fields, floating-car data etc. 


Installation
------------

Install the [git](http://git-scm.com/download) version control system.

Download this repository:

    git clone git@github.com:movsim/movsim.git
    
or
       
    git clone https://github.com/movsim/movsim.git
              

Install [Java](http://www.java.com/en/download/manual.jsp), if you do not already have it. You need at least version 6
of Java (JRE 1.6 or higher).

Install [Apache Maven](http://maven.apache.org/download.html). Maven is the software build and management tool that is used to build MovSim.

MovSim produces output that can be plotted using _gnuplot_. If you wish to use this output to produce graphs, install [gnuplot](http://www.gnuplot.info/).


Usage
-----

To build MovSim, type `mvn install` from the main MovSim directory.

There are a number of predefined simulation scenarios defined in the `sim/` directory. The `runapp` script can be used
to run these scenarios and plot the results using gnuplot, for example:

    ./runapp startStop_IDM

etc. The `.csv` output is put in the `sim` directory. The graphical output is put in the `sim/figs` directory, in `.eps` (Encapsulated PostScript) files.

MovSim can be run directly from the command-line. To see the MovSim options, type:

    java -jar target/movsim-1.0-jar-with-dependencies.jar -h


Commercial use
--------------

For commercial use, please contact the copyright holders at info@movsim.org


Copyright
---------

MovSim is Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber, Ralph Germ, and Martin Budden.

MovSim is licensed under [GPL version 3](https://github.com/movsim/movsim/blob/master/COPYING).

