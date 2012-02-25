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
//import java.awt.Font;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.ResourceBundle;
//
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JCheckBox;
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JPanel;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.plot.Plot;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYItemRenderer;
//import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.RectangleEdge;
//import org.movsim.output.LoopDetector;
//import org.movsim.simulator.Simulator;
//import org.movsim.utilities.ObserverInTime;
//import org.movsim.viewer.graphics.charts.model.DetectorDataPoint;
//import org.movsim.viewer.util.SwingHelper;
//
///**
// * @author ralph
// * 
// */
//public class DetectorsView extends JFrame implements ObserverInTime, ActionListener {
//
//    private List<LoopDetector> loopDetectors = new ArrayList<LoopDetector>();
//    private final List<String> detectorNames = new ArrayList<String>();
//    private final Map<String, List<DetectorDataPoint>> hashDetectors = new HashMap<String, List<DetectorDataPoint>>();
//    private ChartPanel detectorChartPanel;
//    private int numberOfDetectors;
//    private XYSeries[] series;
//    private Plot plot;
//    private final double scaleCA;
//
//    public DetectorsView(ResourceBundle resourceBundle, JCheckBoxMenuItem cb) {
//        this.setLayout(new BorderLayout());
//        setLocation(10, 700);
//
//        scaleCA = 1; // TODO
//        final Simulator simulator = Simulator.getInstance();
//
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent evnt) {
//                cb.setSelected(false);
//                evnt.getWindow().setVisible(false);
//                evnt.getWindow().dispose();
//            }
//        });
//
//        if (simulator.getSimObservables().getLoopDetectors() != null) {
//            loopDetectors = simulator.getSimObservables().getLoopDetectors();
//            for (final LoopDetector loopDet : loopDetectors) {
//                loopDet.registerObserver(this);
//                final double xDetectorInt = ((loopDet.getDetPosition() * scaleCA / 1000));
//                final String s = String.valueOf(xDetectorInt) + " km";
//                detectorNames.add(s);
//                final List<DetectorDataPoint> detectorDataPoints = new ArrayList<DetectorDataPoint>();
//                hashDetectors.put(s, detectorDataPoints);
//            }
//
//            numberOfDetectors = detectorNames.size();
//
//            if (numberOfDetectors == 0) {
//                System.out.println("No detectors"); // TODO Message
//            }
//
//            series = new XYSeries[numberOfDetectors];
//            for (int i = 0; i < numberOfDetectors; i++) {
//                series[i] = new XYSeries(String.valueOf(detectorNames.get(i)));
//                series[i].add(0, 0);
//            }
//
//            final JFreeChart chart = createChart();
//            detectorChartPanel = new ChartPanel(chart);
//            detectorChartPanel.setSize(440, 280);
//
//            detectorChartPanel.setMouseWheelEnabled(true);
//
//            // Hide detector
//            final JPanel checkBoxpanel = new JPanel();
//            checkBoxpanel.setLayout(new BoxLayout(checkBoxpanel, BoxLayout.Y_AXIS));
//
//            final Font f = new Font("Dialog", Font.BOLD, 12);
//            final JLabel lblCheckboxPanel = new JLabel("Loop detectors:");
//
//            checkBoxpanel.add(lblCheckboxPanel);
//
//            for (int i = 0; i < numberOfDetectors; i++) {
//                final JCheckBox jcheckbox = new JCheckBox(String.valueOf(detectorNames.get(i)));
//                jcheckbox.setActionCommand(String.valueOf(detectorNames.get(i)));
//                jcheckbox.addActionListener(this);
//                if ((i == 4) || (i == 8) || (i == 12) || (i == 1)) {
//                    jcheckbox.setSelected(true);
//                } else {
//                    jcheckbox.setSelected(false);
//                }
//
//                checkBoxpanel.add(jcheckbox);
//                checkBoxpanel.add(Box.createVerticalGlue());
//            }
//
//            add(checkBoxpanel, BorderLayout.EAST);
//
//            final XYItemRenderer renderer = ((XYPlot) plot).getRenderer();
//            for (int i = 0; i < numberOfDetectors; i++) {
//                if (!((i == 4) || (i == 8) || (i == 12) || (i == 1))) {
//                    if (i >= 0) {
//                        final boolean flag = renderer.getItemVisible(i, 0);
//                        renderer.setSeriesVisible(i, new Boolean(!flag));
//                    }
//                }
//            }
//
//            SwingHelper.setComponentSize(detectorChartPanel, 400, 280);
//            add(detectorChartPanel, BorderLayout.CENTER);
//
//            pack();
//            setVisible(true);
//        }
//    }
//
//    /**
//     * @return
//     */
//    private JFreeChart createChart() {
//        final JFreeChart chart = ChartFactory.createScatterPlot(null, "Density (vehicles/km)", "Flow (vehicles/h)",
//                createDataSerie(), PlotOrientation.VERTICAL, true, true, false);
//
//        chart.setBackgroundPaint(getBackground());
//
//        plot = chart.getPlot();
//
//        final NumberAxis rangeAxis = (NumberAxis) ((XYPlot) plot).getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final NumberAxis domainAxis = (NumberAxis) ((XYPlot) plot).getDomainAxis();
//        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final LegendTitle legend = chart.getLegend();
//        legend.setPosition(RectangleEdge.RIGHT);
//        return chart;
//    }
//
//    /**
//     * @return
//     */
//    private XYDataset createDataSerie() {
//        final XYSeriesCollection dataSet = new XYSeriesCollection();
//        for (final XYSeries se : series) {
//            dataSet.addSeries(se);
//        }
//        return dataSet;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
//     */
//    @Override
//    public void notifyObserver(double time) {
//        if (loopDetectors.size() > 0) {
//            pullLoopDetectorData(time);
//            updateView();
//        }
//    }
//
//    private void updateView() {
//        // Add Data to XYSeries
//        for (int i = 0; i < numberOfDetectors; i++) {
//            addDataToSerie(series[i], detectorNames.get(i));
//        }
//        this.repaint();
//    }
//
//    private void addDataToSerie(XYSeries serie, String string) {
//        final List<DetectorDataPoint> listData = hashDetectors.get(string);
//
//        final DetectorDataPoint detectorDataPoint = listData.get(listData.size() - 1);
//        serie.add(detectorDataPoint.getDensity(), detectorDataPoint.getFlow());
//    }
//
//    private void pullLoopDetectorData(double time) {
//        for (final LoopDetector det : loopDetectors) {
//            final double xDetectorInt = det.getDetPosition() * scaleCA / 1000;
//            final String s = String.valueOf(xDetectorInt) + " km";
//            final List<DetectorDataPoint> list = hashDetectors.get(s);
//            list.add(new DetectorDataPoint(time, det.getFlow() * 3600, det.getDensityArithmetic() * 1000 * scaleCA, det
//                    .getMeanSpeed() * 3.6));
//
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
//     */
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        final XYItemRenderer renderer = ((XYPlot) plot).getRenderer();
//        for (int i = 0; i < numberOfDetectors; i++) {
//            if (e.getActionCommand().equals(String.valueOf(detectorNames.get(i)))) {
//                if (i >= 0) {
//                    final boolean flag = renderer.getItemVisible(i, 0);
//                    renderer.setSeriesVisible(i, new Boolean(!flag));
//                }
//            }
//        }
//    }
//
//}
