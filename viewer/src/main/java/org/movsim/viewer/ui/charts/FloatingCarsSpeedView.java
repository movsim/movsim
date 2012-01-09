package org.movsim.viewer.ui.charts;
///**
// * Copyright (C) 2010, 2011 by Arne Kesting, Martin Treiber,
// *                             Ralph Germ, Martin Budden
// *                             <movsim@akesting.de>
// * ----------------------------------------------------------------------
// * 
// *  This file is part of 
// *  
// *  MovSim - the multi-model open-source vehicular-traffic simulator 
// *
// *  MovSim is free software: you can redistribute it and/or modify
// *  it under the terms of the GNU General Public License as published by
// *  the Free Software Foundation, either version 3 of the License, or
// *  (at your option) any later version.
// *
// *  MovSim is distributed in the hope that it will be useful,
// *  but WITHOUT ANY WARRANTY; without even the implied warranty of
// *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// *  GNU General Public License for more details.
// *
// *  You should have received a copy of the GNU General Public License
// *  along with MovSim.  If not, see <http://www.gnu.org/licenses/> or
// *  <http://www.movsim.org>.
// *  
// * ----------------------------------------------------------------------
// */
//package org.movsim.viewer.graphics.charts;
//
//import java.awt.BorderLayout;
//import java.awt.Dimension;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.text.DecimalFormat;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JCheckBox;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.ValueAxis;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
//import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.RectangleEdge;
//import org.movsim.output.FloatingCars;
//import org.movsim.simulator.Simulator;
//import org.movsim.utilities.ObserverInTime;
//import org.movsim.viewer.graphics.charts.model.FloatingCarDataPoint;
//import org.movsim.viewer.util.SwingHelper;
//
///**
// * @author ralph
// * 
// */
//public class FloatingCarsSpeedView extends JFrame implements ActionListener, ObserverInTime {
//
//    private final FloatingCars floatingCars;
//
//    private List<Integer> listOfFloatingCars;
//
//    private Map<Integer, List<FloatingCarDataPoint>> floatingCarsDataPoints;
//
//    private final ChartPanel chartPanel;
//
//    private String titleChart;
//
//    private String xAxis;
//
//    private String yAxis;
//
//    private List<Integer> floatingCarsList;
//
//    private int numberOfFloatingCars;
//
//    private XYSeries series[];
//
//    private XYPlot plot;
//
//    private Boolean flagDp = false;
//
//    private final JLabel[] lblTraveledDistance;
//
//    private final Simulator simulator;
//
//    private final DecimalFormat format;
//
//    /**
//     * @param floatingCarPanel
//     * @param simulator
//     */
//    public FloatingCarsSpeedView() {
//        this.simulator = Simulator.getInstance();
//        this.setLayout(new BorderLayout());
//
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent evnt) {
//                // removeObserver();
//                // cbMenu.setSelected(false);
//                evnt.getWindow().setVisible(false);
//                evnt.getWindow().dispose();
//            }
//        });
//
//        floatingCars = simulator.getSimObservables().getFloatingCars();
//        if (floatingCars == null) {
//            System.out.println("error, expected floating_cars xml configuration for use case here ");
//        } else {
//            floatingCars.registerObserver(this);
//            listOfFloatingCars = floatingCars.getFcdList();
//            floatingCarsDataPoints = new HashMap<Integer, List<FloatingCarDataPoint>>();
//            System.out.println("Alles klar fc");
//            floatingCarsList = floatingCars.getFcdList();
//            numberOfFloatingCars = floatingCarsList.size();
//            series = new XYSeries[numberOfFloatingCars];
//            for (int i = 0; i < numberOfFloatingCars; i++) {
//                series[i] = new XYSeries(String.valueOf(floatingCarsList.get(i) - 1));
//                series[i].add(0, 0);
//            }
//        }
//
//        setTitleChart("Floating Cars");
//        setxAxis("x"); // TODO label speed
//        setyAxis("y");
//        final JFreeChart chart = createChart();
//        chartPanel = new ChartPanel(chart, true, false, false, true, true);
//
//        SwingHelper.setComponentSize(chartPanel, 200, 100);
//
//        chartPanel.setMouseWheelEnabled(true);
//
//        lblTraveledDistance = new JLabel[numberOfFloatingCars];
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            lblTraveledDistance[i] = new JLabel("0 km/h");
//            SwingHelper.setComponentSize(lblTraveledDistance[i], 68, 22);
//        }
//        // Hide FC
//        final JPanel checkBoxpanel = new JPanel();
//        checkBoxpanel.setLayout(new BoxLayout(checkBoxpanel, BoxLayout.Y_AXIS));
//        checkBoxpanel.add(Box.createRigidArea(new Dimension(10, 40)));
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            final JCheckBox jcheckbox = new JCheckBox("Car " + String.valueOf(floatingCarsList.get(i) - 1));
//            jcheckbox.setActionCommand(String.valueOf(floatingCarsList.get(i)));
//            jcheckbox.addActionListener(this);
//            jcheckbox.setSelected(true);
//
//            checkBoxpanel.add(jcheckbox);
//            checkBoxpanel.add(lblTraveledDistance[i]);
//
//            final Integer fc = listOfFloatingCars.get(i);
////            final Vehicle floatingCar = floatingCars.getVehicleContainers().get(0).getVehicles().get(fc);
////            lblTraveledDistance[i].setText(String.valueOf(floatingCar.physicalQuantities().getSpeed() + " km/h"));
////            checkBoxpanel.add(Box.createVerticalGlue()); //TODO
//        }
//        final JCheckBox rendererCheckBox = new JCheckBox("dp");
//        rendererCheckBox.setActionCommand("dp");
//        rendererCheckBox.addActionListener(this);
//        rendererCheckBox.setSelected(flagDp);
//        checkBoxpanel.add(rendererCheckBox);
//
//        checkBoxpanel.add(Box.createRigidArea(new Dimension(10, 40)));
//
//        add(checkBoxpanel, BorderLayout.EAST);
//        add(chartPanel, BorderLayout.CENTER);
//
//        format = new DecimalFormat("#.#");
//
//        pack();
//        setVisible(true);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
//     */
//    @Override
//    public void notifyObserver(double time) {
//        if (floatingCars != null) {
//            pullFloatingCarsData(time);
//            updateView();
//        }
//    }
//
//    private void updateView() {
//        // Add Data to XYSeries
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            addDataToSerie(series[i], floatingCarsList.get(i), i);
//        }
//        this.repaint();
//    }
//
//    /**
//     * @param i
//     * 
//     */
//    private void addDataToSerie(XYSeries se, int fc, int i) {
//        final List<FloatingCarDataPoint> speedData = floatingCarsDataPoints.get(fc);
//
//        final double yAx = speedData.get(speedData.size() - 1).getSpeed();
//        final double time = speedData.get(speedData.size() - 1).getTime();
//        se.add(time, yAx);
//        lblTraveledDistance[i].setText(String.valueOf(format.format(yAx) + " km/h"));
//    }
//
//    private XYSeriesCollection createDataSerie() {
//        final XYSeriesCollection dataSet = new XYSeriesCollection();
//        for (final XYSeries se : series) {
//            dataSet.addSeries(se);
//        }
//        return dataSet;
//    }
//
//    private JFreeChart createChart() {
//        final JFreeChart chart = ChartFactory.createXYLineChart(null, xAxis, yAxis, createDataSerie(),
//                PlotOrientation.VERTICAL, true, false, false);
//        chart.setBackgroundPaint(getBackground());
//        plot = (XYPlot) chart.getPlot();
//
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
//        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final double duration = simulator.getSimInput().getSimulationInput().getMaxSimTime();
//        final ValueAxis vDomainAxis = plot.getDomainAxis();
//        domainAxis.setRange(0, duration);
//
//        // XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)
//        // plot.getRenderer();
//        // renderer.setShapesVisible(true);
//        // renderer.setShapesFilled(true);
//
//        // smooth chart
//        // chart.getXYPlot().setRenderer(new XYSplineRenderer());
//
//        final LegendTitle legend = chart.getLegend();
//        legend.setPosition(RectangleEdge.RIGHT);
//        return chart;
//
//    }
//
//    private void pullFloatingCarsData(double time) {
//        for (final int fc : listOfFloatingCars) {
////            final Vehicle floatingCar = floatingCars.getVehicleContainers().get(0).getVehicles().get(fc);
////            final List<FloatingCarDataPoint> data = floatingCarsDataPoints.get(fc);
////            if (data == null) {
////                final ArrayList<FloatingCarDataPoint> list = new ArrayList<FloatingCarDataPoint>();
////                list.add(new FloatingCarDataPoint(time, floatingCar.physicalQuantities().getPosition(), floatingCar
////                        .physicalQuantities().getSpeed() * 3.6, floatingCar.physicalQuantities().getAcc()));
////                floatingCarsDataPoints.put(fc, list);
////            } else {
////                floatingCarsDataPoints.get(fc).add(
////                        new FloatingCarDataPoint(time, floatingCar.physicalQuantities().getPosition(), floatingCar
////                                .physicalQuantities().getSpeed() * 3.6, floatingCar.physicalQuantities().getAcc()));
////            }//TODO
//
//        }
//    }
//
//    public String getTitleChart() {
//        return titleChart;
//    }
//
//    public void setTitleChart(String titleChart) {
//        this.titleChart = titleChart;
//    }
//
//    public String getxAxis() {
//        return xAxis;
//    }
//
//    public void setxAxis(String xAxis) {
//        this.xAxis = xAxis;
//    }
//
//    public String getyAxis() {
//        return yAxis;
//    }
//
//    public void setyAxis(String yAxis) {
//        this.yAxis = yAxis;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//
//        final XYItemRenderer renderer = plot.getRenderer();
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            System.out.println("fcin action: " + floatingCarsList.get(i));
//            if (e.getActionCommand().equals(String.valueOf(floatingCarsList.get(i)))) {
//                if (i >= 0) {
//                    final boolean flag = renderer.getItemVisible(i, 0);
//                    renderer.setSeriesVisible(i, new Boolean(!flag));
//                }
//            }
//        }
//        if (e.getActionCommand().equals("dp")) {
//
//            flagDp = !flagDp;
//            final XYLineAndShapeRenderer rendererLS = (XYLineAndShapeRenderer) plot.getRenderer();
//            rendererLS.setShapesVisible(flagDp);
//            rendererLS.setShapesFilled(flagDp);
//        }
//
//    }
//
//}
