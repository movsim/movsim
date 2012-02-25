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
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
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
//import org.jfree.chart.axis.NumberTickUnit;
//import org.jfree.chart.axis.TickUnitSource;
//import org.jfree.chart.axis.TickUnits;
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
//public class FloatingCarsAccelerationView extends JFrame implements ActionListener, ObserverInTime, Runnable {
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
//    private final ExecutorService exec;
//
//    private double time;
//
//    /**
//     * @param floatingCarPanel
//     * @param simulator
//     */
//    public FloatingCarsAccelerationView() {
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
//            System.out.println("error, expected floating_cars xml configuration for use case here "); // TODO handle
//        } else {
//            floatingCars.registerObserver(this);
//            listOfFloatingCars = floatingCars.getFcdList();
//            floatingCarsDataPoints = new HashMap<Integer, List<FloatingCarDataPoint>>();
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
//        setxAxis("x"); // TODO Label acc
//        setyAxis("Y");
//        final JFreeChart chart = createChart();
//
//        chartPanel = new ChartPanel(chart, true, false, false, true, true);
//
//        SwingHelper.setComponentSize(chartPanel, 200, 100);
//
//        chartPanel.setMouseWheelEnabled(true);
//
//        // chartPanel.setHorizontalAxisTrace(true);
//        // chartPanel.setVerticalAxisTrace(true);
//
//        // Hide FC checkboxes
//        final JPanel checkBoxpanel = new JPanel();
//
//        lblTraveledDistance = new JLabel[numberOfFloatingCars];
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            lblTraveledDistance[i] = new JLabel("0 m/\u00B2");
//            SwingHelper.setComponentSize(lblTraveledDistance[i], 68, 22);
//        }
//
//        checkBoxpanel.setLayout(new BoxLayout(checkBoxpanel, BoxLayout.PAGE_AXIS));
//
//        checkBoxpanel.add(Box.createRigidArea(new Dimension(10, 40)));
//
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            final Integer fc = floatingCarsList.get(i);
//            final JCheckBox jcheckbox = new JCheckBox("Car " + String.valueOf(fc - 1));
//            jcheckbox.setActionCommand(String.valueOf(fc));
//            jcheckbox.addActionListener(this);
//            jcheckbox.setSelected(true);
//
//            checkBoxpanel.add(jcheckbox);
//            checkBoxpanel.add(lblTraveledDistance[i]);
//
////            final Vehicle floatingCar = floatingCars.getVehicleContainers().get(0).getVehicles().get(fc);
////            lblTraveledDistance[i].setText(String.valueOf(floatingCar.physicalQuantities().getAcc() + " m/s\u00B2  "));
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
//        format = new DecimalFormat("#.##");
//
//        exec = Executors.newCachedThreadPool();
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
//    public void notifyObserver(final double time) {
//        this.time = time;
//        if (floatingCars != null) {
//
//            // new Thread(new Runnable() {
//            //
//            // @Override
//            // public void run() {
//            // // TODO Auto-generated method stub
//            // pullFloatingCarsData(time);
//            //
//            // updateView();
//            // }
//            // }).start();
//            exec.execute(this);
//
//        }
//    }
//
//    private void updateView() {
//        for (int i = 0; i < numberOfFloatingCars; i++) {
//            addDataToSerieAndNextToChart(series[i], floatingCarsList.get(i), i);
//
//        }
//
//        System.out.println("updateview() Thread: " + Thread.currentThread().getName());
//        // this.repaint();
//    }
//
//    private void addDataToSerieAndNextToChart(XYSeries se, int fc, int i) {
//        final List<FloatingCarDataPoint> accData = floatingCarsDataPoints.get(fc);
//
//        final double yAx = accData.get(accData.size() - 1).getAcc(); // gets the last added. concurrency problem!
//        final double time = accData.get(accData.size() - 1).getTime();
//        se.add(time, yAx);
//        lblTraveledDistance[i].setText(String.valueOf(format.format(yAx) + " m/s\u00B2  "));
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
//        rangeAxis.setStandardTickUnits(createAccelerationTickUnits());
//
//        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
//        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final double duration = simulator.getSimInput().getSimulationInput().getMaxSimTime();
//        final ValueAxis vDomainAxis = plot.getDomainAxis();
//        domainAxis.setRange(0, duration);
//
//        // plot.setBackgroundPaint(Color.WHITE);
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
////            } // TODO
//
//        }
//        System.out.println("pulldate Thread :" + Thread.currentThread().getName());
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
//            if (e.getActionCommand().equals(String.valueOf(floatingCarsList.get(i)))) {
//                if (i >= 0) {
//                    final boolean flag = renderer.getItemVisible(i, 0);
//                    renderer.setSeriesVisible(i, new Boolean(!flag));
//                }
//            }
//        }
//        if (e.getActionCommand().equals("dp")) {
//            flagDp = !flagDp;
//            final XYLineAndShapeRenderer rendererLS = (XYLineAndShapeRenderer) plot.getRenderer();
//            rendererLS.setShapesVisible(flagDp);
//            rendererLS.setShapesFilled(flagDp);
//        }
//
//    }
//
//    public static TickUnitSource createAccelerationTickUnits() {
//
//        final TickUnits units = new TickUnits();
//        final DecimalFormat df0 = new DecimalFormat("0.0");
//        final DecimalFormat df1 = new DecimalFormat("0.0");
//        final DecimalFormat df2 = new DecimalFormat("0.0");
//        final DecimalFormat df3 = new DecimalFormat("0.0");
//        final DecimalFormat df4 = new DecimalFormat("0.0");
//        final DecimalFormat df5 = new DecimalFormat("0.0");
//        final DecimalFormat df6 = new DecimalFormat("0.0");
//        final DecimalFormat df7 = new DecimalFormat("0.0");
//        final DecimalFormat df8 = new DecimalFormat("0.0");
//        final DecimalFormat df9 = new DecimalFormat("0.0");
//        final DecimalFormat df10 = new DecimalFormat("0.0");
//
//        // we can add the units in any order, the MyTickUnits collection will
//        // sort them...
//        units.add(new NumberTickUnit(0.0000001, df1, 2));
//        units.add(new NumberTickUnit(0.000001, df2, 2));
//        units.add(new NumberTickUnit(0.00001, df3, 2));
//        units.add(new NumberTickUnit(0.0001, df4, 2));
//        units.add(new NumberTickUnit(0.001, df5, 2));
//        units.add(new NumberTickUnit(0.01, df6, 2));
//        units.add(new NumberTickUnit(0.1, df7, 2));
//        units.add(new NumberTickUnit(1, df8, 2));
//        units.add(new NumberTickUnit(10, df8, 2));
//        units.add(new NumberTickUnit(100, df8, 2));
//        units.add(new NumberTickUnit(1000, df8, 2));
//        units.add(new NumberTickUnit(10000, df8, 2));
//        units.add(new NumberTickUnit(100000, df8, 2));
//        units.add(new NumberTickUnit(1000000, df9, 2));
//        units.add(new NumberTickUnit(10000000, df9, 2));
//        units.add(new NumberTickUnit(100000000, df9, 2));
//        units.add(new NumberTickUnit(1000000000, df10, 2));
//        units.add(new NumberTickUnit(10000000000.0, df10, 2));
//        units.add(new NumberTickUnit(100000000000.0, df10, 2));
//
//        units.add(new NumberTickUnit(0.00000025, df0, 5));
//        units.add(new NumberTickUnit(0.0000025, df1, 5));
//        units.add(new NumberTickUnit(0.000025, df2, 5));
//        units.add(new NumberTickUnit(0.00025, df3, 5));
//        units.add(new NumberTickUnit(0.0025, df4, 5));
//        units.add(new NumberTickUnit(0.025, df5, 5));
//        units.add(new NumberTickUnit(0.25, df6, 5));
//        units.add(new NumberTickUnit(2.5, df7, 5));
//        units.add(new NumberTickUnit(25, df8, 5));
//        units.add(new NumberTickUnit(250, df8, 5));
//        units.add(new NumberTickUnit(2500, df8, 5));
//        units.add(new NumberTickUnit(25000, df8, 5));
//        units.add(new NumberTickUnit(250000, df8, 5));
//        units.add(new NumberTickUnit(2500000, df9, 5));
//        units.add(new NumberTickUnit(25000000, df9, 5));
//        units.add(new NumberTickUnit(250000000, df9, 5));
//        units.add(new NumberTickUnit(2500000000.0, df10, 5));
//        units.add(new NumberTickUnit(25000000000.0, df10, 5));
//        units.add(new NumberTickUnit(250000000000.0, df10, 5));
//
//        units.add(new NumberTickUnit(0.0000005, df1, 5));
//        units.add(new NumberTickUnit(0.000005, df2, 5));
//        units.add(new NumberTickUnit(0.00005, df3, 5));
//        units.add(new NumberTickUnit(0.0005, df4, 5));
//        units.add(new NumberTickUnit(0.005, df5, 5));
//        units.add(new NumberTickUnit(0.05, df6, 5));
//        units.add(new NumberTickUnit(0.5, df7, 5));
//        units.add(new NumberTickUnit(5L, df8, 5));
//        units.add(new NumberTickUnit(50L, df8, 5));
//        units.add(new NumberTickUnit(500L, df8, 5));
//        units.add(new NumberTickUnit(5000L, df8, 5));
//        units.add(new NumberTickUnit(50000L, df8, 5));
//        units.add(new NumberTickUnit(500000L, df8, 5));
//        units.add(new NumberTickUnit(5000000L, df9, 5));
//        units.add(new NumberTickUnit(50000000L, df9, 5));
//        units.add(new NumberTickUnit(500000000L, df9, 5));
//        units.add(new NumberTickUnit(5000000000L, df10, 5));
//        units.add(new NumberTickUnit(50000000000L, df10, 5));
//        units.add(new NumberTickUnit(500000000000L, df10, 5));
//
//        return units;
//
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.lang.Runnable#run()
//     */
//    @Override
//    public void run() {
//        pullFloatingCarsData(time);
//        updateView();
//    }
//}
