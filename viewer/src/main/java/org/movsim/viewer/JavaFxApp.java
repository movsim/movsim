package org.movsim.viewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.movsim.input.MovsimCommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.logging.Logger;
import org.movsim.viewer.javafx.FxSimCanvas;
import org.movsim.viewer.ui.ViewProperties;

import java.util.Properties;

public class JavaFxApp extends Application {
    private static Properties viewProperties;
    private int startDragX;
    private int startDragY;
    private int xOffsetSave;
    private int yOffsetSave;

    public static void main(String[] args) {

        Logger.initializeLoggerJavaFx();

        // parse the command line args for simulator settings, putting the results into projectMetaData
        MovsimCommandLine.parse(args);

        // viewer settings
        viewProperties = ViewProperties.loadProperties(ProjectMetaData.getInstance());

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Group main = new Group();
        int xPixSizeWindow = Integer.valueOf(viewProperties.getProperty("xPixSizeWindow", "1000"));
        int yPixSizeWindow = Integer.valueOf(viewProperties.getProperty("yPixSizeWindow", "800"));
        FxSimCanvas canvas = new FxSimCanvas((int) xPixSizeWindow, yPixSizeWindow, viewProperties);
        main.getChildren().add(canvas);

        VBox vBox = new VBox(createToolBar(canvas), main);

        primaryStage.setTitle("Movsim Viewer");
        primaryStage.setScene(new Scene(vBox));
        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> Platform.exit());

        // Dragging canvas support
        canvas.setOnMousePressed((e) -> {
            startDragX = (int) e.getX();
            startDragY = (int) e.getY();
            xOffsetSave = canvas.settings.getxOffset();
            yOffsetSave = canvas.settings.getyOffset();
        });
        canvas.setOnMouseReleased((e) -> {
            int xOffsetNew = xOffsetSave + (int) ((e.getX() - startDragX) / canvas.settings.getScale());
            int yOffsetNew = yOffsetSave + (int) ((e.getY() - startDragY) / canvas.settings.getScale());
            if ((xOffsetNew != canvas.settings.getxOffset()) || (yOffsetNew != canvas.settings.getyOffset())) {
                canvas.settings.setxOffset(xOffsetNew);
                canvas.settings.setyOffset(yOffsetNew);
                canvas.execTranslateFx();
                canvas.forceRepaintBackground();
            }
        });
    }

    private ToolBar createToolBar(FxSimCanvas canvas) {
        ToolBar toolBar = new ToolBar();

        Button startStopButton = new Button("Pause");
        startStopButton.setMinWidth(100);
        startStopButton.setOnMouseClicked((e) -> {
            if (canvas.simulator.getSimulationRunnable().isPaused()) {
                canvas.simulator.getSimulationRunnable().resume();
                startStopButton.setText("Pause");
            } else {
                canvas.simulator.getSimulationRunnable().pause();
                startStopButton.setText("Start");
            }
        });

        Button zoomIn = new Button("Zoom In");
        zoomIn.setMinWidth(100);
        zoomIn.setOnMouseClicked((e) -> {
            final double zoomFactor = Math.sqrt(2.0);
            canvas.settings.setScale(canvas.settings.getScale() * zoomFactor);
            canvas.execScale();
            canvas.forceRepaintBackground();
        });

        Button zoomOut = new Button("Zoom Out");
        zoomOut.setMinWidth(100);
        zoomOut.setOnMouseClicked((e) -> {
            final double zoomFactor = Math.sqrt(2.0);
            canvas.settings.setScale(canvas.settings.getScale() / zoomFactor);
            canvas.execScale();
            canvas.forceRepaintBackground();
        });

        Button reset = new Button("Reset");
        reset.setMinWidth(100);
        reset.setOnMouseClicked((e) -> {
            canvas.init();
        });

        toolBar.getItems().addAll(startStopButton, reset, zoomIn, zoomOut);
        return toolBar;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }
}
