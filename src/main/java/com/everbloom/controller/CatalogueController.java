package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.model.Extra;
import com.everbloom.model.Flower;
import com.everbloom.repository.BouquetTemplateRepository;
import com.everbloom.repository.ExtraRepository;
import com.everbloom.repository.FlowerRepository;
import com.everbloom.service.BouquetTemplateService;
import com.everbloom.service.ExtraService;
import com.everbloom.service.FlowerService;
import com.everbloom.util.MoneyFormatter;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.Optional;

public class CatalogueController {

    @FXML
    private TextField flowerSearchField;

    @FXML
    private TableView<Flower> flowerTable;

    @FXML
    private TableColumn<Flower, String> flowerNameColumn;

    @FXML
    private TableColumn<Flower, String> flowerColorColumn;

    @FXML
    private TableColumn<Flower, String> flowerPriceColumn;

    @FXML
    private TableColumn<Flower, Integer> flowerStockColumn;

    @FXML
    private TableColumn<Flower, String> flowerStatusColumn;

    @FXML
    private TextField extraSearchField;

    @FXML
    private TableView<Extra> extraTable;

    @FXML
    private TableColumn<Extra, String> extraNameColumn;

    @FXML
    private TableColumn<Extra, String> extraDescriptionColumn;

    @FXML
    private TableColumn<Extra, String> extraPriceColumn;

    @FXML
    private TableColumn<Extra, String> extraStatusColumn;

    @FXML
    private TableView<BouquetTemplate> templateTable;

    @FXML
    private TableColumn<BouquetTemplate, String> templateNameColumn;

    @FXML
    private TableColumn<BouquetTemplate, String> templateOccasionColumn;

    @FXML
    private TableColumn<BouquetTemplate, String> templateFlowersColumn;

    @FXML
    private Label messageLabel;

    private FlowerService flowerService;
    private ExtraService extraService;
    private BouquetTemplateService templateService;

    @FXML
    private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        flowerService = new FlowerService(new FlowerRepository(databaseConnection));
        extraService = new ExtraService(new ExtraRepository(databaseConnection));
        templateService = new BouquetTemplateService(new BouquetTemplateRepository(databaseConnection));

