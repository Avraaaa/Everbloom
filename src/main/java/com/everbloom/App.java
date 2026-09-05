package com.everbloom;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    private static final String WINDOW_TITLE = "EverBloom — Flower Shop Manager";

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                App.class.getResource("view/main-view.fxml"),
                "Missing main view"
        ));
        Parent root = loader.load();

        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(560);
        stage.setMinHeight(340);
        stage.setScene(new Scene(root, 760, 460));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
