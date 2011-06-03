package org.movsim.ui.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

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
import org.movsim.utilities.ObserverInTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulatorView implements ObserverInTime, ActionListener{
    
    /** The Constant logger. */
    final static Logger logger = LoggerFactory.getLogger(SimulatorView.class);
    
    /** Reference to the model    */
    private Simulator simulator;
    
    /** Reference to the controller */
    private ControllerInterface controller;

    
    JFrame viewFrame;
    private ControlPanel controlPanel;

    private JFrame controlFrame;
    private JPanel viewPanel;
    private JLabel durationLabel;

    private JPanel durationPanel;
    
    
    public SimulatorView(Simulator simulator, ControllerInterface controller) {
        this.simulator = simulator;
        this.controller = controller;
        
        final SpatioTemporal spatioTemporal = simulator.getSimObservables().getSpatioTemporal();
        if(spatioTemporal == null){
            System.out.println("error, expected spatiotemporal xml configuration for use case here ");
            System.exit(0);
        }
        else{
            spatioTemporal.registerObserver((ObserverInTime)this);
        }
            
        final FloatingCars floatingCars = simulator.getSimObservables().getFloatingCars();
        if(floatingCars == null){
            System.out.println("error, expected floating_cars xml configuration for use case here ");
            System.exit(0);
        }
        else{
            floatingCars.registerObserver((ObserverInTime)this);
        }
        
        // TESTWEISE
        List<VehicleInput> vehicleInput = simulator.getSimInput().getVehicleInputData();
        AccelerationModelInputDataIDM parametersIDM = (AccelerationModelInputDataIDM)vehicleInput.get(0).getModelInputData();
        parametersIDM.getT();
//        parametersIDM.setT(2.0);

//        System.out.println("exit here ");
//        System.exit(0);

        
        
        List<LoopDetector>  loopDetectors = simulator.getSimObservables().getLoopDetectors();
        for(final LoopDetector loopDet : loopDetectors){
            loopDet.registerObserver((ObserverInTime)this);
        }
    }
    
    
    public void createOutputViews() {
        // create all Swing components
        viewFrame = new JFrame("View");
        activateWindowExitButton(viewFrame);
        
        viewFrame.setSize(new Dimension(100, 80));
        viewFrame.setLocation(200, 80);
        
        viewPanel = new JPanel(new GridLayout(1, 2));
        durationLabel = new JLabel("offline", SwingConstants.CENTER);
        durationPanel = new JPanel(new GridLayout(2, 1));
        durationPanel.add(durationLabel);
        viewPanel.add(durationPanel);
        
        viewFrame.getContentPane().add(viewPanel, BorderLayout.CENTER);
        viewFrame.pack();
        viewFrame.setVisible(true);
    }
    
    public void createControls() {
        // create all Swing components
        controlFrame = new JFrame("Control");
        activateWindowExitButton(controlFrame);
        
        controlFrame.setSize(new Dimension(100, 80));
        
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
        } if (e.getSource() == controlPanel.pauseButton) {
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
    }








}
