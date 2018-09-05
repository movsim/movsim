package org.movsim.simulator;

import com.google.common.base.Preconditions;
import org.movsim.scenario.initial.autogen.*;
import org.movsim.simulator.roadnetwork.LaneSegment;
import org.movsim.simulator.roadnetwork.Lanes;
import org.movsim.simulator.roadnetwork.RoadNetwork;
import org.movsim.simulator.roadnetwork.RoadSegment;
import org.movsim.simulator.vehicles.TestVehicle;
import org.movsim.simulator.vehicles.TrafficCompositionGenerator;
import org.movsim.simulator.vehicles.Vehicle;
import org.movsim.utilities.LinearInterpolatedFunction;
import org.movsim.utilities.Units;
import org.movsim.xml.InputLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InitialConditions {

    private static final Logger LOG = LoggerFactory.getLogger(InitialConditions.class);

    private final Set<String> alreadyHandled;

    private final File file;

    private final MovsimInitialConditions movsimInitialConditions;

    public InitialConditions(File file) {
        this.file = Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists(), "initial conditions file " + file + " not found");
        alreadyHandled = new HashSet<>();

        movsimInitialConditions = InputLoader.unmarshallInitialConditions(file);

        LOG.info("loaded initial conditions from file={}", file);
        LOG.info("unmarshalled initial conditions for {} roads",
                movsimInitialConditions.getRoadInitialConditions().size());
    }

    public void setInitialConditions(RoadNetwork roadNetwork, TrafficCompositionGenerator defaultComposition) {
        for (RoadInitialConditionsType roadIC : movsimInitialConditions.getRoadInitialConditions()) {
            String roadId = roadIC.getId();
            RoadSegment roadSegment = roadNetwork.findByUserId(roadId);

            if (roadSegment == null) {
                // TODO improve error reporting LOG.error
                throw new IllegalArgumentException(
                        "wrong input in " + file + " : road with user Id " + roadId + " not defined in road network");
            }

            if (alreadyHandled.contains(roadId)) {
                throw new IllegalArgumentException(
                        "wrong input in " + file + " : road with user Id " + roadId + " defined twice in input file");
            }

            alreadyHandled.add(roadId);

            TrafficCompositionGenerator trafficComposition = roadSegment.hasTrafficComposition() ?
                    roadSegment.getTrafficComposition() :
                    defaultComposition;

            if (roadIC.isSetMacroscopicInitialConditions()) {
                setMacroscopicInitialConditions(roadSegment, roadIC.getMacroscopicInitialConditions(),
                        trafficComposition);
            } else if (roadIC.isSetMicroscopicInitialConditions()) {
                setMicroscopicInitialConditions(roadSegment, roadIC.getMicroscopicInitialConditions(),
                        trafficComposition);
            } else {
                LOG.warn("no initial conditions defined for roadSegment={}", roadSegment.userId());
            }
        }
    }

    /**
     * Determine vehicle positions on all relevant lanes while considering minimum gaps to avoid accidents. Gaps are left at the
     * beginning and the end of the road segment on purpose. However, the consistency check is not complete and other segments
     * are not considered.
     *
     * @param roadSegment
     * @param macroInitialConditions
     * @param trafficComposition
     */
    private void setMacroscopicInitialConditions(RoadSegment roadSegment,
            MacroscopicInitialConditionsType macroInitialConditions, TrafficCompositionGenerator trafficComposition) {

        LOG.info("set macro initial conditions: generate vehicles from macro-localDensity ");
        final InitialConditionsMacro icMacro = new InitialConditionsMacro(macroInitialConditions.getMacroCondition());

        for (LaneSegment laneSegment : roadSegment.laneSegments()) {
            if (laneSegment.type() != Lanes.Type.TRAFFIC) {
                LOG.debug("no macroscopic initial conditions for non-traffic lanes (slip roads etc).");
                continue;
            }

            double position = roadSegment.roadLength(); // start at end of segment
            while (position > 0) {
                final TestVehicle testVehicle = trafficComposition.getTestVehicle();

                final double rhoLocal = icMacro.rho(position);
                double speedInit = icMacro.hasUserDefinedSpeeds() ?
                        icMacro.vInit(position) :
                        testVehicle.getEquilibriumSpeed(rhoLocal);
                if (LOG.isDebugEnabled() && !icMacro.hasUserDefinedSpeeds()) {
                    LOG.debug("use equilibrium speed={} in macroscopic initial conditions.", speedInit);
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format(
                            "macroscopic init conditions from input: roadId=%s, x=%.3f, rho(x)=%.3f/km, speed=%.2fkm/h",
                            roadSegment.id(), position, Units.INVM_TO_INVKM * rhoLocal, Units.MS_TO_KMH * speedInit));
                }

                if (rhoLocal <= 0) {
                    LOG.debug("no vehicle added at x={} for vanishing initial localDensity={}.", position, rhoLocal);
                    position -= 50; // move on in upstream direction
                    continue;
                }

                final Vehicle veh = trafficComposition.createVehicle(testVehicle);
                final double meanDistanceInLane = 1. / (rhoLocal + MovsimConstants.SMALL_VALUE);
                // TODO icMacro for ca
                // final double minimumGap = veh.getLongitudinalModel().isCA() ? veh.getLength() : veh.getLength() +
                // veh.getLongitudinalModel().getS0();
                final double minimumGap = veh.getLength() + veh.getLongitudinalModel().getMinimumGap();
                final double posDecrement = Math.max(meanDistanceInLane, minimumGap);
                position -= posDecrement;

                if (position <= posDecrement) {
                    LOG.debug("leave minimum gap at origin of road segment and start with next lane, pos={}", position);
                    break;
                }
                final Vehicle leader = laneSegment.rearVehicle();
                final double gapToLeader = (leader == null) ?
                        MovsimConstants.GAP_INFINITY :
                        leader.getRearPosition() - position;

                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("meanDistance=%.3f, minimumGap=%.2f, posDecrement=%.3f, gapToLeader=%.3f%n",
                            meanDistanceInLane, minimumGap, posDecrement, gapToLeader));
                }

                if (gapToLeader > 0) {
                    veh.setFrontPosition(position);
                    veh.setSpeed(speedInit);
                    veh.setLane(laneSegment.lane());
                    LOG.debug("add vehicle from macroscopic initial conditions at pos={} with speed={}.", position,
                            speedInit);
                    roadSegment.addVehicle(veh);
                } else {
                    LOG.debug("cannot add vehicle due to gap constraints at pos={} with speed={}.", position,
                            speedInit);
                }

            }
        }
    }

    private void setMicroscopicInitialConditions(RoadSegment roadSegment,
            MicroscopicInitialConditionsType initialMicroConditions, TrafficCompositionGenerator trafficComposition) {
        LOG.debug(("set microscopic initial conditions"));

        int vehicleNumber = 1;
        for (final VehicleInitialConditionType ic : initialMicroConditions.getVehicleInitialCondition()) {
            // TODO counter
            final Vehicle veh = ic.isSetLabel() ?
                    trafficComposition.createVehicle(ic.getLabel()) :
                    trafficComposition.createVehicle();
            veh.setVehNumber(vehicleNumber++);
            // test-wise:
            veh.setFrontPosition(Math.round(ic.getPosition() / veh.physicalQuantities().getxScale()));
            veh.setSpeed(Math.round(ic.getSpeed() / veh.physicalQuantities().getvScale()));
            final int lane = ic.getLane();
            if (lane < Lanes.MOST_INNER_LANE || lane > roadSegment.laneCount()) {
                throw new IllegalArgumentException(
                        "lane=" + lane + " given in initial condition does not exist for road=" + roadSegment.id()
                                + " which has a laneCount of " + roadSegment.laneCount());
            }
            veh.setLane(lane);
            roadSegment.addVehicle(veh);
            if (LOG.isInfoEnabled()) {
                LOG.info(String.format("set vehicle with label = %s on lane=%d with front at x=%.2f, speed=%.2f",
                        veh.getLabel(), veh.lane(), veh.getFrontPosition(), veh.getSpeed()));
                if (veh.getLongitudinalModel().isCA()) {
                    LOG.info(
                            String.format("and for the CA in physical quantities: front position at x=%.2f, speed=%.2f",
                                    veh.physicalQuantities().getFrontPosition(), veh.physicalQuantities().getSpeed()));
                }
            }
        }
    }

    private static class InitialConditionsMacro {

        /**
         * The density profile in 1/m
         */
        private LinearInterpolatedFunction rhoFct;

        /**
         * the speeds along the road segment in m/s. Only initialized when initial speeds are provided.
         */
        private LinearInterpolatedFunction speedFct;

        /**
         * Instantiates a new initial conditions macro.
         */
        public InitialConditionsMacro(List<MacroConditionType> macroConditions) {
            createFunctions(macroConditions);
        }

        private void createFunctions(List<MacroConditionType> macroConditions) {
            final int size = macroConditions.size();
            double[] positions = new double[size];
            double[] densities = new double[size];
            double[] speeds = new double[size];

            for (int i = 0; i < size; i++) {
                MacroConditionType localMacroIC = macroConditions.get(i);
                final double rhoLocal = localMacroIC.getDensityPerKm() * Units.INVKM_TO_INVM;
                if (rhoLocal > MovsimConstants.SMALL_VALUE) {
                    positions[i] = localMacroIC.getPosition();
                    densities[i] = rhoLocal;
                    if (useUserDefinedSpeeds(macroConditions)) {
                        speeds[i] = Math.min(localMacroIC.getSpeed(), MovsimConstants.MAX_VEHICLE_SPEED);
                        LOG.debug("speed={}", speeds[i]);
                    }
                }
            }

            rhoFct = new LinearInterpolatedFunction(positions, densities);
            if (useUserDefinedSpeeds(macroConditions)) {
                speedFct = new LinearInterpolatedFunction(positions, speeds);
            }
        }

        private boolean useUserDefinedSpeeds(List<MacroConditionType> macroConditions) {
            boolean userDefinedSpeed = true;

            for (int i = 0, N = macroConditions.size(); i < N; i++) {
                if (i == 0) {
                    // set initial value
                    userDefinedSpeed = macroConditions.get(i).isSetSpeed();
                }
                if (macroConditions.get(i).isSetSpeed() != userDefinedSpeed) {
                    throw new IllegalArgumentException(
                            "decide whether equilibrium speed or user-defined speeds should be used. Do not mix the speed input!");
                }
            }

            return userDefinedSpeed;
        }

        public boolean hasUserDefinedSpeeds() {
            return speedFct != null;
        }

        public double vInit(double x) {
            Preconditions
                    .checkNotNull(speedFct, "expected usage of equilibrium speeds, check with hasUserDefinedSpeeds");
            return speedFct.value(x);
        }

        public double rho(double x) {
            return rhoFct.value(x);
        }
    }
}
