package org.movsim.ui.desktop;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.ITracePoint2D;
import info.monitorenter.gui.chart.io.ADataCollector;
import info.monitorenter.gui.chart.io.RandomDataCollectorOffset;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.traces.Trace2DSimple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.movsim.input.model.VehicleInput;
import org.movsim.input.model.vehicle.longModel.AccelerationModelInputDataIDM;
import org.movsim.output.FloatingCars;
import org.movsim.output.LoopDetector;
import org.movsim.output.SpatioTemporal;
import org.movsim.simulator.Constants;
import org.movsim.simulator.Simulator;
import org.movsim.simulator.vehicles.Moveable;
import org.movsim.simulator.vehicles.longmodel.accelerationmodels.impl.Newell;
import org.movsim.utilities.ObserverInTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorView implements ObserverInTime, ActionListener {

    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorView.class);

    /** Reference to the model */
    private Simulator simulator;

    /** Reference to the controller */
    private ControllerInterface controller;

    JFrame viewFrame;
    private ControlPanel controlPanel;

    private JFrame controlFrame;
    private JPanel viewPanel;

    private SpatioTemporal spatioTemporal;

    private List<LoopDetector> loopDetectors = new ArrayList<LoopDetector>();

    private List<String> detectorNames = new ArrayList<String>();

    private Map<String, List<DetectorDataPoint>> hashDetectors = new HashMap<String, List<DetectorDataPoint>>();

    private ArrayList<SpatioTemporalData> spatioTemporalDataPoints = new ArrayList<SpatioTemporalData>();

    private FloatingCars floatingCars;

    private List<Integer> listOfFloatingCars;

    private Map<Integer, List<FloatingCarDataPoint>> floatingcarsDataPoints;

    private ADataCollector collector;

    public SimulatorView(Simulator simulator, ControllerInterface controller) {

        this.simulator = simulator;
        this.controller = controller;

        spatioTemporal = simulator.getSimObservables().getSpatioTemporal();
        if (spatioTemporal == null) {
            System.out.println("error, expected spatiotemporal xml configuration for use case here ");
            // System.exit(0);
        } else {
            spatioTemporal.registerObserver((ObserverInTime) this);
            SpatioTemporalData.setDx(spatioTemporal.getDxOut());
        }

        floatingCars = simulator.getSimObservables().getFloatingCars();
        if (floatingCars == null) {
            System.out.println("error, expected floating_cars xml configuration for use case here ");
        } else {
            floatingCars.registerObserver((ObserverInTime) this);
            listOfFloatingCars = floatingCars.getFcdList();
            floatingcarsDataPoints = new HashMap<Integer, List<FloatingCarDataPoint>>();
        }

        // TESTWEISE
        List<VehicleInput> vehicleInput = simulator.getSimInput().getVehicleInputData();
        AccelerationModelInputDataIDM parametersIDM = (AccelerationModelInputDataIDM) vehicleInput.get(0)
                .getAccelerationModelInputData();
        parametersIDM.getT();
        // parametersIDM.setT(2.0);

        // System.out.println("exit here ");
        // System.exit(0);

        if (simulator.getSimObservables().getLoopDetectors() != null) {
            loopDetectors = simulator.getSimObservables().getLoopDetectors();
            for (final LoopDetector loopDet : loopDetectors) {
                loopDet.registerObserver((ObserverInTime) this);
                final int xDetectorInt = (int) loopDet.getDetPosition();
                String s = String.valueOf(xDetectorInt);
                detectorNames.add(s);
                List<DetectorDataPoint> detectorDataPoints = new ArrayList<DetectorDataPoint>();
                hashDetectors.put(s, detectorDataPoints);

            }
        }

    }

    public void createOutputViews() {
        // create all Swing components
//        viewFrame = new JFrame("View");
//        activateWindowExitButton(viewFrame);
//
//        // viewFrame.setSize(400, 400);
//        viewFrame.setLocation(200, 80);
//
//        viewPanel = new JPanel();
//        viewPanel.setSize(400, 400);
//
//        viewFrame.add(viewPanel, BorderLayout.CENTER);
//        viewFrame.pack();
//        viewFrame.setVisible(true);
        
        // Create a chart:  
        Chart2D chart = new Chart2D();
        // Create an ITrace: 
        // Note that dynamic charts need limited amount of values!!! 
        ITrace2D trace = new Trace2DLtd(200); 
        trace.setColor(Color.RED);
     
        // Add the trace to the chart. This has to be done before adding points (deadlock prevention): 
        chart.addTrace(trace);
        
        // Make it visible:
        // Create a frame. 
        JFrame frame = new JFrame("MinimalDynamicChart");
        // add the chart to the frame: 
        frame.getContentPane().add(chart);
        frame.setSize(400,300);
        frame.setVisible(true); 
        // Every 50 milliseconds a new value is collected. 
//       ADataCollector collector = new RandomDataCollectorOffset(trace, 100);
        // Start an internal Thread that adds the values: 
        
        
    }

    public void createControls() {
        // create all Swing components
        controlFrame = new JFrame("Control");
        activateWindowExitButton(controlFrame);

        controlFrame.setSize(100, 80);

        controlPanel = new ControlPanel(this);

        controlFrame.getContentPane().add(controlPanel, BorderLayout.CENTER);
        controlFrame.pack();
        controlFrame.setVisible(true);
    }

    /**
     * 
     */
    private void activateWindowExitButton(JFrame frame) {
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evnt) {
                evnt.getWindow().setVisible(false);
                evnt.getWindow().dispose();
                System.exit(0);
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == controlPanel.startButton) {
            controller.start();
        } else if (e.getSource() == controlPanel.stopButton) {
            controller.stop();
        } else if (e.getSource() == controlPanel.pauseButton) {
            controller.pause();
        }
        logger.debug("command: " + e.getActionCommand());
    }

    public void disableStart() {
        controlPanel.startButton.setEnabled(false);
    }

    public void disablePause() {
        controlPanel.pauseButton.setEnabled(false);
    }

    public void disableStop() {
        controlPanel.stopButton.setEnabled(false);
    }

    public void enableStart() {
        controlPanel.startButton.setEnabled(true);
    }

    public void enablePause() {
        controlPanel.pauseButton.setEnabled(true);
    }

    public void enableStop() {
        controlPanel.stopButton.setEnabled(true);
    }

    @Override
    public void notifyObserver(double time) {
        // TODO: aktuelle Daten liegen vor, hole die Daten ab
        if (spatioTemporal != null) {
            pullSpatioTemporalData(time);
        }
        if (loopDetectors.size() > 0) {
            pullLoopDetectorData(time);
        }
        if (floatingCars != null) {
            pullFloatingCarsData(time);
        }
        // updateViews();
    }

    public void updateViews() {

        // test chart
        Chart2D chart = new Chart2D();

        ITrace2D trace = new Trace2DSimple();
        trace.setColor(Color.red);
//        trace.addTracePainter();

        chart.addTrace(trace);

        // test mit 2.det
        List<DetectorDataPoint> det2 = hashDetectors.get(detectorNames.get(2));

        // add data to trace
        for (DetectorDataPoint dp : det2) {
            trace.addPoint(dp.getFlow(), dp.getDensity());
        }

        // Make it visible:
        // Create a frame.
        JFrame frame = new JFrame("MinimalStaticChart");
        // add the chart to the frame:
        frame.getContentPane().add(chart);
        frame.setSize(400, 300);
        frame.setVisible(true);

    }
    
    public void showfc() {
     // test chart
        Chart2D chart = new Chart2D();

        ITrace2D trace = new Trace2DSimple();
        trace.setColor(Color.red);
//        trace.addTracePainter();

        chart.addTrace(trace);
        
        // add data to trace
        List<FloatingCarDataPoint> list = floatingcarsDataPoints.get(5);
        for (FloatingCarDataPoint dp: list) {
            trace.addPoint(dp.getPosition(), dp.getSpeed());
        }
        
        
     // Make it visible:
        // Create a frame.
        JFrame frame = new JFrame("MinimalStaticChart");
        // add the chart to the frame:
        frame.getContentPane().add(chart);
        frame.setSize(400, 300);
        frame.setVisible(true);
    }

    private void pullLoopDetectorData(double time) {
        for (LoopDetector det : loopDetectors) {
            final int xDetectorInt = (int) det.getDetPosition();
            String s = String.valueOf(xDetectorInt);
            List<DetectorDataPoint> list = hashDetectors.get(s);
            list.add(new DetectorDataPoint(time, det.getFlow() * 3600, det.getDensityArithmetic() * 1000, det
                    .getMeanSpeed() * 3.6));
            
        }
    }

    private void pullSpatioTemporalData(double time) {
        for (int j = 0, N = spatioTemporal.getDensity().length; j < N; j++) {
            final double x = j * spatioTemporal.getDxOut();
            spatioTemporalDataPoints.add(new SpatioTemporalData(time, x, spatioTemporal.getAverageSpeed()[j]));
        }
    }

    private void pullFloatingCarsData(double time) {
        for (int fc : listOfFloatingCars) {
            Moveable floatingCar = floatingCars.getMoveableContainer().getMoveable(fc);
            List<FloatingCarDataPoint> data = floatingcarsDataPoints.get(fc);
            if (data == null) {
                ArrayList<FloatingCarDataPoint> list = new ArrayList<FloatingCarDataPoint>();
                list.add(new FloatingCarDataPoint(time, floatingCar.getPosition(), floatingCar.getSpeed()*3.6, floatingCar
                        .getAcc()));
                floatingcarsDataPoints.put(fc, list);
            } else {
                floatingcarsDataPoints.get(fc).add(
                        new FloatingCarDataPoint(time, floatingCar.getPosition(), floatingCar.getSpeed()*3.6, floatingCar
                                .getAcc()));
            }


        }
    }
}
