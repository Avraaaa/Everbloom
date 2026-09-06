package com.everbloom;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.sql.SQLException;

public class App extends Application {

    private static final String WINDOW_TITLE = "EverBloom — Flower Shop Manager";

    @Override
    public void start(Stage stage) throws IOException {
        initializeDatabase();

        FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(
                App.class.getResource("view/main-view.fxml"),
                "Missing main view"
        ));
        Parent root = loader.load();

        stage.setTitle(WINDOW_TITLE);
        stage.setMinWidth(940);
        stage.setMinHeight(620);
        stage.setScene(new Scene(root, 1180, 760));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void initializeDatabase() throws IOException {
        try {
            DatabaseInitializer databaseInitializer = new DatabaseInitializer(new DatabaseConnection());
            databaseInitializer.initialize();
        } catch (SQLException exception) {
            throw new IOException("Unable to initialize the application database.", exception);
        }
    }
}
