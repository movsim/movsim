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
//import java.awt.Window;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.ResourceBundle;
//
//import javax.swing.JCheckBoxMenuItem;
//import javax.swing.JFrame;
//import javax.swing.SwingUtilities;
//
//import org.jfree.chart.ChartFactory;
//import org.jfree.chart.ChartPanel;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.plot.PlotOrientation;
//import org.jfree.chart.plot.XYPlot;
//import org.jfree.chart.renderer.xy.XYSplineRenderer;
//import org.jfree.chart.title.LegendTitle;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;
//import org.jfree.ui.RectangleEdge;
//import org.movsim.facades.MovsimViewerFacade;
//import org.movsim.utilities.ObserverInTime;
//import org.movsim.utilities.impl.XYDataPoint;
//import org.movsim.viewer.util.SwingHelper;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class TravelTimeDiagram extends JFrame implements ObserverInTime {
//
//    final static Logger LOG = LoggerFactory.getLogger(TravelTimeDiagram.class);
//
//    private static final long serialVersionUID = 6632140050281070354L;
//
//    private final String TITLE_CHART;
//    private final String X_LABEL_CHART;
//    private final String Y_LABEL_CHART;
//    private final String SERIES_KEY_ROUTE_1;
//    private final String SERIES_KEY_ROUTE_2;
//
//    final int INIT_WIDTH = 480;
//    final int INIT_HEIGHT = 280;
//
//    private final int N_DATA_MAX = 80; // depends on update notification from simulation
//
//    private JFreeChart chart;
//    private List<XYSeries> series;
//    private XYSeriesCollection dataSet;
//    private XYPlot plot;
//
//    public TravelTimeDiagram(ResourceBundle resourceBundle, JCheckBoxMenuItem cbMenu) {
//        super(resourceBundle.getString("TitleFrameTravelTime"));
//        TITLE_CHART = resourceBundle.getString("TitleFrameTravelTime");
//        X_LABEL_CHART = resourceBundle.getString("xLabelChart");
//        Y_LABEL_CHART = resourceBundle.getString("yLabelChart");
//        SERIES_KEY_ROUTE_1 = resourceBundle.getString("SerieMainRoute");
//        SERIES_KEY_ROUTE_2 = resourceBundle.getString("RouteDetour");
//
//        addWindowListener(new WindowAdapter() {
//            @Override
//            public void windowClosing(WindowEvent evnt) {
//                removeObserver();
//                cbMenu.setSelected(false);
//                evnt.getWindow().setVisible(false);
//                evnt.getWindow().dispose();
//            }
//        });
//
//        LOG.debug("Constuctor TravelTime diagramm");
//
//        setLocation(820, 700);
//
//        init();
//
//        createChart();
//
//        final ChartPanel chartPanel = new ChartPanel(chart, true, true, true, true, true);
//        chartPanel.setMouseWheelEnabled(true);
//        SwingHelper.setComponentSize(chartPanel, INIT_WIDTH, INIT_HEIGHT);
//
//        add(chartPanel);
//
//        pack();
//        setVisible(true);
//
//        registerObserver();
//
//        // notification update every 20 seconds
//        final long updateIteration = (long) (45 / 0.2); // 20/0.2
//        MovsimViewerFacade.getInstance().getSimObservables().getTravelTimes().setUpdateInterval(updateIteration);
//
//    }
//
//    private void registerObserver() {
//        MovsimViewerFacade.getInstance().getSimObservables().getTravelTimes().registerObserver(this);
//    }
//
//    private void removeObserver() {
//        MovsimViewerFacade.getInstance().getSimObservables().getTravelTimes().removeObserver(this);
//    }
//
//    private void init() {
//        dataSet = new XYSeriesCollection();
//        final List<Double> data = MovsimViewerFacade.getInstance().getTravelTimeDataEMAs(0);
//        series = new ArrayList<XYSeries>();
//        for (int i = 0; i < data.size(); i++) {
//            final String titleSeries = (i == 0) ? SERIES_KEY_ROUTE_1 : SERIES_KEY_ROUTE_2;
//            final XYSeries xyseries = new XYSeries(titleSeries);
//            xyseries.setMaximumItemCount(N_DATA_MAX);
//            series.add(xyseries);
//        }
//
//        // TODO better loop control
//        int index = 0;
//        final List<List<XYDataPoint>> newData = MovsimViewerFacade.getInstance().getTravelTimeEmas();
//        for (final List<XYDataPoint> routeData : newData) {
//            for (final XYDataPoint ema : routeData) {
//                if (ema.getY() > 0) {
//                    series.get(index).add(ema.getX() / 60., ema.getY() / 60.);
//                }
//            }
//            index++;
//        }
//
//        // collection
//        for (final XYSeries serie : series) {
//            dataSet.addSeries(serie);
//        }
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.movsim.utilities.ObserverInTime#notifyObserver(double)
//     */
//    @Override
//    public void notifyObserver(final double time) {
//        SwingUtilities.invokeLater(new Runnable() {
//
//            @Override
//            public void run() {
//                LOG.debug("being notified: pull data and update view .. ");
//                pullData(time);
//            }
//        });
//    }
//
//    private void pullData(double time) {
//        // synchronized (SimulationRunnable.getInstance().dataLock) {
//        int index = 0;
//
//        final List<Double> newData = MovsimViewerFacade.getInstance().getTravelTimeDataEMAs(time);
//
//        for (final Double ttEMA : newData) {
//            if (ttEMA != 0) {
//                series.get(index).add(time / 60., ttEMA / 60.);
//            }
//            index++;
//        }
//    }
//
//    private void createChart() {
//        chart = ChartFactory.createXYLineChart(TITLE_CHART, X_LABEL_CHART, Y_LABEL_CHART, dataSet,
//                PlotOrientation.VERTICAL, true, false, false);
//        chart.setBackgroundPaint(getBackground());
//        plot = chart.getXYPlot();
//
//        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        final NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
//        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//        // smooth chart
//        final XYSplineRenderer renderer = new XYSplineRenderer();
//        renderer.setSeriesShapesVisible(0, false);
//        renderer.setSeriesShapesVisible(1, false);
//        plot.setRenderer(renderer);
//
//        // ValueAxis vDomainAxis = plot.getDomainAxis();
//        // domainAxis.setRange(0, duration);
//        //
//        // ValueAxis vRangeAxis = plot.getRangeAxis();
//        // vRangeAxis.setRange(0, 1000);
//
//        final LegendTitle legend = chart.getLegend();
//        legend.setPosition(RectangleEdge.RIGHT);
//
//    }
//
//    public void closeWindow() {
//        final Window w = this;
//        w.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(w, WindowEvent.WINDOW_CLOSING));
//    }
//}
