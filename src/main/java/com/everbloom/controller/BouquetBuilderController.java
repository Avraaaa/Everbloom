package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Bouquet;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.Customer;
import com.everbloom.model.Flower;
import com.everbloom.repository.CustomerRepository;
import com.everbloom.repository.FlowerRepository;
import com.everbloom.service.CustomerService;
import com.everbloom.service.FlowerService;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.StringConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BouquetBuilderController {

    @FXML
    private ComboBox<Customer> customerComboBox;

    @FXML
    private ComboBox<String> occasionComboBox;

    @FXML
    private ComboBox<String> wrappingComboBox;

    @FXML
    private TextArea messageTextArea;

    @FXML
    private ComboBox<Flower> flowerComboBox;

    @FXML
    private Spinner<Integer> flowerQuantitySpinner;

    @FXML
    private TableView<BouquetFlower> selectedFlowerTable;

    @FXML
    private TableColumn<BouquetFlower, String> selectedFlowerNameColumn;

    @FXML
    private TableColumn<BouquetFlower, Integer> selectedFlowerQuantityColumn;

    @FXML
    private TableColumn<BouquetFlower, String> selectedFlowerSubtotalColumn;

    @FXML
    private TextArea bouquetSummaryArea;

    @FXML
    private Label bouquetSubtotalLabel;

    @FXML
    private Label messageLabel;

    private CustomerService customerService;
    private FlowerService flowerService;
    private BouquetBuilder bouquetBuilder;

    @FXML
    private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        customerService = new CustomerService(new CustomerRepository(databaseConnection));
        flowerService = new FlowerService(new FlowerRepository(databaseConnection));
        bouquetBuilder = new BouquetBuilder();

        configureControls();
        configureSelectedFlowerTable();
        loadOptions();
        showEmptySummary();
    }

    @FXML
    private void updateBouquetOptions() {
        synchronizeBuilderOptions();
        updateSummaryIfComplete();
    }

    @FXML
    private void addFlower() {
        Flower selectedFlower = flowerComboBox.getValue();
        try {
            bouquetBuilder.addFlower(selectedFlower, flowerQuantitySpinner.getValue());
            refreshSelectedFlowers();
            updateSummaryIfComplete();
            showMessage("");
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }
    }

    @FXML
    private void removeSelectedFlower() {
        BouquetFlower selectedFlower = selectedFlowerTable.getSelectionModel().getSelectedItem();
        if (selectedFlower == null) {
            showMessage("Select a flower from the bouquet to remove.");
            return;
        }

        bouquetBuilder.removeFlower(selectedFlower.getFlower());
        refreshSelectedFlowers();
        updateSummaryIfComplete();
        showMessage("");
    }

    @FXML
    private void buildBouquet() {
        synchronizeBuilderOptions();
        try {
            Bouquet bouquet = bouquetBuilder.build();
            showBouquetSummary(bouquet);
            showMessage("Bouquet built and ready for the next order step.");
        } catch (IllegalArgumentException exception) {
            showEmptySummary();
            showMessage(exception.getMessage());
        }
    }

    private void configureControls() {
        occasionComboBox.setItems(FXCollections.observableArrayList(
                "Birthday", "Anniversary", "Thank You", "Congratulations", "Just Because"
        ));
        wrappingComboBox.setItems(FXCollections.observableArrayList(
                "No wrapping", "Kraft paper", "Premium paper", "White paper"
        ));
        wrappingComboBox.setValue("No wrapping");
        flowerQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));

        customerComboBox.setConverter(customerConverter());
        flowerComboBox.setConverter(flowerConverter());
        flowerComboBox.valueProperty().addListener((observable, oldFlower, flower) -> configureQuantitySpinner(flower));
    }

    private void configureSelectedFlowerTable() {
        selectedFlowerNameColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getFlower().getName()));
        selectedFlowerQuantityColumn.setCellValueFactory(cell ->
                new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        selectedFlowerSubtotalColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatPrice(cell.getValue().getLineTotal())));
        selectedFlowerTable.setPlaceholder(new Label("Add flowers to begin your bouquet."));
        selectedFlowerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void loadOptions() {
        try {
            customerComboBox.setItems(FXCollections.observableArrayList(customerService.findAll()));
            flowerComboBox.setItems(FXCollections.observableArrayList(findAvailableFlowers()));
            if (customerComboBox.getItems().isEmpty()) {
                showMessage("Add a customer before building a bouquet.");
            } else if (flowerComboBox.getItems().isEmpty()) {
                showMessage("Add an active flower with stock before building a bouquet.");
            }
        } catch (SQLException exception) {
            showMessage("Unable to load customers and flowers. Please try again.");
        }
    }

    private List<Flower> findAvailableFlowers() throws SQLException {
        List<Flower> availableFlowers = new ArrayList<>();
        for (Flower flower : flowerService.findAll()) {
            if (flower.isActive() && flower.getStockQuantity() > 0) {
                availableFlowers.add(flower);
            }
        }
        return availableFlowers;
    }

    private void synchronizeBuilderOptions() {
        bouquetBuilder.forCustomer(customerComboBox.getValue())
                .forOccasion(occasionComboBox.getValue())
                .withWrapping(wrappingComboBox.getValue())
                .withMessage(messageTextArea.getText());
    }

    private void refreshSelectedFlowers() {
        selectedFlowerTable.setItems(FXCollections.observableArrayList(bouquetBuilder.getSelectedFlowers()));
    }

    private void updateSummaryIfComplete() {
        try {
            showBouquetSummary(bouquetBuilder.build());
        } catch (IllegalArgumentException exception) {
            showEmptySummary();
        }
    }

    private void showBouquetSummary(Bouquet bouquet) {
        bouquetSummaryArea.setText(bouquet.getSummary());
        bouquetSubtotalLabel.setText(formatPrice(bouquet.getFlowerSubtotal()));
    }

    private void showEmptySummary() {
        bouquetSummaryArea.setText("Select a customer and occasion, then add flowers to preview the bouquet.");
        bouquetSubtotalLabel.setText("BDT 0");
    }

    private void configureQuantitySpinner(Flower flower) {
        int maximum = flower == null ? 1 : Math.max(1, flower.getStockQuantity());
        flowerQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maximum, 1));
    }

    private StringConverter<Customer> customerConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
                return customer == null ? "" : customer.getFullName() + " - " + customer.getPhone();
            }

            @Override
            public Customer fromString(String value) {
                return null;
            }
        };
    }

    private StringConverter<Flower> flowerConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Flower flower) {
                return flower == null ? "" : flower.getName() + " (" + flower.getStockQuantity() + " available)";
            }

            @Override
            public Flower fromString(String value) {
                return null;
            }
        };
    }

    private String formatPrice(long price) {
        return "BDT " + price;
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(!message.isBlank());
        messageLabel.setManaged(!message.isBlank());
    }
}
