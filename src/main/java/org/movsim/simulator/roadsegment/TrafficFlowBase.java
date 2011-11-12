/*
 * Copyright (C) 2010, 2011  Martin Budden, Ralph Germ, Arne Kesting, and Martin Treiber.
 *
 * This file is part of MovSim.
 *
 * MovSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MovSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MovSim.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.movsim.simulator.roadsegment;

import org.movsim.simulator.SimulationTimeStep;

/**
 * Abstract base class for all traffic sources (inflows) and sinks (outflows).
 * 
 */
public abstract class TrafficFlowBase implements SimulationTimeStep {

    /**
     * Types of traffic sources, sinks and junctions.
     * 
     */
    public static enum Type {
        /**
         * Traffic Source.
         */
        SOURCE,
        /**
         * Traffic Sink.
         */
        SINK,
        /**
         * Closed Loop - acts as source and sink for a closed road loop.
         */
        CLOSED_LOOP,
        /**
         * JUNCTION source/sink is deprecated, junctions are now handled by joining road segments
         * using Junction class.
         */
        @Deprecated
        JUNCTION
    }

    protected final Type type;
    private static final int INITIAL_ID = 1;
    /**
     * The "not set" value for the id.
     */
    public static int ID_NOT_SET = -1;
    private static int nextSourceId = INITIAL_ID;
    private static int nextSinkId = INITIAL_ID;
    protected final int id;

    // for TrafficSource and subclasses roadSegment is the sink road
    // for TrafficSinks and similar roadSegment is the source road
    protected RoadSegment roadSegment;
    // position of TrafficFlowBase on road segment
    protected double position;

    /**
     * Resets the next id.
     */
    public static void resetNextId() {
        nextSourceId = INITIAL_ID;
        nextSinkId = INITIAL_ID;
    }

    /**
     * Returns the number of TrafficFlows that have been created. Used for instrumentation.
     * 
     * @return the number of TrafficFlows that have been created
     */
    public static int count() {
        return nextSourceId + nextSinkId - 2 * INITIAL_ID;
    }

    /**
     * Constructor, sets the type and assigns a unique id.
     * 
     * @param type
     */
    protected TrafficFlowBase(Type type) {
        this.type = type;
        id = type == Type.SOURCE ? nextSourceId++ : nextSinkId++;
    }

    /**
     * Returns this traffic source's type.
     * 
     * @return this traffic source's type
     */
    public final Type type() {
        return type;
    }

    /**
     * Returns this traffic source's id.
     * 
     * @return this traffic source's id
     */
    public final int id() {
        return id;
    }

    /**
     * Returns this traffic source's id as a string suitable for display.
     * 
     * @return this traffic source's id as a string suitable for display
     */
    public final String idString() {
        if (type == Type.SOURCE || type == Type.SINK) {
            return "S" + id; //$NON-NLS-1$
        }
        return "J" + id; //$NON-NLS-1$
    }

    final double position() {
        return position;
    }

    protected final void setRoadSegment(RoadSegment roadSegment) {
        // a source has its road segment set once and only once, by the road segment
        // in its setSource method
        assert this.roadSegment == null;
        assert roadSegment != null;
        assert roadSegment.source() == this || type != Type.SOURCE;

        this.roadSegment = roadSegment;
    }

    /**
     * Returns this traffic source's source road segment.
     * @return this traffic source's source road segment
     */
    public final RoadSegment sourceRoad() {
        // for TrafficSinks and similar roadSegment is the source road
        assert type != Type.SOURCE;
        return roadSegment;
    }
}
