package com.everbloom.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {

    @FXML
    private TableView<RecentOrderRow> recentOrdersTable;

    @FXML
    private TableColumn<RecentOrderRow, String> orderNumberColumn;

    @FXML
    private TableColumn<RecentOrderRow, String> customerColumn;

    @FXML
    private TableColumn<RecentOrderRow, String> occasionColumn;

    @FXML
    private TableColumn<RecentOrderRow, String> statusColumn;

    @FXML
    private TableColumn<RecentOrderRow, String> amountColumn;

    @FXML
    private void initialize() {
        orderNumberColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().orderNumber()));
        customerColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().customer()));
        occasionColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().occasion()));
        statusColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().status()));
        amountColumn.setCellValueFactory(row -> new ReadOnlyStringWrapper(row.getValue().amount()));

        statusColumn.setCellFactory(column -> new StatusCell());
        recentOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        recentOrdersTable.setItems(FXCollections.observableArrayList(
                new RecentOrderRow("#EB-1048", "Nadia Rahman", "Anniversary", "Preparing", "$86.00"),
                new RecentOrderRow("#EB-1047", "Arian Chowdhury", "Birthday", "Ready", "$54.50"),
                new RecentOrderRow("#EB-1046", "Samira Khan", "Wedding", "Arranging", "$248.00"),
                new RecentOrderRow("#EB-1045", "Mahin Ahmed", "Thank You", "Delivered", "$42.00")
        ));
    }

    private record RecentOrderRow(
            String orderNumber,
            String customer,
            String occasion,
            String status,
            String amount
    ) {
    }

    private static final class StatusCell extends TableCell<RecentOrderRow, String> {

        @Override
        protected void updateItem(String status, boolean empty) {
            super.updateItem(status, empty);

            if (empty || status == null) {
                setGraphic(null);
                return;
            }

            Label badge = new Label(status);
            badge.getStyleClass().addAll("status-badge", "status-" + status.toLowerCase());
            setGraphic(badge);
        }
    }
}
