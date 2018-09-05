package org.movsim.viewer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.movsim.input.MovsimCommandLine;
import org.movsim.input.ProjectMetaData;
import org.movsim.logging.Logger;
import org.movsim.viewer.javafx.FxSimCanvas;
import org.movsim.viewer.ui.ViewProperties;

import java.util.Properties;

public class JavaFxApp extends Application {
    private static Properties properties;
    private int startDragX;
    private int startDragY;
    private int xOffsetSave;
    private int yOffsetSave;

    public static void main(String[] args) {

        Logger.initializeLoggerJavaFx();

        // parse the command line, putting the results into projectMetaData
        MovsimCommandLine.parse(args);

        properties = ViewProperties.loadProperties(ProjectMetaData.getInstance());

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Movsim Viewer");
        Group root = new Group();
        FxSimCanvas canvas = new FxSimCanvas((int) properties.getOrDefault("xPixSizeWindow", 1000), (int) properties.getOrDefault("yPixSizeWindow", 800), properties);
        root.getChildren().add(canvas);

        primaryStage.setScene(new Scene(root));
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
            }
        });
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }
}
