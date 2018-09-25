/*
 * Copyright (C) 2010, 2011, 2012 by Arne Kesting, Martin Treiber, Ralph Germ, Martin Budden
 * <movsim.org@gmail.com>
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

package org.movsim.simulator.roadnetwork.regulator;

import com.google.common.base.Preconditions;
import org.movsim.autogen.RegulatorType;
import org.movsim.autogen.RegulatorsType;
import org.movsim.simulator.SimulationRun;
import org.movsim.simulator.SimulationTimeStep;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class Regulators implements Iterable<Regulator>, SimulationTimeStep, SimulationRun.CompletionCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Regulators.class);

    private static final Set<String> REGULATOR_ID_CACHE = new HashSet<>();
    private static final Set<String> NOTIFY_OBJECTS_ID_CACHE = new HashSet<>();

    private final List<Regulator> regulators = new ArrayList<>();

    public Regulators(RegulatorsType regulatorsType, RoadNetwork roadNetwork) {
        REGULATOR_ID_CACHE.clear();
        NOTIFY_OBJECTS_ID_CACHE.clear();
        if (regulatorsType != null) {
            initialize(regulatorsType, roadNetwork);
        }
    }

    private void initialize(RegulatorsType regulatorsType, RoadNetwork roadNetwork) {
        for (RegulatorType regulatorType : regulatorsType.getRegulator()) {
            if (regulatorType.isSetId()) {
                if (!REGULATOR_ID_CACHE.add(regulatorType.getId())) {
                    throw new IllegalArgumentException("regulator id=" + regulatorType.getId() + " not unique!");
                }
            }
            Regulator regulator = Regulator.create(regulatorType, roadNetwork);
            regulators.add(regulator);
        }
    }

    @Override
    public final void timeStep(double dt, double simulationTime, long iterationCount) {
        for (Regulator regulator : regulators) {
            regulator.timeStep(dt, simulationTime, iterationCount);
        }
    }

    public void simulationCompleted(double simulationTime) {
        for (Regulator regulator : regulators) {
            regulator.simulationCompleted(simulationTime);
        }
    }

    @Override
    public void simulationComplete(double simulationTime) {
        LOG.info("simulation completed at simTime={}", simulationTime);
    }

    static boolean addNotifyObjectId(String id) {
        Preconditions.checkArgument(id != null && !id.isEmpty(), "invalid id=" + id);
        return NOTIFY_OBJECTS_ID_CACHE.add(id);
    }

    @Override
    public Iterator<Regulator> iterator() {
        return regulators.iterator();
    }

}
