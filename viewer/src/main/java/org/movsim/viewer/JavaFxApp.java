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

    public static void main(String[] args) {
        final ProjectMetaData projectMetaData = ProjectMetaData.getInstance();

        Logger.initializeLoggerJavaFx();
        // parse the command line, putting the results into projectMetaData
        MovsimCommandLine.parse(args);

        properties = ViewProperties.loadProperties(projectMetaData);

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Movsim Viewer");
        Group root = new Group();
        Canvas canvas = new FxSimCanvas((int) properties.getOrDefault("xPixSizeWindow", 1000), (int) properties.getOrDefault("yPixSizeWindow", 800), properties);
        root.getChildren().add(canvas);

        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest((e) -> Platform.exit());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }
}
