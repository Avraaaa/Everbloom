package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Customer;
import com.everbloom.repository.CustomerRepository;
import com.everbloom.service.CustomerService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.sql.SQLException;
import java.util.Optional;

public class CustomerController {

    @FXML
    private TextField customerSearchField;

    @FXML
    private TableView<Customer> customerTable;

    @FXML
    private TableColumn<Customer, String> customerNameColumn;

    @FXML
    private TableColumn<Customer, String> customerPhoneColumn;

    @FXML
    private TableColumn<Customer, String> customerEmailColumn;

    @FXML
    private Label messageLabel;

    @FXML
    private Label detailNameLabel;

    @FXML
    private Label detailPhoneLabel;

    @FXML
    private Label detailEmailLabel;

    @FXML
    private Label detailAddressLabel;

    private CustomerService customerService;

    @FXML
    private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        customerService = new CustomerService(new CustomerRepository(databaseConnection));

        configureCustomerTable();
        customerTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldCustomer, customer) -> showCustomerDetails(customer)
        );
        loadCustomers();
    }

    @FXML
    private void searchCustomers() {
        try {
            customerTable.setItems(FXCollections.observableArrayList(customerService.search(customerSearchField.getText())));
            showMessage("");
        } catch (SQLException exception) {
            customerTable.getItems().clear();
            showCustomerDetails(null);
            showMessage("Unable to search customers. Please try again.");
        }
    }

    @FXML
    private void addCustomer() {
        saveCustomerWithDialog(null).ifPresent(customer -> {
            loadCustomers();
            selectCustomer(customer.getId());
            showMessage("Customer added successfully.");
        });
    }

    @FXML
    private void editCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showMessage("Select a customer to edit.");
            return;
        }

        saveCustomerWithDialog(selectedCustomer).ifPresent(customer -> {
            loadCustomers();
            selectCustomer(customer.getId());
            showMessage("Customer updated successfully.");
        });
    }

    @FXML
    private void deleteCustomer() {
        Customer selectedCustomer = customerTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showMessage("Select a customer to delete.");
            return;
        }

        if (!confirmDeletion(selectedCustomer.getFullName())) {
            return;
        }

        try {
            if (customerService.delete(selectedCustomer.getId())) {
                loadCustomers();
                showMessage("Customer deleted successfully.");
            } else {
                showMessage("The selected customer no longer exists.");
            }
        } catch (SQLException exception) {
            showMessage("This customer cannot be deleted because existing orders use this record.");
        }
    }

    private void configureCustomerTable() {
        customerNameColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getFullName()));
        customerPhoneColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getPhone()));
        customerEmailColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(valueOrDash(cell.getValue().getEmail())));
        customerTable.setPlaceholder(new Label("No customers match this search."));
        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void loadCustomers() {
        customerSearchField.clear();
        showCustomerDetails(null);
        searchCustomers();
    }

    private Optional<Customer> saveCustomerWithDialog(Customer customer) {
        TextField nameField = new TextField(customer == null ? "" : customer.getFullName());
        TextField phoneField = new TextField(customer == null ? "" : customer.getPhone());
        TextField emailField = new TextField(customer == null ? "" : valueOrEmpty(customer.getEmail()));
        TextArea addressArea = new TextArea(customer == null ? "" : valueOrEmpty(customer.getAddress()));
        addressArea.setPrefRowCount(3);

        GridPane form = createForm();
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Phone"), phoneField);
        form.addRow(2, new Label("Email"), emailField);
        form.addRow(3, new Label("Address"), addressArea);

        Dialog<Customer> dialog = new Dialog<>();
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.setTitle(customer == null ? "Add Customer" : "Edit Customer");
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        FormValue<Customer> formValue = new FormValue<>();
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            Customer formCustomer = new Customer(
                    customer == null ? 0 : customer.getId(),
                    nameField.getText(),
                    phoneField.getText(),
                    emailField.getText(),
                    addressArea.getText()
            );
            try {
                if (customer == null) {
                    customerService.create(formCustomer);
                } else if (!customerService.update(formCustomer)) {
                    dialog.setHeaderText("This customer no longer exists.");
                    event.consume();
                    return;
                }
                formValue.value = formCustomer;
            } catch (IllegalArgumentException | SQLException exception) {
                dialog.setHeaderText(readableMessage(exception, "Unable to save the customer."));
                event.consume();
            }
        });
        dialog.setResultConverter(buttonType -> buttonType == saveButtonType ? formValue.value : null);
        return dialog.showAndWait();
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPrefWidth(380);
        return form;
    }

    private boolean confirmDeletion(String customerName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Customer");
        alert.setHeaderText("Delete " + customerName + "?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void selectCustomer(long customerId) {
        for (Customer customer : customerTable.getItems()) {
            if (customer.getId() == customerId) {
                customerTable.getSelectionModel().select(customer);
                return;
            }
        }
    }

    private void showCustomerDetails(Customer customer) {
        if (customer == null) {
            detailNameLabel.setText("Select a customer");
            detailPhoneLabel.setText("—");
            detailEmailLabel.setText("—");
            detailAddressLabel.setText("—");
            return;
        }

        detailNameLabel.setText(customer.getFullName());
        detailPhoneLabel.setText(customer.getPhone());
        detailEmailLabel.setText(valueOrDash(customer.getEmail()));
        detailAddressLabel.setText(valueOrDash(customer.getAddress()));
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(!message.isBlank());
        messageLabel.setManaged(!message.isBlank());
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String readableMessage(Exception exception, String fallbackMessage) {
        String message = exception.getMessage();
        if (message != null && message.contains("UNIQUE constraint failed: customers.phone")) {
            return "A customer with this phone number already exists.";
        }
        return message == null || message.isBlank() ? fallbackMessage : message;
    }

    private static class FormValue<T> {

        private T value;
    }
}
