package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ResourceBundle;

import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.movsim.facades.MovsimViewerFacade;
import org.movsim.viewer.control.SimulationRunnable;
import org.movsim.viewer.graphics.GraphicsConfigurationParameters;
import org.movsim.viewer.graphics.TrafficCanvasKeyListener;
import org.movsim.viewer.graphics.TrafficCanvasScenarios;
import org.movsim.viewer.graphics.TrafficCanvasScenarios.Scenario;
import org.movsim.viewer.util.SwingHelper;

public class CanvasPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    TrafficCanvasScenarios trafficCanvas;
    SimulationRunnable simulationRunnable;
    TrafficCanvasKeyListener controller;

    private ResourceBundle resourceBundle;

    public CanvasPanel(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;

        // SwingHelper.makeLightWeightComponentsVisible(); // TODO check if needed anymore

        final JTextArea logArea = new JTextArea();
        LogWindow.setupLog4JAppender(logArea);

        try {
            // Execute a job on the event-dispatching thread; creating this applet's GUI.
            SwingUtilities.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    initApp();
                }
            });
        } catch (final Exception e) {
            e.printStackTrace();
            System.err.println("initApp didn't complete successfully"); //$NON-NLS-1$
        }

    }

    protected void initApp() {
        final MovsimViewerFacade movsimViewerFacade = MovsimViewerFacade.getInstance();
        simulationRunnable = SimulationRunnable.getInstance();

        this.setBackground(GraphicsConfigurationParameters.BACKGROUND_COLOR_SIM);

        trafficCanvas = new TrafficCanvasScenarios(simulationRunnable, movsimViewerFacade);

        this.controller = new TrafficCanvasKeyListener(trafficCanvas);

        initStrings(resourceBundle);

        layoutCanvas();

        // first scenario
        trafficCanvas.setupTrafficScenario(Scenario.STARTSTOPFILE);

    }

    /**
     * Handle component resized event.
     */
    public void resized() {
        final int width = this.getSize().width;
        final int height = this.getSize().height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));
        trafficCanvas.setSize(width, height);
        trafficCanvas.requestFocusInWindow(); // give the canvas the keyboard focus
    }

    private void initStrings(ResourceBundle resourceBundle) {
        trafficCanvas.setMessageStrings((String) resourceBundle.getObject("VehiclePopup"), //$NON-NLS-1$
                (String) resourceBundle.getObject("VehiclePopupNoExit"), //$NON-NLS-1$
                (String) resourceBundle.getObject("TrafficInflow"), //$NON-NLS-1$
                (String) resourceBundle.getObject("RampingFinished"), //$NON-NLS-1$
                (String) resourceBundle.getObject("PerturbationApplied")); //$NON-NLS-1$
    }

    private void layoutCanvas() {

        final int width = this.getSize().width;
        final int height = this.getSize().height;
        trafficCanvas.setPreferredSize(new Dimension(width, height));

        this.add(trafficCanvas, BorderLayout.CENTER);
        trafficCanvas.setSize(width, height);

        this.repaint();
    }

    public void setDrawRoadId(boolean drawRoadId) {
        trafficCanvas.setDrawRoadId(drawRoadId);
    }

    public void quit() {
        if (trafficCanvas.isStopped() == false) {
            trafficCanvas.stop();
            // statusCallbacks.stateChanged();
        }
    }
}
