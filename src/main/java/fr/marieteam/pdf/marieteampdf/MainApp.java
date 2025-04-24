package fr.marieteam.pdf.marieteampdf;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static Stage primaryStage;

    public static void setRoot(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource(fxml));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        primaryStage.setScene(scene);
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        setRoot("Welcome.fxml");
        primaryStage.setTitle("MarieTeam PDF Generator");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}