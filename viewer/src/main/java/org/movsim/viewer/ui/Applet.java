package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.graphics.TrafficCanvas;
import org.movsim.viewer.util.LocalizationStrings;

public class Applet extends JApplet {
    private static final long serialVersionUID = 1L;
    private static String DEFAULT_SCENARIO = "/sim/games/routing";

    private CanvasPanel canvasPanel;
    private StatusPanel statusPanel;
    private Component toolBar;

    @Override
    public void init() {
        super.init();

        setLayout(new BorderLayout());
        initializeLogger();
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName());

        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        projectMetaData.setXmlFromResources(true);
        projectMetaData.setInstantaneousFileOutput(false);
        Properties properties = ViewProperties.loadDefaultProperties();

        String scenario = getParameter("scenario");
        if (scenario == null) {
            scenario = DEFAULT_SCENARIO;
        }
        final String scenarioPath = scenario.substring(0, scenario.lastIndexOf("/")+1);
        projectMetaData.setPathToProjectXmlFile(scenarioPath);
        final String scenarioName = scenario.substring(scenario.lastIndexOf("/")+1);
        projectMetaData.setProjectName(scenarioName);

        final Simulator simulator = new Simulator();
        initLookAndFeel();

        final TrafficCanvas trafficCanvas = new TrafficCanvas(simulator, properties);
        canvasPanel = new CanvasPanel(resourceBundle, trafficCanvas);
        statusPanel = new StatusPanel(resourceBundle, simulator);

        addToolBar(resourceBundle, trafficCanvas);
        addMenu(resourceBundle, simulator, trafficCanvas);

        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);

        setSize(1280, 800);
        resize(1280, 800);
        canvasPanel.setSize(1280, 800);
        trafficCanvas.setSize(1280, 800);

        canvasPanel.resized();
        canvasPanel.repaint();

        statusPanel.setWithProgressBar(false);
        statusPanel.reset();
        trafficCanvas.setupTrafficScenario(projectMetaData.getProjectName(), projectMetaData.getPathToProjectFile());
        final String projectName = projectMetaData.getProjectName();
        if (projectName.equals("routing") || projectName.equals("ramp_metering")) {
            trafficCanvas.setVehicleColorMode(TrafficCanvas.VehicleColorMode.EXIT_COLOR);
        }
        trafficCanvas.start();
    }

    private void addToolBar(ResourceBundle resourceBundle, TrafficCanvas trafficCanvas) {
        toolBar = new MovSimToolBar(statusPanel, trafficCanvas, resourceBundle);
    }

    private void addMenu(ResourceBundle resourceBundle, Simulator simulator, TrafficCanvas trafficCanvas) {
        final AppletMenu trafficMenus = new AppletMenu(canvasPanel, trafficCanvas, statusPanel, resourceBundle);
        trafficMenus.initMenus(this);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            System.out.println("set to system LaF");
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        } catch (final InstantiationException e) {
            e.printStackTrace();
        } catch (final IllegalAccessException e) {
            e.printStackTrace();
        } catch (final UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(this);
    }

    /**
     * Initializes the LOG.
     */
    private static void initializeLogger() {
        // Log Levels: DEBUG < INFO < WARN < ERROR;
        final URL log4jConfig = Applet.class.getResource("/config/log4japplet.properties");
        PropertyConfigurator.configure(log4jConfig);
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
    }

    @Override
    public void resize(Dimension d) {
        super.resize(d);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
