package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Order;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.Extra;
import com.everbloom.observer.NotificationObserver;
import com.everbloom.observer.OrderViewRefreshObserver;
import com.everbloom.repository.NotificationRepository;
import com.everbloom.repository.OrderRepository;
import com.everbloom.service.NotificationService;
import com.everbloom.service.OrderService;
import com.everbloom.util.MoneyFormatter;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class OrdersController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> fulfillmentComboBox;
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
        fulfillmentComboBox.setItems(FXCollections.observableArrayList("All fulfillment", "PICKUP", "DELIVERY"));
        fulfillmentComboBox.setValue("All fulfillment");
        numberColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getOrderNumber()));
        customerColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getCustomer().getFullName()));
        statusColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getStatus()));
        totalColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(MoneyFormatter.format(cell.getValue().getTotal())));
        orderTable.getSelectionModel().selectedItemProperty().addListener((value, oldOrder, order) -> showDetails(order));
        refreshOrders();
    }

    @FXML private void refreshOrders() {
        try {
            orderTable.setItems(FXCollections.observableArrayList(orderService.findOrders(
                    searchField.getText(), statusComboBox.getValue(), fulfillmentComboBox.getValue())));
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
        StringBuilder details = new StringBuilder();
        details.append("Order: ").append(order.getOrderNumber())
                .append("\nCustomer: ").append(order.getCustomer().getFullName())
                .append("\nPlaced: ").append(order.getPlacedAt() == null ? "—" : order.getPlacedAt().toString().replace('T', ' '))
                .append("\nStatus: ").append(order.getStatus())
                .append("\nFulfillment: ").append(order.getFulfillmentType());
        if (order.getDeliveryAddress() != null) {
            details.append("\nDelivery address: ").append(order.getDeliveryAddress());
        }
        if (order.getBouquet() != null) {
            details.append("\n\nOccasion: ").append(order.getBouquet().getOccasion())
                    .append("\nWrapping: ").append(valueOrNone(order.getBouquet().getWrappingStyle()))
                    .append("\nMessage: ").append(valueOrNone(order.getBouquet().getMessage()))
                    .append("\nFlowers:");
            for (BouquetFlower flower : order.getBouquet().getFlowers()) {
                details.append("\n- ").append(flower.getQuantity()).append(" × ")
                        .append(flower.getFlower().getName()).append(" — ")
                        .append(MoneyFormatter.format(flower.getLineTotal()));
            }
            details.append("\nExtras:");
            if (order.getExtras().isEmpty()) details.append(" None");
            for (Extra extra : order.getExtras()) {
                details.append("\n- ").append(extra.getName()).append(" — ")
                        .append(MoneyFormatter.format(extra.getUnitPrice()));
            }
        } else {
            try {
                orderService.findEventPackage(order.getId()).ifPresent(eventPackage ->
                        details.append("\n\nEvent package:\n").append(eventPackage.getSummary()));
            } catch (SQLException exception) {
                details.append("\n\nUnable to load event package details.");
            }
        }
        details.append("\n\nPricing: ").append(order.getPricingPolicy())
                .append("\nSubtotal: ").append(MoneyFormatter.format(order.getSubtotal()))
                .append("\nDiscount: ").append(MoneyFormatter.format(order.getDiscount()))
                .append("\nTotal: ").append(MoneyFormatter.format(order.getTotal()));
        detailsArea.setText(details.toString());
    }
    private String valueOrNone(String value) { return value == null || value.isBlank() ? "None" : value; }
    private void showMessage(String message) { messageLabel.setText(message); messageLabel.setVisible(!message.isBlank()); messageLabel.setManaged(!message.isBlank()); }
}
