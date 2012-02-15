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
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.text.DecimalFormat;
//import java.util.ResourceBundle;
//
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JFrame;
//
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.AxisLocation;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.TickUnitSource;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.PaintScale;
//import org.jfree.chart.renderer.xy.XYBlockRenderer;
//import org.jfree.chart.title.PaintScaleLegend;
//import org.jfree.data.DomainOrder;
//import org.jfree.data.xy.AbstractXYZDataset;
//import org.jfree.ui.RectangleEdge;
//import org.jfree.ui.RectangleInsets;
//import org.movsim.output.SpatioTemporal;
//import org.movsim.simulator.Simulator;
//import org.movsim.utilities.ObserverInTime;
//import org.movsim.viewer.graphics.charts.util.GradientPaintScale;
//import org.movsim.viewer.graphics.charts.util.MyTickUnits;
//import org.movsim.viewer.graphics.charts.util.MyXNumberTickUnit;
//import org.movsim.viewer.graphics.charts.util.MyYNumberTickUnit;
//import org.movsim.viewer.util.SwingHelper;
//
///**
// * @author ralph
// * 
// */
//public class SpatioTemporalView extends JFrame implements ObserverInTime {
//
//    private final Simulator simulator;
//    private final SpatioTemporal spatioTemporal;
//
//    private double dt;
//    private int countXElements;
//    private int length;
//    private double[][] data;
//    private double duration;
//    private int cols;
//    private boolean flagAfterFirstData = false;
//    private ChartPanel spChartPanel;
//    private XYZArrayDataset xyzSet;
//    private double dx;
//    private double roadlength;
//    private final double scaleCA;
//    private double rows;
//
//    public SpatioTemporalView(ResourceBundle resourceBundle, JCheckBoxMenuItem cb) {
//        this.simulator = Simulator.getInstance();
//        this.setLayout(new BorderLayout());
//        setLocation(10, 700);
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent evnt) {
//                cb.setSelected(false);
//                evnt.getWindow().setVisible(false);
//                evnt.getWindow().dispose();
//                removeObserver();
//            }
//        });
//
//        scaleCA = 1; // TODO
//
//        spatioTemporal = simulator.getSimObservables().getSpatioTemporal();
//        if (spatioTemporal == null) {
//            System.out.println("error, expected spatiotemporal xml configuration for use case here ");
//            cb.setSelected(false);
//        } else {
//            spatioTemporal.registerObserver(this);
//
//            dt = spatioTemporal.getDtOut();
//            dx = spatioTemporal.getDxOut() * scaleCA;
//            roadlength = simulator.getSimInput().getSimulationInput().getSingleRoadInput().getRoadLength() * scaleCA;
//            duration = simulator.getSimInput().getSimulationInput().getMaxSimTime();
//            cols = (int) (duration / dt);
//            System.out.println("cols: " + cols);
//            rows = (int) (roadlength / dx);
//            System.out.println(("rows: " + rows));
//            countXElements = 0;
//
//            data = new double[][] { {}, };
//
//            final JFreeChart chart = createXYZChart(data);
//            data = null;
//            spChartPanel = new ChartPanel(chart, true, false, false, true, true);
//            SwingHelper.setComponentSize(spChartPanel, 800, 380);
//
//            spChartPanel.setMouseWheelEnabled(true);
//            spChartPanel.setHorizontalAxisTrace(true);
//            spChartPanel.setVerticalAxisTrace(true);
//            add(spChartPanel, BorderLayout.CENTER);
//
//            pack();
//            setVisible(true);
//        }
//    }
//
//    protected void removeObserver() {
//        spatioTemporal.removeObserver(this);
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
//     */
//    @Override
//    public void notifyObserver(final double time) {
//        // System.out.println("spatio notified time: "+time);
//        if (spatioTemporal != null) {
//            pullSpatioTemporalData(time);
//            updateview();
//        }
//    }
//
//    private void updateview() {
//        xyzSet.setData(data);
//        callRepaint();
//    }
//
//    protected void callRepaint() {
//        spChartPanel.repaint();
//    }
//
//    private void pullSpatioTemporalData(double time) {
//        final double[] averageSpeed = spatioTemporal.getAverageSpeed();
//        if (flagAfterFirstData) {
//            countXElements++;
//        } else {
//            length = averageSpeed.length;
//            data = new double[cols][length];
//
//            for (int j = 0; j < cols; j++) {
//                for (int i = 0; i < averageSpeed.length; i++) {
//                    data[j][i] = -0.001;
//                }
//            }
//
//            flagAfterFirstData = true;
//        }
//
//        for (int i = 0; i < averageSpeed.length; i++) {
//            data[countXElements][i] = averageSpeed[i] * 3.6 * scaleCA;
//        }
//
//    }
//
//    private JFreeChart createXYZChart(double[][] data) {
//        final NumberAxis xAxis = new NumberAxis("x"); // TODO labels sp
//        // xAxis.setLabelFont(getFont().deriveFont(Font.BOLD));
//        xAxis.setRange(0, cols);
//
//        final NumberAxis yAxis = new NumberAxis("y");
//        // yAxis.setLabelFont(getFont().deriveFont(Font.BOLD));
//        yAxis.setRange(0, rows);
//
//        xyzSet = new XYZArrayDataset(data);
//        final XYPlot plot = new XYPlot(xyzSet, xAxis, yAxis, null);
//
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(createYTickUnits());
//
//        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
//        domainAxis.setStandardTickUnits(createXTickUnits());
//
//        final XYBlockRenderer r = new XYBlockRenderer();
//
//        // Use own implementation of a gradient paintScale
//        final PaintScale ps = new GradientPaintScale(-0.001, 140.0);
//        r.setPaintScale(ps);
//        r.setBlockHeight(1.0f);
//        r.setBlockWidth(1.0f);
//
//        plot.setRenderer(r);
//
//        final JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
//
//        chart.setBackgroundPaint(getBackground());
//
//        // color scale
//        final NumberAxis scaleAxis = new NumberAxis("Legend"); // TODO Legend sp
//        // scaleAxis.setTickLabelFont(getFont().deriveFont());
//        // scaleAxis.setLabelFont(getFont().deriveFont(Font.BOLD));
//
//        final PaintScaleLegend legend = new PaintScaleLegend(ps, scaleAxis);
//        legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
//        legend.setPadding(new RectangleInsets(10, 10, 10, 10));
//        legend.setStripWidth(30);
//        legend.setPosition(RectangleEdge.RIGHT);
//        legend.setBackgroundPaint(getBackground());
//
//        chart.addSubtitle(legend);
//
//        return chart;
//    }
//
//    /**
//     * @return
//     */
//    private static TickUnitSource createYTickUnits() {
//        final MyTickUnits units = new MyTickUnits();
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
//
//        units.add(new MyYNumberTickUnit(0.0000001, df1, 2));
//        units.add(new MyYNumberTickUnit(0.000001, df2, 2));
//        units.add(new MyYNumberTickUnit(0.00001, df3, 2));
//        units.add(new MyYNumberTickUnit(0.0001, df4, 2));
//        units.add(new MyYNumberTickUnit(0.001, df5, 2));
//        units.add(new MyYNumberTickUnit(0.01, df6, 2));
//        units.add(new MyYNumberTickUnit(0.1, df7, 2));
//        units.add(new MyYNumberTickUnit(1, df8, 2));
//        units.add(new MyYNumberTickUnit(10, df8, 2));
//        units.add(new MyYNumberTickUnit(100, df8, 2));
//        units.add(new MyYNumberTickUnit(1000, df8, 2));
//        units.add(new MyYNumberTickUnit(10000, df8, 2));
//        units.add(new MyYNumberTickUnit(100000, df8, 2));
//        units.add(new MyYNumberTickUnit(1000000, df9, 2));
//        units.add(new MyYNumberTickUnit(10000000, df9, 2));
//        units.add(new MyYNumberTickUnit(100000000, df9, 2));
//        units.add(new MyYNumberTickUnit(1000000000, df10, 2));
//        units.add(new MyYNumberTickUnit(10000000000.0, df10, 2));
//        units.add(new MyYNumberTickUnit(100000000000.0, df10, 2));
//
//        units.add(new MyYNumberTickUnit(0.00000025, df0, 5));
//        units.add(new MyYNumberTickUnit(0.0000025, df1, 5));
//        units.add(new MyYNumberTickUnit(0.000025, df2, 5));
//        units.add(new MyYNumberTickUnit(0.00025, df3, 5));
//        units.add(new MyYNumberTickUnit(0.0025, df4, 5));
//        units.add(new MyYNumberTickUnit(0.025, df5, 5));
//        units.add(new MyYNumberTickUnit(0.25, df6, 5));
//        units.add(new MyYNumberTickUnit(2.5, df7, 5));
//        units.add(new MyYNumberTickUnit(25, df8, 5));
//        units.add(new MyYNumberTickUnit(250, df8, 5));
//        units.add(new MyYNumberTickUnit(2500, df8, 5));
//        units.add(new MyYNumberTickUnit(25000, df8, 5));
//        units.add(new MyYNumberTickUnit(250000, df8, 5));
//        units.add(new MyYNumberTickUnit(2500000, df9, 5));
//        units.add(new MyYNumberTickUnit(25000000, df9, 5));
//        units.add(new MyYNumberTickUnit(250000000, df9, 5));
//        units.add(new MyYNumberTickUnit(2500000000.0, df10, 5));
//        units.add(new MyYNumberTickUnit(25000000000.0, df10, 5));
//        units.add(new MyYNumberTickUnit(250000000000.0, df10, 5));
//
//        units.add(new MyYNumberTickUnit(0.0000005, df1, 5));
//        units.add(new MyYNumberTickUnit(0.000005, df2, 5));
//        units.add(new MyYNumberTickUnit(0.00005, df3, 5));
//        units.add(new MyYNumberTickUnit(0.0005, df4, 5));
//        units.add(new MyYNumberTickUnit(0.005, df5, 5));
//        units.add(new MyYNumberTickUnit(0.05, df6, 5));
//        units.add(new MyYNumberTickUnit(0.5, df7, 5));
//        units.add(new MyYNumberTickUnit(5L, df8, 5));
//        units.add(new MyYNumberTickUnit(50L, df8, 5));
//        units.add(new MyYNumberTickUnit(500L, df8, 5));
//        units.add(new MyYNumberTickUnit(5000L, df8, 5));
//        units.add(new MyYNumberTickUnit(50000L, df8, 5));
//        units.add(new MyYNumberTickUnit(500000L, df8, 5));
//        units.add(new MyYNumberTickUnit(5000000L, df9, 5));
//        units.add(new MyYNumberTickUnit(50000000L, df9, 5));
//        units.add(new MyYNumberTickUnit(500000000L, df9, 5));
//        units.add(new MyYNumberTickUnit(5000000000L, df10, 5));
//        units.add(new MyYNumberTickUnit(50000000000L, df10, 5));
//        units.add(new MyYNumberTickUnit(500000000000L, df10, 5));
//
//        return units;
//
//    }
//
//    /**
//     * @return
//     */
//    private static TickUnitSource createXTickUnits() {
//        final MyTickUnits units = new MyTickUnits();
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
//
//        units.add(new MyXNumberTickUnit(0.0000001, df1, 2));
//        units.add(new MyXNumberTickUnit(0.000001, df2, 2));
//        units.add(new MyXNumberTickUnit(0.00001, df3, 2));
//        units.add(new MyXNumberTickUnit(0.0001, df4, 2));
//        units.add(new MyXNumberTickUnit(0.001, df5, 2));
//        units.add(new MyXNumberTickUnit(0.01, df6, 2));
//        units.add(new MyXNumberTickUnit(0.1, df7, 2));
//        units.add(new MyXNumberTickUnit(1, df8, 2));
//        units.add(new MyXNumberTickUnit(10, df8, 2));
//        units.add(new MyXNumberTickUnit(100, df8, 2));
//        units.add(new MyXNumberTickUnit(1000, df8, 2));
//        units.add(new MyXNumberTickUnit(10000, df8, 2));
//        units.add(new MyXNumberTickUnit(100000, df8, 2));
//        units.add(new MyXNumberTickUnit(1000000, df9, 2));
//        units.add(new MyXNumberTickUnit(10000000, df9, 2));
//        units.add(new MyXNumberTickUnit(100000000, df9, 2));
//        units.add(new MyXNumberTickUnit(1000000000, df10, 2));
//        units.add(new MyXNumberTickUnit(10000000000.0, df10, 2));
//        units.add(new MyXNumberTickUnit(100000000000.0, df10, 2));
//
//        units.add(new MyXNumberTickUnit(0.00000025, df0, 5));
//        units.add(new MyXNumberTickUnit(0.0000025, df1, 5));
//        units.add(new MyXNumberTickUnit(0.000025, df2, 5));
//        units.add(new MyXNumberTickUnit(0.00025, df3, 5));
//        units.add(new MyXNumberTickUnit(0.0025, df4, 5));
//        units.add(new MyXNumberTickUnit(0.025, df5, 5));
//        units.add(new MyXNumberTickUnit(0.25, df6, 5));
//        units.add(new MyXNumberTickUnit(2.5, df7, 5));
//        units.add(new MyXNumberTickUnit(25, df8, 5));
//        units.add(new MyXNumberTickUnit(250, df8, 5));
//        units.add(new MyXNumberTickUnit(2500, df8, 5));
//        units.add(new MyXNumberTickUnit(25000, df8, 5));
//        units.add(new MyXNumberTickUnit(250000, df8, 5));
//        units.add(new MyXNumberTickUnit(2500000, df9, 5));
//        units.add(new MyXNumberTickUnit(25000000, df9, 5));
//        units.add(new MyXNumberTickUnit(250000000, df9, 5));
//        units.add(new MyXNumberTickUnit(2500000000.0, df10, 5));
//        units.add(new MyXNumberTickUnit(25000000000.0, df10, 5));
//        units.add(new MyXNumberTickUnit(250000000000.0, df10, 5));
//
//        units.add(new MyXNumberTickUnit(0.0000005, df1, 5));
//        units.add(new MyXNumberTickUnit(0.000005, df2, 5));
//        units.add(new MyXNumberTickUnit(0.00005, df3, 5));
//        units.add(new MyXNumberTickUnit(0.0005, df4, 5));
//        units.add(new MyXNumberTickUnit(0.005, df5, 5));
//        units.add(new MyXNumberTickUnit(0.05, df6, 5));
//        units.add(new MyXNumberTickUnit(0.5, df7, 5));
//        units.add(new MyXNumberTickUnit(5L, df8, 5));
//        units.add(new MyXNumberTickUnit(50L, df8, 5));
//        units.add(new MyXNumberTickUnit(500L, df8, 5));
//        units.add(new MyXNumberTickUnit(5000L, df8, 5));
//        units.add(new MyXNumberTickUnit(50000L, df8, 5));
//        units.add(new MyXNumberTickUnit(500000L, df8, 5));
//        units.add(new MyXNumberTickUnit(5000000L, df9, 5));
//        units.add(new MyXNumberTickUnit(50000000L, df9, 5));
//        units.add(new MyXNumberTickUnit(500000000L, df9, 5));
//        units.add(new MyXNumberTickUnit(5000000000L, df10, 5));
//        units.add(new MyXNumberTickUnit(50000000000L, df10, 5));
//        units.add(new MyXNumberTickUnit(500000000000L, df10, 5));
//
//        return units;
//
//    }
//
//    // ==============================================================================================================================
//
//    public class XYZArrayDataset extends AbstractXYZDataset {
//
//        @Override
//        public DomainOrder getDomainOrder() {
//            return super.getDomainOrder();
//        }
//
//        double[][] dataArray;
//        int rowCount = 0;
//        int columnCount = 0;
//
//        XYZArrayDataset(double[][] dataparam) {
//            this.dataArray = dataparam;
//            rowCount = dataparam.length;
//            columnCount = dataparam[0].length;
//        }
//
//        public void setData(double[][] dataparam) {
//            this.dataArray = dataparam;
//            rowCount = dataArray.length;
//            columnCount = dataArray[0].length;
//            fireDatasetChanged();
//        }
//
//        @Override
//        public int getSeriesCount() {
//            return 1;
//        }
//
//        @Override
//        public Comparable getSeriesKey(int series) {
//            return "heatMap";
//        }
//
//        @Override
//        public int getItemCount(int series) {
//            return rowCount * columnCount;
//        }
//
//        @Override
//        public double getXValue(int series, int item) {
//            return (item / columnCount);
//        }
//
//        @Override
//        public double getYValue(int series, int item) {
//            return item % columnCount;
//        }
//
//        @Override
//        public double getZValue(int series, int item) {
//            return dataArray[(item / columnCount)][item % columnCount];
//        }
//
//        @Override
//        public Number getX(int series, int item) {
//            return new Double((item / columnCount));
//        }
//
//        @Override
//        public Number getY(int series, int item) {
//            return new Double(item % columnCount);
//        }
//
//        @Override
//        public Number getZ(int series, int item) {
//            return new Double(dataArray[(item / columnCount)][item % columnCount]);
//        }
//    }
//
//}
