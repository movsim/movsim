MovSim
======

MovSim = **M**ulti-model **o**pen-source **v**ehicular-traffic **Sim**ulator.

http://www.movsim.org

Demonstration:

(MovSim v1.0) www.verkehrsdynamik.de/simulation.shtml

(MovSim v1.2) www.movsim.org/app.shtml

(Routing game) www.movsim.org

(Android client) Android App


Description
-----------

MovSim is a microscopic traffic simulator with xml-based configuration and csv text output.

Features:

- multi-lane simulator including onramps, offramps, "flow-conserving bottlenecks" and traffic-lights
- multiple models of different model classes (car-following models, coupled-map models and cellular automata)
  * Intelligent Driver Model (IDM) [Wikipedia](http://en.wikipedia.org/wiki/Intelligent_driver_model)
  * Enhanced IDM/Adaptive Cruise Control Model [Preprint] (http://arxiv.org/abs/0912.3613)
  * Optimal Velocity or Bando Model 
  * Velocity Difference Model 
  * Gipps Model [Wikipedia] (http://en.wikipedia.org/wiki/Gipps%27_Model)
  * Krauss Model
  * Nagel-Schreckenberg Cellular Automaton [Wikipedia] (http://en.wikipedia.org/wiki/Nagel-Schreckenberg_model)
  * Kerner-Klenov-Wolf Cellular Automaton
- general lane-changing model MOBIL [Paper](http://www.akesting.de/download/MOBIL_TRR_2007.pdf)
- detailed physics-based model for fuel consumption and emissions
- text-file output of detectors, spatiotemporal fields, floating-car data etc.

MovSim has several main components: 

* The _core_ contains the main MovSim library and a console application that can run a traffic simulation and produce _.csv_ output for further processing or graphical display.
* The _viewer_ displays an animated traffic simulation.
* The _consumption_ comprises a physics-based fuel consumption model which can also be fed by csv data.


Installation
------------

Install the [git](http://git-scm.com/download) version control system.

Download this repository:

    git clone git@github.com:movsim/movsim.git
    
or
       
    git clone https://github.com/movsim/movsim.git
              
Install [Java](http://www.java.com/en/download/manual.jsp), if you do not already have it. You need at least version 7
of Java (JRE 1.7).

Install [Apache Maven] (http://maven.apache.org/download.html). Maven is the software build and management tool that is used to build MovSim.

MovSim produces output that can be plotted using _gnuplot_. If you wish to use this output to produce graphs, install [gnuplot](http://www.gnuplot.info/).


Usage
-----

To build MovSim, type `mvn install` from the main MovSim directory.

To run the movsim _core_ or _viewer_ see their respective readme files: [core](https://github.com/movsim/movsim/blob/master/core/README.md) and [viewer](https://github.com/movsim/movsim/blob/master/viewer/README.md).


Eclipse
-------

MovSim can readily be built and run from within the Eclipse IDE. To use Eclipse:

Install the [Eclipse IDE for Java Developers](http://www.eclipse.org/downloads/), if you do not already have it.

From with the Eclipse IDE install the m2e(Maven Integration for Eclipse) plugin (from the _Help_ menu in Eclipse select _Eclipse Marketplace..._ and in the resulting enter `maven` in the _Find_ box and then install the plugin).

Import the project into Eclipse from the _File >> Import_ menu item. In the resulting Select dialog, choose the _General >> Existing Projects into Workspace_ option. In the resulting dialog select the `movsim/core` directory and import. Repeat for the `movsim/viewer` directory.

Alternatively, create an eclipse configuration from the commandline by typing `mvn eclipse:eclipse` and using the Eclipse _Import..._ dialog for `Existing Maven Project`. Attention: this method leads to bug in eclipse. See [maven.README](https://github.com/movsim/movsim/blob/master/maven.README).

You can then build and run either the _core_ or _viewer_ Java applications.

We use the source code formatter _movsim/codestyle/eclipse_movsim_profile.xml_.
 
Demos
-----

There are a number of predefined simulation scenarios defined in the [_sim_ directory](https://github.com/movsim/movsim/tree/master/sim).

Applet demos on the website www.movsim.org


Commercial use
--------------

For commercial use, please contact the copyright holders at movsim.org@gmail.com.


Copyright
---------

MovSim is Copyright (C) 2010, 2011, 2012, 2013 by Arne Kesting, Martin Treiber, Ralph Germ, and Martin Budden.

MovSim is licensed under [GPL version 3](https://github.com/movsim/movsim/blob/master/COPYING).

