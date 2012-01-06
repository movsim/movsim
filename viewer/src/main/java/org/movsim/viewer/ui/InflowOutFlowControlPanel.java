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
package org.movsim.viewer.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.movsim.viewer.graphics.GraphicsConfigurationParameters;

/**
 * @author ralph
 * 
 */
public class InflowOutFlowControlPanel extends JPanel implements ChangeListener {

    /**
     * 
     */
    private static final long serialVersionUID = 8857745466845163347L;

    final String LABEL_INFLOW_MAINROAD = "Inflow highway";
    final String LABEL_INFLOW_RAMP = "Inflow onramp";
    final String LABEL_OFFRAMP = "Fraction of detour";
    final String LABEL_TRAFFICLIGHT = "Traffic light control";

    private final JSlider inFlowMainRoad;
    private final JSlider inFlowOnramp;
    private final JSlider fractionToOfframp;
    private final JSlider trafficLightControl;

    /**
     * @param movsimViewerFacade
     */
    public InflowOutFlowControlPanel() {

        this.setLayout(new FlowLayout());
        this.setBackground(GraphicsConfigurationParameters.BACKGROUND_COLOR_SIM);

        inFlowMainRoad = new JSlider(SwingConstants.HORIZONTAL, 0, 2000, 0);
        inFlowMainRoad.addChangeListener(this);
        // Turn on labels at major tick marks.
        inFlowMainRoad.setMajorTickSpacing(500);
        inFlowMainRoad.setMinorTickSpacing(50);
        inFlowMainRoad.setPaintTicks(true);
        inFlowMainRoad.setPaintLabels(true);
        add(new JLabel(LABEL_INFLOW_MAINROAD));
        add(inFlowMainRoad);

        add(Box.createRigidArea(new Dimension(20, 22)));

        inFlowOnramp = new JSlider(SwingConstants.HORIZONTAL, 0, 800, 0);
        inFlowOnramp.addChangeListener(this);
        inFlowOnramp.setMajorTickSpacing(200);
        inFlowOnramp.setMinorTickSpacing(20);
        inFlowOnramp.setPaintTicks(true);
        inFlowOnramp.setPaintLabels(true);
        add(new JLabel(LABEL_INFLOW_RAMP));
        add(inFlowOnramp);

        add(Box.createRigidArea(new Dimension(20, 22)));

        fractionToOfframp = new JSlider(SwingConstants.HORIZONTAL, 0, 30, 0);
        fractionToOfframp.addChangeListener(this);
        fractionToOfframp.setMajorTickSpacing(10);
        fractionToOfframp.setMinorTickSpacing(1);
        fractionToOfframp.setPaintTicks(true);
        fractionToOfframp.setPaintLabels(true);
        add(new JLabel(LABEL_OFFRAMP));
        add(fractionToOfframp);

        add(Box.createRigidArea(new Dimension(20, 22)));

        // traffic light controller
        trafficLightControl = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 0);
        trafficLightControl.addChangeListener(this);
        trafficLightControl.setMajorTickSpacing(20);
        trafficLightControl.setMinorTickSpacing(4);
        trafficLightControl.setPaintTicks(true);
        trafficLightControl.setPaintLabels(true);
        add(new JLabel(LABEL_TRAFFICLIGHT));
        add(trafficLightControl);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent )
     */
    @Override
    public void stateChanged(ChangeEvent e) {
//        final JSlider source = (JSlider) e.getSource();
//        final MovsimControllers movsimControllers = MovsimControllers.getInstance();
//        if (!source.getValueIsAdjusting()) {
//            final int value = source.getValue();
//            if (source == inFlowMainRoad) {
//                System.out.println("stateChanged: new slider value for flowPerLane mainroads: " + value);
//                movsimControllers.setInflowControlMainroads(value / 3600.);
//            } else if (source == inFlowOnramp) {
//                System.out.println("stateChanged: new slider value for flowPerLane onramps: " + value);
//                movsimControllers.setInflowControlOnramps(value / 3600.);
//            } else if (source == fractionToOfframp) {
//                System.out.println("fraction to offramp (per cent): " + value);
//                movsimControllers.setOutflowFractionMainroads(value / 100.0);
//            } else if (source == trafficLightControl) {
//                System.out.println("trafficlight control of relative red phase (per cent): " + value);
//                movsimControllers.setRelativeRedPhaseOfTrafficLight(value / 100.0);
//            }
//        }
    }

    public void reset() {
//        final MovsimControllers movsimControllers = MovsimControllers.getInstance();
//
//        // inflow mainroads
//        final double inflowMainPerLane = movsimControllers.getInitFlowPerLaneMainroads();
//        movsimControllers.setInflowControlMainroads(inflowMainPerLane);
//        inFlowMainRoad.setValue((int) (3600 * inflowMainPerLane));
//
//        // inflow mainroads
//        final double inflowOnrampPerLane = movsimControllers.getInitFlowPerLaneOnramps();
//        movsimControllers.setInflowControlOnramps(inflowOnrampPerLane);
//        inFlowOnramp.setValue((int) (3600 * inflowOnrampPerLane));
//
//        final double initFraction = movsimControllers.getInitFractionToOfframp();
//        movsimControllers.setOutflowFractionMainroads(initFraction);
//        fractionToOfframp.setValue((int) (initFraction * 100));
//
//        final double initRelativeRedPhase = movsimControllers.getInitRelRedPhaseOfTrafficLight();
//        movsimControllers.setRelativeRedPhaseOfTrafficLight(initRelativeRedPhase);
//        trafficLightControl.setValue((int) (initRelativeRedPhase * 100));

    }

}
