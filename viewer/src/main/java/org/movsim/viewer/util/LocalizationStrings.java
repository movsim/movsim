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

package org.movsim.viewer.util;

import java.util.ListResourceBundle;

/**
 * Localization strings for the MovSim Traffic MovsimMain.
 * 
 */
public class LocalizationStrings extends ListResourceBundle {
    // Default, English version
    @Override
    public Object[][] getContents() {
        return contents;
    }

    @SuppressWarnings("nls")
    private static final Object[][] contents = {
            { "FrameName", "MovSim" },

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
            { "FuelConsumption", "Fuel Consumption" },

            // View Menu
            { "ViewMenu", "View" },
            { "VehicleColors", "Vehicle colors" },
            { "VehicleColorSpeedDependant", "Speed Dependant" },
            { "VehicleColorRandom", "Random" },

            { "LogOutput", "Log messages of simulator" },
            { "StatusPanel", "Display Status Panel" },
            { "DrawRoadIds", "Draw Road Ids" },
            { "DrawSources", "Draw Sources" },
            { "DrawSinks", "Draw Sinks" },
            { "DrawSpeedLimits", "Draw Speed Limits" },
            { "DrawFlowConservingBootleNecks", "Draw Flow-Conserving-Bootlenecks" },
            { "DrawRoutesTravelTime", "Draw Routes for Travel Time" },
            { "DrawRoutesSpatioTemporal", "Draw Routes for Spatio-Temporal-Contour" },

            { "VehicleColorSpectrum", "Color spectrum according to speed" },
            { "VehicleColorBlack", "All Black" },

            // Scenarios Menu
            { "ScenarioMenu", "Scenarios" },
            { "OnRamp", "on ramp" },
            { "OffRamp", "off ramp" },
            { "FlowConservingBottleNeck", "flow conserving bottleneck" },
            { "SpeedLimit", "speed limit" },
            { "TrafficLight", "traffic light" },
            { "LaneClosing", "lane closing" },
            { "CloverLeaf", "clover leaf" },
            { "RoundAbout", "roundabout" },
            { "CityInterSection", "city intersection" },
            { "RingRoad", "ring road" },

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
            { "lblScenario", "current scenario: " },
            { "lblSimTime", "simulation time: " },
            { "simTimeTooltip", "The time of current simulation since start." },
            { "deltaTimeTooltip", "The simulation's numerical update (integration) time step." },
            { "lblDeltaTime", "update time: " },
            { "timeWarpTooltip", "The speed-up of the simulation against the simulation time." },
            { "lblTimeWarp", "time warp:" },
            { "traveltimeTooltip", "The (smoothed) travel time of route " },
            { "traveltime", "travel time " },
            { "highway", "highway" },
            { "detour", "detour" },

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

            { "VehicleColorsTip", "Changes vehicle colors to reflect their speed or acceleration" },
            { "Vehicles", "Change Vehicles" },
            { "VehiclesTip", "Changes vehicles" },

            // old
            { "ControlMenu", "Control" },
            { "VehicleColors", "Vehicle Colors" },

            { "VarietyPack", "Variety\nPack" },
            { "VarietyPackTip", "Displays a variety of road traffic scenarios" },
            { "Cloverleaf", "Cloverleaf" },
            { "CloverleafTip", "Displays a cloverleaf junction" },
            { "Perturbation", "Bottleneck" },
            { "PerturbationTip", "Displays a section of road containing a bottleneck" },
            { "Bezier", "Bezier curve" },
            { "BezierTip", "Displays a curved section of road" },
            { "Racetrack", "Scalextric" },
            { "RacetrackTip", "Displays two cars on a simple racetrack" },
            { "TrafficAppletApplication", "TrafficAppletApplication" },
            { "TestTip", "Displays the current test scenario" },
            { "onramp MovSim", "onramp MovSim" },
            { "onramp MovSimTip", "onramp MovSim" },
            { "test MovSim", "test MovSim" },
            { "test MovSimTip", "test MovSim" },
            { "startStop_KKW", "startStop_KKW" },
            { "startStop_KKWTip", "startStop_KKW" },
            { "startStop_IDM_COMPARE", "startStop_IDM_COMPARE" },
            { "startStop_IDM_COMPARETip", "startStop_IDM_COMPARE" },
            { "startStop_All", "startStop_All_Models" },
            { "startStop_AllTip", "startStop_All_Models" },
            { "Offramp_Onramp", "Offramp_Onramp" },
            { "Offramp_OnrampTip", "Offramp_Onramp" },
            { "onramp_OVM", "onramp_OVM" },
            { "onramp_OVMTip", "onramp scenario, fact sheet book" },
            { "onramp_VDIFF", "onramp_VDIFF" },
            { "onramp_FVDMTip", "onramp scenario, fact sheet book" },
            { "onramp_IDM", "onramp_IDM" },
            { "onramp_IDMTip", "onramp scenario, fact sheet book" },
            { "onramp_IIDM", "onramp_IIDM" },
            { "onramp_IIDMTip", "onramp scenario, fact sheet book" },
            { "onramp_IDMM", "onramp_IDMM" },
            { "onramp_IDMMTip", "onramp scenario, fact sheet book" },
            { "onramp_ACC", "onramp_ACC" },
            { "onramp_ACCTip", "onramp scenario, fact sheet book" },
            { "onramp_KRAUSS", "onramp_KRAUSS" },
            { "onramp_KRAUSSTip", "onramp scenario, fact sheet book" },
            { "onramp_GIPPS", "onramp_GIPPS" },
            { "onramp_GIPPSTip", "onramp scenario, fact sheet book" },
            { "onramp_NSM", "onramp_NSM" },
            { "onramp_NSMTip", "onramp scenario, fact sheet book" },
            { "onramp_BARL", "onramp_BARL" },
            { "onramp_BARLTip", "onramp scenario, fact sheet book" },
            { "onramp_KKW", "onramp_KKW" },
            { "onramp_KKWTip", "onramp scenario, fact sheet book" },
            { "laneClosing_ACC", "two lane motorway with a closing lane area" },
            { "laneClosing_ACCTip", "two lane motorway with a closing lane area" },
            { "trafficlight_ACC", "two lane city traffic" },
            { "trafficlight_ACCTip", "two lane city traffic" },

            // vehicle information popup window
            {
                    "VehiclePopup",
                    "Vehicle\n  id: %d\n type: %s\n lane: %d\n  pos: %.0fm\n  vel: %.0f km/h\n  acc: %.4f m/s\u00B2\n  distance: %.0fm\n  exit: R%d(L%d:L%d)" },
            {
                    "VehiclePopupNoExit",
                    "Vehicle\n  id: %d\n  lane: %d\n  pos: %.0fm\n  vel: %.0f km/h\n  acc: %.4f m/s\u00B2\n  distance: %.0fm\n  exit: end of road" },

            // status messages
            { "Paused", "Paused" },
            { "RampingFinished", "Ramping finished" },
            { "PerturbationApplied", "Perturbation applied" },
            { "TrafficInflow", "Traffic inflow: %d vehicles per hour" },
            { "SerieMainRoute", "Route Highway" },
            { "RouteDetour", "Route Detour" },

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
                    "Version Viewer: 0.1" + " " + "\n" + "Version movsim.org: 1.1" + " " + "\n"
                            + "=========================================================================\n"
                            + "authors: Arne Kesting (TomTom), Ralph Germ, Martin Budden, Martin Treiber\n"
                            + "email: mail@akesting.de\n" + "(c) 2010, 2011, 2012\n"
                            + "=========================================================================\n" } };
}
