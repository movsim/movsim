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
// import org.movsim.core.autogen.Inflow;
// import org.movsim.utilities.Units;
//
// public class InflowDataPoint {
//
// /** The time in seconds */
// private final double time;
//
// /** The flow in 1/s. */
// private final double flow;
//
// /** The speed in m/s. */
// private final double speed;
//
// /**
// * Constructor.
// *
// * @param inflowDataPoint
// * the map
// */
// public InflowDataPoint(Inflow inflowDataPoint) {
// this(inflowDataPoint.getT(), inflowDataPoint.getQPerHour(), inflowDataPoint.getV());
// }
//
// /**
// * Constructor.
// *
// * @param time
// * @param flowPerHour
// * @param speed
// */
// public InflowDataPoint(double time, double flowPerHour, double speed) {
// this.time = time;
// this.flow = flowPerHour * Units.INVH_TO_INVS;
// this.speed = speed; // given in m/s
// }
//
// public double getTime() {
// return time;
// }
//
// public double getFlow() {
// return flow;
// }
//
// public double getSpeed() {
// return speed;
// }
//
// }
