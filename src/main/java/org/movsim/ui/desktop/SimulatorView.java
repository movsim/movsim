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

import org.movsim.output.LoopDetector;
import org.movsim.simulator.Simulator;
import org.movsim.utilities.ObserverInTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ralph
 *
 */
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
        
        simulator.getSimObservables().getSpatioTemporal().registerObserver(this);
        simulator.getSimObservables().getFloatingCars().registerObserver(this);
        List<LoopDetector>  loopDetectors = simulator.getSimObservables().getLoopDetectors();
        for(final LoopDetector loopDet : loopDetectors){
            loopDet.registerObserver(this);
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

    /* (non-Javadoc)
     * @see org.movsim.output.Macro3DObserver#updateMacro3D()
     */
//    public void updateMacro3D() {
//        double[] state = simulator.getSimOutput().getMacro3D().getState();
//        System.out.println("state: "+ state[0] + " " +state[1] + " " +state[2]);
//        durationLabel.setText(String.valueOf(state[0]));
//        System.out.println(String.valueOf(state[0]));
//        
//        //  open up standard input
//        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
//
//        String userName = null;
//
//        try {
//           userName = br.readLine();
//        } catch (IOException ioe) {
//           System.out.println("IO error trying to read your name!");
//           System.exit(1);
//        }
//    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
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


    /**
     * 
     */
    public void disableStart() {
        controlPanel.startButton.setEnabled(false);
    }


    /**
     * 
     */
    public void disablePause() {
        controlPanel.pauseButton.setEnabled(false);
    }


    /**
     * 
     */
    public void disableStop() {
        controlPanel.stopButton.setEnabled(false);
    }
    
    /**
     * 
     */
    public void enableStart() {
        controlPanel.startButton.setEnabled(true);
    }


    /**
     * 
     */
    public void enablePause() {
        controlPanel.pauseButton.setEnabled(true);
    }


    /**
     * 
     */
    public void enableStop() {
        controlPanel.stopButton.setEnabled(true);
    }


    @Override
    public void notifyObserver(double time) {
        // TODO: aktuelle Daten liegen vor, hole die Daten ab
        double[] flows = simulator.getSimObservables().getSpatioTemporal().getFlow();
        System.out.println("update view: "+flows);
        System.exit(0);
    }








}
