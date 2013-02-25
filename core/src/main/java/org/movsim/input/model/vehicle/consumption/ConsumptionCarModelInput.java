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
// package org.movsim.input.model.vehicle.consumption;
//
// import java.util.Map;
//
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
//
// public class ConsumptionCarModelInput {
//
// /** The Constant logger. */
// final static Logger logger = LoggerFactory.getLogger(ConsumptionCarModelInput.class);
//
// private final double vehicleMass; // in kg
// private final double crossSectionSurface; // in m^2
// private final double cdValue; // unitless
// private final double electricPower; // in Watt
// private final double consFrictionCoefficient; // unitless
// private final double vFrictionCoefficient;
// private final double dynamicTyreRadius; // in m
//
// public ConsumptionCarModelInput(Map<String, String> map) {
// this.vehicleMass = Double.parseDouble(map.get("mass"));
// this.crossSectionSurface = Double.parseDouble(map.get("cross_section_surface"));
// this.cdValue = Double.parseDouble(map.get("cd_value"));
// this.electricPower = Double.parseDouble(map.get("electric_power"));
// this.consFrictionCoefficient = Double.parseDouble(map.get("const_friction"));
// this.vFrictionCoefficient = Double.parseDouble(map.get("v_friction"));
// this.dynamicTyreRadius = Double.parseDouble(map.get("dynamic_tyre_radius"));
// }
//
// public double getVehicleMass() {
// return vehicleMass;
// }
//
// public double getCrossSectionSurface() {
// return crossSectionSurface;
// }
//
// public double getCwValue() {
// return cdValue;
// }
//
// public double getElectricPower() {
// return electricPower;
// }
//
// public double getConsFrictionCoefficient() {
// return consFrictionCoefficient;
// }
//
// public double getvFrictionCoefficient() {
// return vFrictionCoefficient;
// }
//
// public double getDynamicTyreRadius() {
// return dynamicTyreRadius;
// }
// }
