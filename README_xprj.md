
What is the overall structure of a MovSim simulation?
-----------------------------------------------------

A MovSim simulation is bundled in a named "project" projectName and
consists of several files of the form projectName.extension


I wonder if there are typical example projects
----------------------------------------------

- all projects in ./sim/buildingBlocks/, i.e., all .xprj files in that
  directory ('.' refers to the location of this README file)

- bigger example: Vasaloppet simulation at sim/vasa/vasa_CCS.xprj

- mainroad with traffic lights and onramps:
  sim/output/city_example.xprj

- two games at sim/games/


How to run these projects at a linux command-line? 
-------------------------------------------------

Assume a bash shell with a working directory that of this README
file. Give following commands:

```bash
runViewer="java -jar $PWD/viewer/target/MovsimViewer-1.6.0-SNAPSHOT-jar-with-dependencies.jar"
```

examples:

```bash
$runViewer -f sim/buildingBlocks/trafficlight.xprj

$runViewer -f sim/buildingBlocks/offramp.xprj
```

Which input files are absolutely necessary?
------------------------------------------

each project consists at least of the following input files:

- projectName.xprj:  an overall project specification using xml syntax
- projectName.xodr:  network specification in the Opendrive format

Since the xodr file name is specified in projectName.xprj, it may also
be called differently, e.g. a common "network.xodr" for  the projects
"simulation1.xprj", "simulation2.xprj" etc. In any case, the .xodr
file specified in .xprj should exist and be consistent with that
project


Which input files are optional?
-------------------------------

- projectName.properties: Here, you can set global properties such as
  colors and screen sizes. If it does not exist, the file at
  ./viewer/target/classes/config/defaultviewerconfig.properties is
  used as default setting. You can copy this file to projectName.properties
  and use it as a template for further changes. It contains all global
  properties that are read/interpreted by MovSim. In your .properties,
  you do not need to specify all. Those unspecified are taken from the default

  Often, the screen-size settings, the scale [pixels/m], and the
  initial offsets [m] are changed, e.g., by the entries

```
  xPixSizeWindow=1000
  yPixSizeWindow=500
  initialScale=0.9
  xOffset=-300
  yOffset=-100
```

Many more initializations can be changed by the properties file, e.g.,
the initial simulation speed. To find a complete list in the sources,
just search for "getProperty", e.g. in Linux:

```
  grep getProperty `find . -name "*.java"`
```

- log4j.properties?




Building new projects: Elements of the XML project specification
================================================================

All movsim .xprj specification files have as top-level block
```xml
<Movsim> ... </Movsim>
```
(similar to the html body block)

Inside MovSim, following blocks can be specified (some of them optional, see
the examples)


Defining the types of the vehicle population, including obstacles:
------------------------------------------------------------------

general syntax:

```xml
<VehiclePrototypes>
     <VehiclePrototypeConfiguration label="ACC1" length="6" maximum_deceleration="9">
         <AccelerationModelType> ... </AccelerationModelType>
 	 <LaneChangeModelType ...> ... </LaneChangeModelType>
	 <NoiseParameter ... />
     </VehiclePrototypeConfiguration>

     [same for all the vehicle types of this simulation]
</VehiclePrototypes>
```

