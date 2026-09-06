package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.DashboardOrder;
import com.everbloom.model.Notification;
import com.everbloom.repository.DashboardRepository;
import com.everbloom.repository.NotificationRepository;
import com.everbloom.service.DashboardService;
import com.everbloom.service.NotificationService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;

public class DashboardController {

    @FXML private Label todayOrdersLabel;
    @FXML private Label readyOrdersLabel;
    @FXML private Label todayRevenueLabel;
    @FXML private Label customerCountLabel;
    @FXML private Label messageLabel;
    @FXML private TableView<DashboardOrder> recentOrdersTable;
    @FXML private TableColumn<DashboardOrder, String> orderNumberColumn;
    @FXML private TableColumn<DashboardOrder, String> customerColumn;
    @FXML private TableColumn<DashboardOrder, String> occasionColumn;
    @FXML private TableColumn<DashboardOrder, String> statusColumn;
    @FXML private TableColumn<DashboardOrder, String> amountColumn;
    @FXML private TableView<Notification> notificationsTable;
    @FXML private TableColumn<Notification, String> notificationColumn;
    @FXML private TableColumn<Notification, String> notificationStatusColumn;

    private DashboardService dashboardService;

    @FXML
    private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        dashboardService = new DashboardService(new DashboardRepository(databaseConnection),
                new NotificationService(new NotificationRepository(databaseConnection)));
        configureTables();
        refreshDashboard();
    }

    @FXML
    private void refreshDashboard() {
        try {
            todayOrdersLabel.setText(String.valueOf(dashboardService.getTodayOrderCount()));
            readyOrdersLabel.setText(String.valueOf(dashboardService.getReadyOrderCount()));
            todayRevenueLabel.setText(formatPrice(dashboardService.getTodayRevenue()));
            customerCountLabel.setText(String.valueOf(dashboardService.getCustomerCount()));
            recentOrdersTable.setItems(FXCollections.observableArrayList(dashboardService.findRecentOrders()));
            notificationsTable.setItems(FXCollections.observableArrayList(dashboardService.findRecentNotifications()));
            showMessage("");
        } catch (SQLException exception) {
            showMessage("Unable to load dashboard data.");
        }
    }

    private void configureTables() {
        orderNumberColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrderNumber()));
        customerColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCustomerName()));
        occasionColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOccasion()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus()));
        amountColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatPrice(cell.getValue().getTotal())));
        statusColumn.setCellFactory(column -> new StatusCell<>());
        recentOrdersTable.setPlaceholder(new Label("No orders have been placed yet."));
        recentOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        notificationColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getMessage()));
        notificationStatusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus()));
        notificationStatusColumn.setCellFactory(column -> new StatusCell<>());
        notificationsTable.setPlaceholder(new Label("Status notifications will appear here."));
        notificationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private String formatPrice(long value) { return "BDT " + value; }
    private void showMessage(String message) { messageLabel.setText(message); messageLabel.setVisible(!message.isBlank()); messageLabel.setManaged(!message.isBlank()); }

    private static final class StatusCell<T> extends TableCell<T, String> {
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
