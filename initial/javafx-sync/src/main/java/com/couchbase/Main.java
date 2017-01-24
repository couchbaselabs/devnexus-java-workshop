package com.couchbase;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    CouchbaseSingleton couchbase;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/MovieFX.fxml"));
        primaryStage.setTitle("Couchbase JavaFX Example");
        primaryStage.setScene(new Scene(root, 600, 380));
        primaryStage.setResizable(false);
        primaryStage.show();
        this.couchbase = CouchbaseSingleton.getInstance();
        this.couchbase.startReplication(new URL("http://localhost:4984/devnexus/"), true);
    }

    @Override
    public void stop() {
        this.couchbase.stopReplication();
    }

    public static void main(String[] args) {
        launch(args);
    }
}