        configureFlowerTable();
        configureExtraTable();
        configureTemplateTable();
        loadFlowers();
        loadExtras();
        loadTemplates();
    }

    @FXML
    private void searchFlowers() {
        try {
            flowerTable.setItems(FXCollections.observableArrayList(flowerService.search(flowerSearchField.getText())));
            showMessage("");
        } catch (SQLException exception) {
            flowerTable.getItems().clear();
            showMessage("Unable to search flowers. Please try again.");
        }
    }

    @FXML
    private void searchExtras() {
        try {
            extraTable.setItems(FXCollections.observableArrayList(extraService.search(extraSearchField.getText())));
            showMessage("");
        } catch (SQLException exception) {
            extraTable.getItems().clear();
            showMessage("Unable to search extras. Please try again.");
        }
    }

    @FXML
    private void addFlower() {
        showFlowerDialog(null).ifPresent(flower -> {
            try {
                flowerService.create(flower);
                loadFlowers();
                showMessage("Flower added successfully.");
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to add the flower."));
            }
        });
    }

    @FXML
    private void editFlower() {
        Flower selectedFlower = flowerTable.getSelectionModel().getSelectedItem();
        if (selectedFlower == null) {
            showMessage("Select a flower to edit.");
            return;
        }

        showFlowerDialog(selectedFlower).ifPresent(flower -> {
            try {
                flowerService.update(flower);
                loadFlowers();
                showMessage("Flower updated successfully.");
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to update the flower."));
            }
        });
    }

    @FXML
    private void deleteFlower() {
        Flower selectedFlower = flowerTable.getSelectionModel().getSelectedItem();
        if (selectedFlower == null) {
            showMessage("Select a flower to delete.");
            return;
        }

        if (!confirmDeletion("flower", selectedFlower.getName())) {
            return;
        }

        try {
            if (flowerService.delete(selectedFlower.getId())) {
                loadFlowers();
                showMessage("Flower deleted successfully.");
            } else {
                showMessage("The selected flower no longer exists.");
            }
        } catch (SQLException exception) {
            showMessage("This flower cannot be deleted because it is used by another record.");
        }
    }

    @FXML
    private void addExtra() {
        showExtraDialog(null).ifPresent(extra -> {
            try {
                extraService.create(extra);
                loadExtras();
                showMessage("Extra added successfully.");
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to add the extra."));
            }
        });
    }

    @FXML
    private void editExtra() {
        Extra selectedExtra = extraTable.getSelectionModel().getSelectedItem();
        if (selectedExtra == null) {
            showMessage("Select an extra to edit.");
            return;
        }

        showExtraDialog(selectedExtra).ifPresent(extra -> {
            try {
                extraService.update(extra);
                loadExtras();
                showMessage("Extra updated successfully.");
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to update the extra."));
            }
        });
    }

    @FXML
    private void deleteExtra() {
        Extra selectedExtra = extraTable.getSelectionModel().getSelectedItem();
        if (selectedExtra == null) {
            showMessage("Select an extra to delete.");
            return;
        }

        if (!confirmDeletion("extra", selectedExtra.getName())) {
            return;
        }

        try {
            if (extraService.delete(selectedExtra.getId())) {
                loadExtras();
                showMessage("Extra deleted successfully.");
            } else {
                showMessage("The selected extra no longer exists.");
            }
        } catch (SQLException exception) {
            showMessage("This extra cannot be deleted because it is used by another record.");
        }
    }

    @FXML
    private void addTemplate() {
        showTemplateDialog(null).ifPresent(template -> {
            try {
                templateService.create(template);
                loadTemplates();
                showMessage("Bouquet template added successfully.");
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to add the bouquet template."));
            }
        });
    }

    @FXML
    private void editTemplate() {
        BouquetTemplate selectedTemplate = templateTable.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            showMessage("Select a bouquet template to edit.");
            return;
        }
        showTemplateDialog(selectedTemplate).ifPresent(template -> {
            try {
                if (templateService.update(template)) {
                    loadTemplates();
                    showMessage("Bouquet template updated successfully.");
                } else {
                    showMessage("The selected bouquet template no longer exists.");
                }
            } catch (IllegalArgumentException | SQLException exception) {
                showMessage(readableMessage(exception, "Unable to update the bouquet template."));
            }
        });
    }

    private void configureFlowerTable() {
        flowerNameColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getName()));
        flowerColorColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(valueOrDash(cell.getValue().getColor())));
        flowerPriceColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(formatPrice(cell.getValue().getUnitPrice())));
        flowerStockColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyObjectWrapper<>(cell.getValue().getStockQuantity()));
        flowerStatusColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().isActive() ? "Active" : "Inactive"));
        flowerTable.setPlaceholder(new Label("No flowers match this search."));
        flowerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void configureExtraTable() {
        extraNameColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getName()));
        extraDescriptionColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(valueOrDash(cell.getValue().getDescription())));
        extraPriceColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(formatPrice(cell.getValue().getUnitPrice())));
        extraStatusColumn.setCellValueFactory(cell -> new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().isActive() ? "Active" : "Inactive"));
        extraTable.setPlaceholder(new Label("No extras match this search."));
        extraTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void configureTemplateTable() {
        templateNameColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getName()));
        templateOccasionColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getOccasion()));
        templateFlowersColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(cell.getValue().getFlowerSummary()));
        templateTable.setPlaceholder(new Label("No bouquet templates are available."));
        templateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void loadFlowers() {
        flowerSearchField.clear();
        searchFlowers();
    }

    private void loadExtras() {
        extraSearchField.clear();
        searchExtras();
    }

    private void loadTemplates() {
        try {
            templateTable.setItems(FXCollections.observableArrayList(templateService.findAll()));
        } catch (SQLException exception) {
            templateTable.getItems().clear();
            showMessage("Unable to load bouquet templates.");
        }
    }

    private Optional<Flower> showFlowerDialog(Flower flower) {
        TextField nameField = new TextField(flower == null ? "" : flower.getName());
        TextField colorField = new TextField(flower == null ? "" : valueOrEmpty(flower.getColor()));
        TextField priceField = new TextField(flower == null ? "" : formatEditablePrice(flower.getUnitPrice()));
        TextField stockField = new TextField(flower == null ? "" : String.valueOf(flower.getStockQuantity()));
        CheckBox activeCheckBox = new CheckBox("Available for new bouquets");
        activeCheckBox.setSelected(flower == null || flower.isActive());

        GridPane form = createForm();
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Color"), colorField);
        form.addRow(2, new Label("Price (BDT)"), priceField);
        form.addRow(3, new Label("Stock quantity"), stockField);
        form.add(activeCheckBox, 1, 4);

        DialogResult<Flower> dialogResult = showFormDialog(
                flower == null ? "Add Flower" : "Edit Flower",
                form,
                () -> new Flower(
                        flower == null ? 0 : flower.getId(),
                        nameField.getText(),
                        colorField.getText(),
                        parsePrice(priceField.getText()),
                        parseStockQuantity(stockField.getText()),
                        activeCheckBox.isSelected()
                )
        );
        return dialogResult.value();
    }

    private Optional<Extra> showExtraDialog(Extra extra) {
        TextField nameField = new TextField(extra == null ? "" : extra.getName());
        TextField descriptionField = new TextField(extra == null ? "" : valueOrEmpty(extra.getDescription()));
        TextField priceField = new TextField(extra == null ? "" : formatEditablePrice(extra.getUnitPrice()));
        CheckBox activeCheckBox = new CheckBox("Available for new bouquets");
        activeCheckBox.setSelected(extra == null || extra.isActive());

        GridPane form = createForm();
        form.addRow(0, new Label("Name"), nameField);
        form.addRow(1, new Label("Description"), descriptionField);
        form.addRow(2, new Label("Price (BDT)"), priceField);
        form.add(activeCheckBox, 1, 3);

        DialogResult<Extra> dialogResult = showFormDialog(
                extra == null ? "Add Extra" : "Edit Extra",
                form,
                () -> new Extra(
                        extra == null ? 0 : extra.getId(),
                        nameField.getText(),
                        descriptionField.getText(),
                        parsePrice(priceField.getText()),
                        activeCheckBox.isSelected()
                )
        );
        return dialogResult.value();
    }

    private Optional<BouquetTemplate> showTemplateDialog(BouquetTemplate template) {
        TextField nameField = new TextField(template == null ? "" : template.getName());
        TextField occasionField = new TextField(template == null ? "" : template.getOccasion());
        TextField wrappingField = new TextField(template == null ? "" : valueOrEmpty(template.getWrappingStyle()));
        TextField messageField = new TextField(template == null ? "" : valueOrEmpty(template.getMessage()));
        ListView<BouquetFlower> compositionList = new ListView<>(FXCollections.observableArrayList(
                template == null ? java.util.List.of() : template.getFlowers()));
        compositionList.setPrefHeight(150);
        compositionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(BouquetFlower item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getQuantity() + " × " + item.getFlower().getName());
            }
        });

        ComboBox<Flower> flowerComboBox = new ComboBox<>();
        flowerComboBox.setPromptText("Choose flower");
        flowerComboBox.setMaxWidth(Double.MAX_VALUE);
        flowerComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Flower flower) { return flower == null ? "" : flower.getName(); }
            @Override public Flower fromString(String value) { return null; }
        });
        Spinner<Integer> quantitySpinner = new Spinner<>();
        quantitySpinner.setPrefWidth(90);
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));
        flowerComboBox.valueProperty().addListener((observable, oldFlower, flower) -> {
            int maximum = flower == null ? 1 : Math.max(1, flower.getStockQuantity());
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maximum, 1));
        });
        try {
            flowerComboBox.setItems(FXCollections.observableArrayList(findActiveFlowers()));
        } catch (SQLException exception) {
            showMessage("Unable to load flowers for the template.");
            return Optional.empty();
        }

        Label dialogMessage = new Label();
        Button addFlowerButton = new Button("Add Flower");
        addFlowerButton.setOnAction(event -> {
            Flower flower = flowerComboBox.getValue();
            if (flower == null) {
                dialogMessage.setText("Select a flower to add.");
                return;
            }
            compositionList.getItems().removeIf(line -> line.getFlower().getId() == flower.getId());
            compositionList.getItems().add(new BouquetFlower(flower, quantitySpinner.getValue()));
            dialogMessage.setText("");
        });
        Button removeFlowerButton = new Button("Remove Selected");
        removeFlowerButton.setOnAction(event -> {
            BouquetFlower selectedFlower = compositionList.getSelectionModel().getSelectedItem();
            if (selectedFlower == null) {
                dialogMessage.setText("Select a template flower to remove.");
                return;
            }
            compositionList.getItems().remove(selectedFlower);
            dialogMessage.setText("");
        });

        GridPane details = createForm();
        details.addRow(0, new Label("Name"), nameField);
        details.addRow(1, new Label("Occasion"), occasionField);
        details.addRow(2, new Label("Wrapping"), wrappingField);
        details.addRow(3, new Label("Message"), messageField);
        HBox flowerControls = new HBox(8, flowerComboBox, quantitySpinner, addFlowerButton);
        VBox content = new VBox(10, details, new Label("Flower composition"), flowerControls,
                compositionList, removeFlowerButton, dialogMessage);
        content.setPrefWidth(520);

        javafx.scene.control.Dialog<BouquetTemplate> dialog = new javafx.scene.control.Dialog<>();
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.setTitle(template == null ? "Add Bouquet Template" : "Edit Bouquet Template");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().isBlank() || occasionField.getText().isBlank()
                    || compositionList.getItems().isEmpty()) {
                dialogMessage.setText("Enter a name and occasion, then add at least one flower.");
                event.consume();
            }
        });
        dialog.setResultConverter(button -> button == saveButtonType
                ? new BouquetTemplate(template == null ? 0 : template.getId(), nameField.getText(),
                occasionField.getText(), wrappingField.getText(), messageField.getText(),
                java.util.List.copyOf(compositionList.getItems()))
                : null);
        return dialog.showAndWait();
    }

    private java.util.List<Flower> findActiveFlowers() throws SQLException {
        java.util.List<Flower> activeFlowers = new java.util.ArrayList<>();
        for (Flower flower : flowerService.findAll()) {
            if (flower.isActive() && flower.getStockQuantity() > 0) activeFlowers.add(flower);
        }
        return activeFlowers;
    }

    private <T> DialogResult<T> showFormDialog(String title, GridPane form, FormValueSupplier<T> valueSupplier) {
        javafx.scene.control.Dialog<T> dialog = new javafx.scene.control.Dialog<>();
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.setTitle(title);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        FormValue<T> formValue = new FormValue<>();
        saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                formValue.value = valueSupplier.get();
            } catch (IllegalArgumentException exception) {
                dialog.setHeaderText(exception.getMessage());
                event.consume();
            }
        });
        dialog.setResultConverter(buttonType -> buttonType == saveButtonType ? formValue.value : null);
        return new DialogResult<>(dialog.showAndWait());
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPrefWidth(360);
        return form;
    }

    private boolean confirmDeletion(String itemType, String itemName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete " + itemType);
        alert.setHeaderText("Delete " + itemName + "?");
        alert.setContentText("This action cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private long parsePrice(String priceText) {
        try {
            BigDecimal price = new BigDecimal(priceText.trim()).setScale(2, RoundingMode.UNNECESSARY);
            long minorUnits = price.movePointRight(2).longValueExact();
            if (minorUnits < 0) {
                throw new IllegalArgumentException("Price cannot be negative.");
            }
            return minorUnits;
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalArgumentException("Enter a valid price with up to two decimal places.");
        }
    }

    private int parseStockQuantity(String quantityText) {
        try {
            int quantity = Integer.parseInt(quantityText.trim());
            if (quantity < 0) {
                throw new IllegalArgumentException("Stock quantity cannot be negative.");
            }
            return quantity;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a whole stock quantity.");
        }
    }

    private String formatPrice(long minorUnits) {
        return MoneyFormatter.format(minorUnits);
    }

    private String formatEditablePrice(long minorUnits) {
        return MoneyFormatter.formatAmount(minorUnits);
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(!message.isBlank());
        messageLabel.setManaged(!message.isBlank());
    }

    private String readableMessage(Exception exception, String fallbackMessage) {
        return exception.getMessage() == null || exception.getMessage().isBlank() ? fallbackMessage : exception.getMessage();
    }

    @FunctionalInterface
    private interface FormValueSupplier<T> {

        T get();
    }

    private static class FormValue<T> {

        private T value;
    }

    private record DialogResult<T>(Optional<T> value) {
    }
}
