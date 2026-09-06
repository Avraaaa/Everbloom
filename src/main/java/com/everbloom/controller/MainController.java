package com.everbloom.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MainController {

    private static final String SELECTED_STYLE_CLASS = "selected";

    @FXML
    private StackPane contentArea;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button bouquetBuilderButton;

    @FXML
    private Button ordersButton;

    @FXML
    private Button catalogueButton;

    @FXML
    private Button customersButton;

    @FXML
    private Button reportsButton;

    private List<Button> navigationButtons;

    @FXML
    private void initialize() {
        navigationButtons = List.of(
                dashboardButton,
                bouquetBuilderButton,
                ordersButton,
                catalogueButton,
                customersButton,
                reportsButton
        );
        showDashboard();
    }

    @FXML
    private void showDashboard() {
        showPage(loadView("dashboard-view.fxml"), dashboardButton);
    }

    @FXML
    private void showBouquetBuilder() {
        showPage(loadView("bouquet-builder-view.fxml"), bouquetBuilderButton);
    }

    @FXML
    private void showOrders() {
        showPage(loadView("orders-view.fxml"), ordersButton);
    }

    @FXML
    private void showCatalogue() {
        showPage(loadView("catalogue-view.fxml"), catalogueButton);
    }

    @FXML
    private void showCustomers() {
        showPage(loadView("customer-view.fxml"), customersButton);
    }

    @FXML
    private void showReports() {
        showPlaceholder("Reports", reportsButton);
    }

    private Parent loadView(String viewName) {
        try {
            return FXMLLoader.load(Objects.requireNonNull(
                    getClass().getResource("/com/everbloom/view/" + viewName),
                    "Missing view: " + viewName
            ));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load view: " + viewName, exception);
        }
    }

    private void showPlaceholder(String pageName, Button selectedButton) {
        Label title = new Label(pageName);
        title.getStyleClass().add("page-title");

        Label message = new Label(pageName + " will be implemented in a later step.");
        message.getStyleClass().add("placeholder-message");

        VBox placeholder = new VBox(12, title, message);
        placeholder.getStyleClass().add("placeholder-page");
        showPage(placeholder, selectedButton);
    }

    private void showPage(Parent page, Button selectedButton) {
        contentArea.getChildren().setAll(page);
        navigationButtons.forEach(button -> button.getStyleClass().remove(SELECTED_STYLE_CLASS));
        selectedButton.getStyleClass().add(SELECTED_STYLE_CLASS);
    }
}
