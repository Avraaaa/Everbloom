package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Order;
import com.everbloom.observer.NotificationObserver;
import com.everbloom.observer.OrderViewRefreshObserver;
import com.everbloom.repository.NotificationRepository;
import com.everbloom.repository.OrderRepository;
import com.everbloom.service.NotificationService;
import com.everbloom.service.OrderService;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class OrdersController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, String> numberColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> totalColumn;
    @FXML private TextArea detailsArea;
    @FXML private Label messageLabel;
    private OrderService orderService;

    @FXML private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        orderService = new OrderService(new OrderRepository(databaseConnection));
        orderService.addObserver(new NotificationObserver(new NotificationService(new NotificationRepository(databaseConnection))));
        orderService.addObserver(new OrderViewRefreshObserver(this::refreshOrders));
        statusComboBox.setItems(FXCollections.observableArrayList("All statuses", "ORDERED", "PREPARING", "ARRANGING", "READY", "DELIVERED"));
        statusComboBox.setValue("All statuses");
        numberColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrderNumber()));
        customerColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCustomer().getFullName()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus()));
        totalColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper("BDT " + cell.getValue().getTotal()));
        orderTable.getSelectionModel().selectedItemProperty().addListener((value, oldOrder, order) -> showDetails(order));
        refreshOrders();
    }

    @FXML private void refreshOrders() {
        try {
            orderTable.setItems(FXCollections.observableArrayList(orderService.findOrders(searchField.getText(), statusComboBox.getValue())));
            showMessage("");
        } catch (SQLException exception) { showMessage("Unable to load orders."); }
    }

    @FXML private void advanceOrder() {
        try {
            orderService.advanceOrder(orderTable.getSelectionModel().getSelectedItem());
        } catch (IllegalArgumentException | IllegalStateException exception) { showMessage(exception.getMessage()); }
        catch (SQLException exception) { showMessage("Unable to update the order."); }
    }

    private void showDetails(Order order) {
        if (order == null) { detailsArea.clear(); return; }
        detailsArea.setText("Order: " + order.getOrderNumber() + "\nCustomer: " + order.getCustomer().getFullName() + "\nStatus: " + order.getStatus() + "\nFulfillment: " + order.getFulfillmentType() + "\nPricing: " + order.getPricingPolicy() + "\nSubtotal: BDT " + order.getSubtotal() + "\nDiscount: BDT " + order.getDiscount() + "\nTotal: BDT " + order.getTotal());
    }
    private void showMessage(String message) { messageLabel.setText(message); messageLabel.setVisible(!message.isBlank()); messageLabel.setManaged(!message.isBlank()); }
}