- Examples, e.g., in the .xodr files of *./sim/buildingBlocks/*

- There is an optional width attribute in
  VehiclePrototypeConfiguration. It may be used to make obstacles look
  differently/cover the whole road

- the percentage of the vehicle types on the various roads at
  simulation start and at the inflows is specified in the
  *TrafficComposition* block, see below.
  

Defining the car-following models
---------------------------------

this is done in the *AccelerationModelType* block. The specification consists of the model type
and the corresponding parameters, e.g., for the ACC model (an IDM
derivative, see www.traffic-simulation.de):

 ```xml
 <AccelerationModelType>
     <ModelParameterACC v0="35" T="1.2" s0="2" s1="0" delta="4"
                         a="1.2" b="2.0" coolness="1" />
  </AccelerationModelType>
```

- Examples of all the possible 14 models can be found in
  *./sim/bookScenarioStartStop/,   particularly

```
  ./sim/bookScenarioStartStop/startStop_all_continuous.xprj
  ./sim/bookScenarioStartStop/startStop_all_ca.xprj
```

- Some ModelParameter types are for several models, e.g.,
  *ModelParameterOVM_FVDM*

- Some models have fundamental-diagram/optimal-speed types as
  parameters. At present (feb18), only  *optimal_speed_function="bando"*
  is implemented

- Some models are cellular automata rather than time-continuous models
  with an update time step of typically 1 s rather than 0.1 or 0.2 s
  (*<Simulation timestep="0.2" ...>*). Therefore, the initial_sleep_time
  in the .properties file should be increased to obtain a similar
  timelapse as in the continuous models (ratio Simulation
  timestep/initial_sleep_time gives time-lapse factor)

- for obstacle vehicle types, set the desired speed parameter of the
  corresponding model v0="0" (since the label "obstacle" is just a
  label, MovSim does not know that this is an obstacle, so omitting
  the  car-following model will lead to a parsing error)

- for each *AccelerationModelType*, the parameter can optionally vary
  stochastically, see *TrafficComposition*

speed limits
-------------

Since speed limits are road network, and not vehicle-driver,
attribbutes, they are not defined in the car-following model block but
in the infrastructure (*.xodr*) file. The car-following models take
this info and decrease the models's desired speed (each model has one)
if it happens to be above the speed limit. See
*sim/buildingBlocks/speedlimit.xodr*.


defining the lane-changing models
---------------------------------

similar to the car-following models, there is a LaneChangeModelType
block inside of the <VehiclePrototypeConfiguration> block, e.g.,

  <LaneChangeModelType european_rules="true" crit_speed_eur="20">
      <ModelParameterMOBIL safe_deceleration="5.0" minimum_gap="2.0"
                           threshold_acceleration="0.05"
			   right_bias_acceleration="0.05" politeness="0.0" />
  </LaneChangeModelType>

- example project e.g., *sim/buildingBlocks/onramp.xprj*

- at present (feb18), only the MOBIL lane changing model is
  implemented but it is quite flexible (see www.traffic-simulation.de)

- for obstacles (i.e. the desired speed of the car-following model is
  zero), or for simulations w/o lane changes (one lane, no ramps), you
  do not need a lane-change specification, Just give 
  <LaneChangeModelType /> for these cases


Introducing acceleration noise
------------------------------

Example:

```xml
<NoiseParameter tau="5" fluct_strength="0.1" />
```
inside the *VehiclePrototypeConfiguration* block

- defines for this vehicle type acceleration noise of amplitude
  fluct_strength and a persistence (correlation) time *tau*:
  
- the stochastic acceleration *A=fluct_strength* *W(tau)*
  where *W(tau)* is a standard Wiener process

- for *tau* to infty, the noise becomes a vehicle-individual permanent acceleration
  bias, roughly comparable with defining heterogeneous  model
  parameters by the *relative_v0_randomization* attribute of *VehicleType*
  inside *TrafficComposition*


Defining the network 
--------------------

in the Scenario block just below the toplevel MovSim block, a link is
given to the file containing the network specification in the
Opendrive format, e.g.,

```xml
  <Scenario network_filename="onramp.xodr">
     <Simulation ...>
         ...
     </Simulation>
  </Scenario>
```

- the top-level block of the .xodr files is always

```xml
<OpenDRIVE>
  ...
</OpenDRIVE>
 ```
 
- several *.xprj* projects can link to the same network *.xodr* file,
  e.g., when simulating a network with several car-following/lane
  changing models, different traffic flows etc

- the road id's of the *road* blocks of the *.xodr* file must be consistent with the Road
  id's in the *Simulation* blocks, e.g.,
  
  ```xml
  <road name="R1" length="1200.0" id="1" junction="-1"> ... ,
  <Road id="1"> ...
  ```
  
  inside the *OpenDrive* block of the *.xodr* file and the *Simulation*
  block of the *.xprj* file, respectively.

- not the complete Opendrive specifications is implemented, e.g., no
  Clothoides (linear changes of the curvature)

- the *road*'s length attribute is often irrelevant; what
  counts is the length inside the block *planView* which is inside *.xodr*'s *road* 


Defining the top-level simulation attributes
--------------------------------------------

These are the attributes of the *Simulation* block inside the *Scenario*
block, e.g.,

```xml
<Simulation timestep="0.2" duration="1200" seed="42" crash_exit="true">
```

- *timestep* is the only mandatory attribute. For time-continuous
  models, 0.1-0.2 [seconds] are good values, for cellular automata,
  and the Newell and Gipps models, the time step is a parameter and
  typically *timestep="1"*

- *duration* in seconds. default: infinity

- *seed* is only relevant if there are stochastic elements. Without
  specification, the seed is determined dynamically by the starting
  time. Use a defined seed to get reproducible result and no seed
  specification when several trajectory realisations should be
  obtained by several simulation runs

- *crash_exit* is typically set to "true" (default) for testing and to "false"
  for public demos. WARNING: unlike the JavaScript simulator at
  traffic-simulator.de, MovSim is very easily confused by crashes with
  the consequence that sometimes vehicles do no longer recognize their
  leaders and just "drive through". Sometimes, ignoring crashes works,
  though (*sim/vasa/vasa_CCS.xprj*). Probably solvable by doing a
  quicksort of the vehicle positions after every timestep (go for it,
  developers!) 


Defining the percentages of the vehicle-driver types: block *TrafficComposition*
-------------------------------------------------------------------------

The *TrafficComposition* block can be placed inside the
*Simulation* block, and optionally also inside *Road* blocks

- typical specification:
```xml
  <TrafficComposition>
      <VehicleType label="ACC1" fraction="0.9" relative_v0_randomization="0.1" />
      <VehicleType label="ACC2" fraction="0.1" relative_v0_randomization="0.1" />
  </TrafficComposition>
```

- the labels must correspond to the labels given in the
  *VehiclePrototypeConfiguration* block

- the percentages are interpreted liberally: if the sum is below 1,
  the percentage of the last type will be increased such that it is 1,
  if the sum is above 1, the percentage of the last type with initial
  cumulated percentage < 1 will be reduced accordingly, and all further
  types will be ignored. 

- besides a percentage, also distributed parameters (heterogeneous
  vehicles/drivers) may be given. See "introducing acceleration
  noise" for introducing time-dependent stochasticity within a single
  vehicle/driver  

- the traffic composition applies to the initial condition (unless
  microICs are specified) and the external inflows (sources)

- a *TrafficComposition* block just below  *Simulation* applies for all
  roads of the network. It may be overridden for certain roads by *TrafficComposition*
  blocks inside *Road* blocks, for an example, see
  sim/buildingBlocks/onramp.xprj

- of course, the traffic flow dynamics may lead to different
  compositions over time, particularly if a road-specific composition
  is specified for a road without an external source. Furthermore,
  microscopic initial conditions override the *TrafficComposition* 


Defining initial and boundary conditions, including traffic demand: *Road*
--------------------------------------------------------------------------

General form of the *Road* block inside the *Simulation* block:

```xml
  <Road id="idString">
     <TrafficComposition> ... </TrafficComposition>
     <InitialConditions> ... </InitialConditions>
     <TrafficSource> ... </TrafficSource
  </Road>
```

- as already said, the .xprj's Road id (not label!) must agree with
  the .xodr's road id (not name!)

- *TrafficComposition* is optional if global composition is to be
  overridden

- *InitialConditions* can be micro or macro or missing (then start
  with empty road):

```xml
     <InitialConditions>
         <MicroIC position="10" speed="0" />
          ...
     </InitialConditions> 
```
or
```xml
     <InitialConditions>
         <MacroIC position="10" density_per_km="100" speed="0" />
     </InitialConditions>
```

- the TrafficSource is always macroscopic and only allowed for
  roads with sources, i.e., no *predecessor* elements in .xodr's
  *road* block with the fitting id. It specifies a time-dependent
  inflow (linearly interpolated, constantly extrapolated). The
  composition is given by the global or road'specific *TrafficComposition*:

 ```xml
     <TrafficSource logging="false">
          <Inflow t="0" q_per_hour="1200" />
          <Inflow t="600" q_per_hour="1600" />
      </TrafficSource>
```

- not all roads of the .xodr network need a traffic specification
  *Road* in .xprj: If a road has no open inflowing end (a *predecessor*
  exists in .xodr) or has zero
  inflow, and has zero initial density, nothing needs to be specified. The
  dynamics and the network takes care of the filling with vehicles



Routes
------

These are given by the optional block *Routes*  inside *Scenario* and
define routes as a sequence of connected links, i.e., *Road*s. In
the following example, two routes using the links 1,4, and the link 3,
are set up:
```xml
<Routes>
<Route label="route1">
  <Road id="1" />
  <Road id="4" />
</Route>
<Route label="route2">
  <Road id="3" />
</Route>
</Routes>
```
An error is cast if the links are not connected.


- This can be used to implement traffic demands given by an OD matrix,
  where each OD element uses a given (shortest) route 

- For output of vehicle trajectories, it is also necessary to define
  routes as part of *OutputConfiguration* inside *Scenario*:

```xml
<OutputConfiguration>
  <Trajectories dt="1" route="route1" />
  ...
</OutputConfiguration>
```

- Example: *sim/output/city_example.xprj*


Traffic lights
---------------

These are given by the optional block *TrafficLights* inside
*Scenario*

- see, e.g,. *sim/buildingBlocks/trafficlight.xprj*

- predefined cicles

- overridden by clicking on the light


Output specification
--------------------

Given by the optional *OutputConfiguration* block inside *Scenario*

- all output is written in the directory from which the simulation is
  run, not at the locations of the .xprj and .xodr files!

- all output files names start with projectName and have as the last
  extension .csv (so can be easily removed since no input file has the
  *.csv* ending)

- Examples of an output include *Trajectories* giving trajectories of
  all vehicles on a specified route (see above) and
  *FloatingCarOutput* giving xFCD for a defined set of vehicle indices
  on defined routes (if you just wand the vehicles on a road segment,
  just define a route with one segment). For example,

```xml
<FloatingCarOutput n_timestep="5" route="main">
  <FloatingCar number="2" />
  <FloatingCar number="11" />
</FloatingCarOutput>
```

will record Car 2 and Car 11 of the route/road segment "main" (notice
that the first car has the lowest index).


Interaction
-----------

see the scenarios with traffic lights and the games
