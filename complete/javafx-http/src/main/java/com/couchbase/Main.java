package com.couchbase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/MovieFX.fxml"));
        primaryStage.setTitle("Couchbase JavaFX Example");
        primaryStage.setScene(new Scene(root, 600, 380));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() { }

    public static void main(String[] args) {
        launch(args);
    }
}