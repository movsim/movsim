package org.movsim.viewer.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.PropertyConfigurator;
import org.movsim.input.ProjectMetaData;
import org.movsim.simulator.Simulator;
import org.movsim.viewer.graphics.SimulationCanvasBase;
import org.movsim.viewer.util.LocalizationStrings;

public class Applet extends JApplet {

    private static final long serialVersionUID = 1L;

    private CanvasPanel canvasPanel;
    private StatusPanel statusPanel;

    private Component toolBar;

    @Override
    public void init() {

        setLayout(new BorderLayout());
        initLogger();
        final ResourceBundle resourceBundle = ResourceBundle.getBundle(LocalizationStrings.class.getName());

        String scenario = getParameter("scenario");
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();
        projectMetaData.setXmlFromResources(true);
        projectMetaData.setInstantaneousFileOutput(false);
        projectMetaData.setPathToProjectXmlFile("/sim/buildingBlocks/");
        if (scenario == null) {
            projectMetaData.setProjectName("cloverleaf");
        } else {
            projectMetaData.setProjectName(scenario);
        }
       

        final Simulator simulator = new Simulator(projectMetaData);
        initLookAndFeel();

        canvasPanel = new CanvasPanel(resourceBundle, simulator);
        final SimulationCanvasBase trafficCanvas = canvasPanel.trafficCanvas();
        statusPanel = new StatusPanel(resourceBundle, simulator);

        addToolBar(resourceBundle);
        addMenu(resourceBundle, simulator);

        add(canvasPanel, BorderLayout.CENTER);
        add(toolBar, BorderLayout.NORTH);


        this.setSize(1280, 800);
        this.resize(1280, 800);
        canvasPanel.setSize(1280, 800);
        trafficCanvas.setSize(1280, 800);

        canvasPanel.resized();
        canvasPanel.repaint();

        statusPanel.setWithProgressBar(false);
        simulator.loadScenarioFromXml(projectMetaData.getProjectName(), projectMetaData.getPathToProjectXmlFile());
        trafficCanvas.reset();
        trafficCanvas.start();
        statusPanel.reset();

        super.init();
    }

    /**
     * @param resourceBundle
     */
    private void addToolBar(ResourceBundle resourceBundle) {
        toolBar = new MovSimToolBar(statusPanel, canvasPanel.trafficCanvas(), canvasPanel.controller(), resourceBundle);
    }

    /**
     * @param resourceBundle
     */
    private void addMenu(ResourceBundle resourceBundle, Simulator simulator) {
        final AppletMenu trafficMenus = new AppletMenu(this, simulator, canvasPanel, statusPanel, resourceBundle);
        trafficMenus.initMenus();
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
     * Inits the localization and logger.
     */
    private static void initLogger() {
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
