// /*
// * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
// * <movsim.org@gmail.com>
// * -----------------------------------------------------------------------------------------
// *
// * This file is part of
// *
// * MovSim - the multi-model open-source vehicular-traffic simulator.
// *
// * MovSim is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * MovSim is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// * See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with MovSim. If not, see <http://www.gnu.org/licenses/>
// * or <http://www.movsim.org>.
// *
// * -----------------------------------------------------------------------------------------
// */
// package org.movsim.input.model.simulation;
//
// import java.util.Map;
//
// public class SpeedLimitDataPoint {
//
// /** The x in m. */
// private final double x;
//
// /** The speedlimit in m/s. */
// private final double speedlimit;
//
// /**
// * Instantiates a new speed limit data point impl.
// *
// * @param map
// * the map
// */
// public SpeedLimitDataPoint(Map<String, String> map) {
// this.x = Double.parseDouble(map.get("x"));
// this.speedlimit = Double.parseDouble(map.get("speedlimit_kmh")) / 3.6;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see org.movsim.input.model.simulation.SpeedLimitDataPoint#getPosition()
// */
// public double getPosition() {
// return x;
// }
//
// /*
// * (non-Javadoc)
// *
// * @see org.movsim.input.model.simulation.SpeedLimitDataPoint#getSpeedlimit()
// */
// public double getSpeedlimit() {
// return speedlimit;
// }
//
// }
