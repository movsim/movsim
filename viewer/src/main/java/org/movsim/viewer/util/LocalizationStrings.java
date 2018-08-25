/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden <movsim.org@gmail.com>
 * ----------------------------------------------------------------------------------------- This file is part of MovSim - the
 * multi-model open-source vehicular-traffic simulator. MovSim is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version. MovSim is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details. You should have received a copy of the GNU General Public License along with MovSim. If not, see
 * <http://www.gnu.org/licenses/> or <http://www.movsim.org>.
 * -----------------------------------------------------------------------------------------
 */

package org.movsim.viewer.util;

import java.util.ListResourceBundle;

/**
 * Localization strings for the MovSim Traffic MovsimMain.
 */
public class LocalizationStrings extends ListResourceBundle {
    // Default, English version
    @Override
    public Object[][] getContents() {
        return contents;
    }

    @SuppressWarnings("nls")
    private static final Object[][] contents =
            {
                    { "FrameName", "MovSim.org - Multi-model open-source vehicular traffic Simulator" },

                    // File Menu
                    { "FileMenu", "File" },
                    { "FileMenuOpen", "Open File..." },
                    { "XmlEditor", "Open XML Editor" },
                    { "FileMenuPreferences", "Preferences" },
                    { "FileMenuExit", "Exit" },

                    // Output Menu
                    { "OutputMenu", "Output" },
                    { "TravelTime", "Travel Time" },
                    { "Detectors", "Virtual Detectors" },
                    { "FloatingCars", "Floating Cars" },
                    { "SpatioTemporal", "Spatio Temporal Diagram" },
                    { "Consumption", "Fuel Consumption" },

                    // View Menu
                    { "ViewMenu", "View" },
                    { "VehicleColors", "Vehicle colors" },
                    { "VehicleColorSpeedDependant", "Speed Dependant" },
                    { "VehicleColorRandom", "Random" },

                    { "LogOutput", "Log messages of simulator" },
                    { "DrawRoadIds", "Draw Road Ids" },
                    { "DrawSources", "Draw Sources" },
                    { "DrawSinks", "Draw Sinks" },
                    { "DrawSpeedLimits", "Draw Speed Limits" },
                    { "DrawFlowConservingBootleNecks", "Draw Flow-Conserving-Bootlenecks" },
                    { "DrawRoutesTravelTime", "Draw Routing for Travel Time" },
                    { "DrawRoutesSpatioTemporal", "Draw Routing for Spatio-Temporal-Contour" },

                    { "VehicleColorSpectrum", "Color spectrum according to speed" },
                    { "VehicleColorBlack", "All Black" },

                    // Scenarios Menu
                    { "ScenarioMenu", "Scenarios" },
                    { "OnRamp", "on ramp" },
                    { "OffRamp", "off ramp" },
                    { "FlowConservingBottleNeck", "flow conserving bottleneck" },
                    { "SpeedLimitOUTDATED", "speed limit" },
                    { "TrafficLight", "traffic light" },
                    { "LaneClosing", "laneIndex closure" },
                    { "CloverLeaf", "clover leaf" },
                    { "RoundAbout", "roundabout" },
                    { "CityInterSection", "city intersection" },
                    { "RingRoad", "ring road with one laneIndex" },
                    { "RingRoad2Lanes", "ring with road two lanes" },
                    { "GameRampMetering", "ramp metering game" },
                    { "GameRouting", "routing game" },
                    { "Vasaloppet", "Vasaloppet start phase simulation" },
                    { "VasaloppetThreeWaves", "Vasaloppet start phase simulation with 3 waves" },
                    { "VasaloppetTenWaves", "Vasaloppet start phase simulation with 10 waves" },

                    // Help Menu
                    { "HelpMenu", "Help" },
                    { "HelpMenuAbout", "About" },
                    { "HelpMenuDocumentation", "Documentation" },
                    { "LanguageChooser", "Change Language" },
                    { "English", "English" },
                    { "German", "German" },

                    // Model Menu
                    { "ModelMenu", "Model" },
                    { "ModelMenuViewParams", "Defined Vehicles" },

                    // TOOLBAR
                    { "ToolBarTitle", "MovSim toolbar" },

                    // StatusPanel
                    { "lblScenario", "scenario: " },
                    { "lblUnspecifiedScenarioName", "not spezified" },
                    { "lblSimTime", "time: " },
                    { "simTimeTooltip", "The time of the current simulation since start." },
                    { "deltaTimeTooltip", "The simulation's numerical update (integration) time step." },
                    { "lblDeltaTime", "update time [s]:" },
                    { "timeWarpTooltip", "The speed-up of the simulation against the simulation time." },
                    { "lblTimeWarp", "time warp:" },
                    { "vehicleCountTooltip", "The total number of vehicles in the current simulation." },
                    { "lblVehicleCount", "vehicles:" },
                    { "vehiclesMeanSpeedTooltip", "The mean speed over all vehicles in the current simulation." },
                    { "lblVehiclesMeanSpeed", "speed [km/h]:" },
                    { "vehiclesStoppedTooltip", "The number of stopped vehicles with zero speed in the simulation." },
                    { "lblVehiclesStopped", "Stopped vehicles:" },

                    // Buttons
                    { "SimulationComplete", "Simulaton complete" },
                    { "SimulationCompleteTitle", "Finished" },
                    { "Pause", "Pause" },
                    { "PauseTip", "Pauses the animation" },
                    { "Start", "Start" },
                    { "StartTip", "Controls the animation" },
                    { "Resume", "Resume" },
                    { "ResumeTip", "Resumes the animation" },
                    { "Restart", "Restart" },
                    { "RestartTip", "Restarts the animation" },
                    { "Reset", "Reset" },
                    { "ResetTip", "Resets the animation" },
                    { "ZoomIn", "Zoom in" },
                    { "ZoomInTip", "Zooms in" },
                    { "ZoomOut", "Zoom out" },
                    { "ZoomOutTip", "Zooms out" },
                    { "Recenter", "Recenter" },
                    { "RecenterTip", "Recenters the animation" },
                    { "Faster", "Faster" },
                    { "FasterTip", "Speeds up the animation" },
                    { "Slower", "Slower" },
                    { "SlowerTip", "Slows down the animation" },
                    { "VehicleColorsTip", "Toggles through color modes" },

                    // vehicle information popup window
                    {
                            "VehiclePopup",
                            "Vehicle\n  id: %d\n label: %s\n type: %s\n lane: %d\n  pos: %.0fm\n  vel: %.0f km/h\n  acc: %.4f m/s\u00B2\n  distance: %.0fm\n  exit: %s" },
                    { "VehiclePopupNoExit", "end of road" },

                    // status messages
                    { "Paused", "Paused" },
                    { "RampingFinished", "Ramping finished" },
                    { "PerturbationApplied", "Perturbation applied" },
                    { "TrafficInflow", "Traffic inflow: %d vehicles per hour" },
                    {
                            "SimulationFinished",
                            "Simulation finished in %d seconds\nTotal travel time (all vehicles): %d seconds\nTotal travel distance (all vehicles): %d km\nTotal fuel used (all vehicles): %.2f liters\nFrom %d run(s) you made it to rank %d" },
                    { "AskingForName", "Please enter your name:" },

                    // Error messages
                    { "NoTravelTime", "No travel time configuration provided in xml!" },

                    // Diagrams
                    { "TitleFrameTravelTime", "Travel Times" },
                    { "xLabelChart", "Simulation time [min]" },
                    { "yLabelChart", "Travel Time [min]" },

                    // Preferences
                    { "TitlePreferences", "MovSim Viewer Preferences" },

                    // LogWindow
                    { "LogWindowTitle", "Log Output Window" },
                    { "LogInfo", "info" },
                    { "LogDebug", "debug" },
                    { "LogOff", "off" },
                    { "LogWarn", "warn" },
                    { "LogError", "error" },

                    // Editor
                    { "TitleEditor", "Xml Viewer" },

                    // Key press help text
                    { "KeyShortcuts",
                            "KEYS - F: faster, S: slower, I: zoom in, O: zoom out, P: toggle pause, V: vehicle colors" },

                    // Help text
                    { "HelpText", "This is the help text" },
                    // About dialog
                    { "AboutTitle", "About" },
                    {
                            "AboutText",
                            "MovSim.org -- Multi-model open source vehicular traffic simulator" + "\n"
                                    + "Movsim Viewer Version: 1.0" + " " + "\n" + "Movsim Core Version: 1.2" + " " + "\n"
                                    + "====================================================================\n"
                                    + "authors: Arne Kesting, Ralph Germ, Martin Budden, Martin Treiber\n"
                                    + "email: movsim.org@gmail.com\n" + "(c) 2010, 2011, 2012\n"
                                    + "====================================================================\n" } };
}
