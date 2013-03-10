#!/usr/bin/perl
## perl -i -p -e 's/MovsimScenario/Scenario/g' *.xprj

use warnings;
use strict;

if ($#ARGV!= 0){
  print "\nerror: provide input file\n";
  exit(0);
}

my $project = $ARGV[0];
my $infile=$project;
open(IN,$infile)
  or die("Cannot open file \"$infile\". Abort!");
my @file=<IN>; #read in whole
close(IN);

`cp -v $infile $infile~`;  #backup

#oeffne zum schreiben (dabei wird bestehender inhalt geloescht (>)
open(OUT,">$infile")
  or die("Cannot open file \"$infile\" to write. Abort!");

for(my $i=0;$i<=$#file;$i++){
#  print $file[$i];
  $_ = $file[$i];
  $_ = replace("SCENARIO", "Scenario", $_);
  $_ = replace("<VEHICLE_TYPE", "<VehicleType", $_);
  $_ = replace("VEHICLES", "VehiclePrototypes", $_);
  $_ = replace("VEHICLE", "VehiclePrototypeConfiguration", $_);
  $_ = replace("LONGITUDINAL_MODEL", "AccelerationModelType", $_);
  $_ = replace("LANE_CHANGE_MODEL", "LaneChangeModelType", $_);
  $_ = replace("MEMORY", "MemoryParameter", $_);
  $_ = replace("NOISE", "NoiseParameter", $_);
  $_ = replace("b_max", "maximum_deceleration", $_);
  $_ = replace("consumption", "consumption_model_name", $_);
  $_ = replace("<IDM", "<ModelParameterIDM", $_);
  $_ = replace("<ACC", "<ModelParameterACC", $_);
  $_ = replace("<OVM_FVDM", "<ModelParameterOVM_FVDM", $_);
  $_ = replace("<GIPPS", "<ModelParameterGipps", $_);
  $_ = replace("<KRAUSS", "<ModelParameterKrauss", $_);
  $_ = replace("<NEWELL", "<ModelParameterNewell", $_);
  $_ = replace("<NSM", "<ModelParameterNSM", $_);
  $_ = replace("<KKW", "<ModelParameterKKW", $_);
  $_ = replace("<CCS", "<ModelParameterCCS", $_);
  $_ = replace("<PTM", "<ModelParameterPTM", $_);
  $_ = replace("<MOBIL", "<ModelParameterMOBIL", $_);
  $_ = replace("s_min", "minimum_gap", $_);
  $_ = replace("l_int", "transition_width", $_);
  $_ = replace("lambda", "gamma", $_);
  $_ = replace("variant", "optimal_speed_function", $_);
  $_ = replace("b_safe", "safe_deceleration", $_);
  $_ = replace("threshold", "threshold_acceleration", $_);
  $_ = replace("bias_right", "right_bias_acceleration", $_);
  $_ = replace("rho", "density_per_km", $_);
  $_ = replace("x=\"", "position=\"", $_);  ## dx --> dposition
  $_ = replace("SIMULATION", "Simulation", $_);
  $_ = replace("TRAFFIC_COMPOSITION", "TrafficComposition", $_);
  $_ = replace("ROUTES", "Routes", $_);
  $_ = replace("ROUTE", "Route", $_);
  $_ = replace("OUTPUT", "OutputConfiguration", $_);
  $_ = replace("SPATIOTEMPORAL", "SpatioTemporalConfiguration", $_);
  $_ = replace("FLOATING_CAR_DATA", "FloatingCarOutput", $_);
  $_ = replace("TRAJECTORIES", "Trajectories", $_);
  $_ = replace("TRAVELTIMES", "TravelTimes", $_);
  $_ = replace("FC", "FloatingCar", $_);
  $_ = replace("eur_rules", "european_rules", $_);
  $_ = replace("INITIAL_CONDITIONS", "InitialConditions", $_);
  $_ = replace("TRAFFIC_SOURCE", "TrafficSource", $_);
  $_ = replace("TRAFFIC_SINK", "TrafficSink", $_);
  $_ = replace("SPEED_LIMITS", "SpeedLimits", $_);
  $_ = replace("SPEED_LIMIT", "SpeedLimit", $_);
  $_ = replace("DETECTORS", "Detectors", $_);
  $_ = replace("CROSS_SECTION", "CrossSection", $_);
  $_ = replace("TRAFFIC_LIGHTS", "TrafficLights", $_);
  $_ = replace("TRAFFIC_LIGHT", "TrafficLight", $_);
  $_ = replace("FLOW_CONSERVING_INHOMOGENEITIES", "FlowConservingInhomogeneities", $_);
  $_ = replace("INHOMOGENEITY", "Inhomogeneity", $_);
  $_ = replace("SLOPES", "Slopes", $_);
  $_ = replace("SLOPE", "Slope", $_);
  $_ = replace("SIMPLE_RAMP", "SimpleRamp", $_);
  $_ = replace("ROAD", "Road", $_);
  $_ = replace("IC_MACRO", "MacroIC", $_);
  $_ = replace("IC_MICRO", "MicroIC", $_);
  $_ = replace("TRAFFIC_SINK", "TrafficSink", $_);
  $_ = replace("INFLOW", "Inflow", $_);
  $_ = replace("FUEL", "ConsumptionCalculation", $_);
  $_ = replace("fixed_seed=\"true\"", "", $_);
  $_ = replace("Detectors timestep=\"", "Detectors sample_interval=\"", $_);
 
 ## quick hack: drop second line defining doctype
  if($i != 1){
	print OUT $_;
  }
}

close(OUT);

#########################################################
sub replace {
      my ($from,$to,$string) = @_;
      $string =~ s/$from/$to/g; 
      return $string;
}

