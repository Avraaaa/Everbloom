package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.Bouquet;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.BouquetItem;
import com.everbloom.model.BouquetFlower;
import com.everbloom.model.BouquetTemplate;
import com.everbloom.model.BaseBouquet;
import com.everbloom.model.Customer;
import com.everbloom.model.Extra;
import com.everbloom.model.ExtraDecorator;
import com.everbloom.model.Flower;
import com.everbloom.model.Order;
import com.everbloom.pricing.LoyaltyPricingStrategy;
import com.everbloom.pricing.PricingStrategy;
import com.everbloom.pricing.StandardPricingStrategy;
import com.everbloom.repository.CustomerRepository;
import com.everbloom.repository.ExtraRepository;
import com.everbloom.repository.FlowerRepository;
import com.everbloom.repository.BouquetTemplateRepository;
import com.everbloom.repository.OrderRepository;
import com.everbloom.service.CustomerService;
import com.everbloom.service.ExtraService;
import com.everbloom.service.FlowerService;
import com.everbloom.service.BouquetTemplateService;
import com.everbloom.service.PricingService;
import com.everbloom.service.OrderService;
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
    private ComboBox<BouquetTemplate> templateComboBox;

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
    private ComboBox<Extra> extraComboBox;

    @FXML
    private ComboBox<String> pricingPolicyComboBox;

    @FXML
    private ComboBox<String> fulfillmentComboBox;

    @FXML
    private TextArea deliveryAddressTextArea;

    @FXML
    private TableView<Extra> selectedExtraTable;

    @FXML
    private TableColumn<Extra, String> selectedExtraNameColumn;

    @FXML
    private TableColumn<Extra, String> selectedExtraPriceColumn;

    @FXML
    private TextArea bouquetSummaryArea;

    @FXML
    private Label bouquetSubtotalLabel;

    @FXML
    private Label selectedPricingPolicyLabel;

    @FXML
    private Label discountLabel;

    @FXML
    private Label finalTotalLabel;

    @FXML
    private Label messageLabel;

    private CustomerService customerService;
    private FlowerService flowerService;
    private ExtraService extraService;
    private BouquetTemplateService templateService;
    private PricingService pricingService;
    private OrderService orderService;
    private BouquetBuilder bouquetBuilder;
    private final List<Extra> selectedExtras = new ArrayList<>();

    @FXML
    private void initialize() {
        DatabaseConnection databaseConnection = new DatabaseConnection();
        customerService = new CustomerService(new CustomerRepository(databaseConnection));
        flowerService = new FlowerService(new FlowerRepository(databaseConnection));
        extraService = new ExtraService(new ExtraRepository(databaseConnection));
        templateService = new BouquetTemplateService(new BouquetTemplateRepository(databaseConnection));
        pricingService = new PricingService();
        orderService = new OrderService(new OrderRepository(databaseConnection));
        bouquetBuilder = new BouquetBuilder();

        configureControls();
        configureSelectedFlowerTable();
        configureSelectedExtraTable();
        loadOptions();
        showEmptySummary();
    }

    @FXML
    private void updateBouquetOptions() {
        synchronizeBuilderOptions();
        updateSummaryIfComplete();
    }

    @FXML
    private void updatePricing() {
        synchronizeBuilderOptions();
        updateSummaryIfComplete();
    }

    @FXML
    private void applyTemplate() {
        try {
            BouquetTemplate template = templateComboBox.getValue();
            bouquetBuilder = templateService.copyToBuilder(template);
            bouquetBuilder.forCustomer(customerComboBox.getValue());
            occasionComboBox.setValue(template.getOccasion());
            wrappingComboBox.setValue(template.getWrappingStyle());
            messageTextArea.setText(template.getMessage());
            selectedExtras.clear();
            refreshSelectedFlowers();
            refreshSelectedExtras();
            updateSummaryIfComplete();
            showMessage("Template copied. You can now customize this bouquet.");
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        }
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
    private void addExtra() {
        Extra selectedExtra = extraComboBox.getValue();
        if (selectedExtra == null) {
            showMessage("Select an extra to add.");
            return;
        }
        if (isExtraSelected(selectedExtra)) {
            showMessage("This extra is already part of the bouquet.");
            return;
        }

        selectedExtras.add(selectedExtra);
        refreshSelectedExtras();
        updateSummaryIfComplete();
        showMessage("");
    }

    @FXML
    private void removeSelectedExtra() {
        Extra selectedExtra = selectedExtraTable.getSelectionModel().getSelectedItem();
        if (selectedExtra == null) {
            showMessage("Select an extra from the bouquet to remove.");
            return;
        }

        selectedExtras.remove(selectedExtra);
        refreshSelectedExtras();
        updateSummaryIfComplete();
        showMessage("");
    }

    @FXML
    private void buildBouquet() {
        synchronizeBuilderOptions();
        try {
            Bouquet bouquet = bouquetBuilder.build();
            showBouquetSummary(bouquet, decorateBouquet(bouquet));
            showMessage("Bouquet built and ready for the next order step.");
        } catch (IllegalArgumentException exception) {
            showEmptySummary();
            showMessage(exception.getMessage());
        }
    }

    @FXML
    private void placeOrder() {
        synchronizeBuilderOptions();
        try {
            Bouquet bouquet = bouquetBuilder.build();
            BouquetItem bouquetItem = decorateBouquet(bouquet);
            PricingStrategy pricingStrategy = getSelectedPricingStrategy();
            long subtotal = bouquetItem.getSubtotal();
            long discount = pricingService.calculateDiscount(bouquetItem, pricingStrategy);
            long total = pricingService.calculateFinalTotal(bouquetItem, pricingStrategy);
            Order order = new Order(createOrderNumber(), bouquet.getCustomer(), bouquet, selectedExtras,
                    fulfillmentComboBox.getValue(), getDeliveryAddress(), pricingStrategy.getName(),
                    subtotal, discount, total);
            Order savedOrder = orderService.placeOrder(order);
            resetBuilder();
            showMessage("Order " + savedOrder.getOrderNumber() + " placed successfully.");
        } catch (IllegalArgumentException exception) {
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            showMessage("Unable to place the order. Please try again.");
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
        pricingPolicyComboBox.setItems(FXCollections.observableArrayList("Standard Pricing", "Loyalty Pricing"));
        pricingPolicyComboBox.setValue("Standard Pricing");
        fulfillmentComboBox.setItems(FXCollections.observableArrayList("PICKUP", "DELIVERY"));
        fulfillmentComboBox.setValue("PICKUP");
        flowerQuantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1, 1));

        customerComboBox.setConverter(customerConverter());
        templateComboBox.setConverter(templateConverter());
        flowerComboBox.setConverter(flowerConverter());
        extraComboBox.setConverter(extraConverter());
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

    private void configureSelectedExtraTable() {
        selectedExtraNameColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(cell.getValue().getName()));
        selectedExtraPriceColumn.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(formatPrice(cell.getValue().getUnitPrice())));
        selectedExtraTable.setPlaceholder(new Label("Optional extras will appear here."));
        selectedExtraTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void loadOptions() {
        try {
            customerComboBox.setItems(FXCollections.observableArrayList(customerService.findAll()));
            templateComboBox.setItems(FXCollections.observableArrayList(templateService.findAll()));
            flowerComboBox.setItems(FXCollections.observableArrayList(findAvailableFlowers()));
            extraComboBox.setItems(FXCollections.observableArrayList(findAvailableExtras()));
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

    private List<Extra> findAvailableExtras() throws SQLException {
        List<Extra> availableExtras = new ArrayList<>();
        for (Extra extra : extraService.findAll()) {
            if (extra.isActive()) {
                availableExtras.add(extra);
            }
        }
        return availableExtras;
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

    private void refreshSelectedExtras() {
        selectedExtraTable.setItems(FXCollections.observableArrayList(selectedExtras));
    }

    private void updateSummaryIfComplete() {
        try {
            Bouquet bouquet = bouquetBuilder.build();
            showBouquetSummary(bouquet, decorateBouquet(bouquet));
        } catch (IllegalArgumentException exception) {
            showEmptySummary();
        }
    }

    private BouquetItem decorateBouquet(Bouquet bouquet) {
        BouquetItem bouquetItem = new BaseBouquet(bouquet);
        for (Extra extra : selectedExtras) {
            bouquetItem = new ExtraDecorator(bouquetItem, extra);
        }
        return bouquetItem;
    }

    private void showBouquetSummary(Bouquet bouquet, BouquetItem bouquetItem) {
        PricingStrategy pricingStrategy = getSelectedPricingStrategy();
        long subtotal = bouquetItem.getSubtotal();
        long discount = pricingService.calculateDiscount(bouquetItem, pricingStrategy);
        long finalTotal = pricingService.calculateFinalTotal(bouquetItem, pricingStrategy);
        bouquetSummaryArea.setText(bouquet.getSummary()
                + "\n\nSelection: " + bouquetItem.getDescription()
                + "\nBouquet and extras subtotal: " + formatPrice(subtotal)
                + "\nPricing policy: " + pricingStrategy.getName()
                + "\nDiscount: " + formatPrice(discount)
                + "\nFinal total: " + formatPrice(finalTotal));
        bouquetSubtotalLabel.setText(formatPrice(subtotal));
        selectedPricingPolicyLabel.setText(pricingStrategy.getName());
        discountLabel.setText(formatPrice(discount));
        finalTotalLabel.setText(formatPrice(finalTotal));
    }

    private void showEmptySummary() {
        bouquetSummaryArea.setText("Select a customer and occasion, then add flowers to preview the bouquet.");
        bouquetSubtotalLabel.setText("BDT 0");
        selectedPricingPolicyLabel.setText("Standard Pricing");
        discountLabel.setText("BDT 0");
        finalTotalLabel.setText("BDT 0");
    }

    private PricingStrategy getSelectedPricingStrategy() {
        if ("Loyalty Pricing".equals(pricingPolicyComboBox.getValue())) {
            return new LoyaltyPricingStrategy();
        }
        return new StandardPricingStrategy();
    }

    private String createOrderNumber() {
        return "EB-" + System.currentTimeMillis();
    }

    private String getDeliveryAddress() {
        if (!"DELIVERY".equals(fulfillmentComboBox.getValue())) {
            return null;
        }
        String address = deliveryAddressTextArea.getText();
        return address == null ? null : address.trim();
    }

    private void resetBuilder() {
        bouquetBuilder = new BouquetBuilder();
        selectedExtras.clear();
        customerComboBox.setValue(null);
        templateComboBox.setValue(null);
        occasionComboBox.setValue(null);
        wrappingComboBox.setValue("No wrapping");
        messageTextArea.clear();
        fulfillmentComboBox.setValue("PICKUP");
        deliveryAddressTextArea.clear();
        pricingPolicyComboBox.setValue("Standard Pricing");
        refreshSelectedFlowers();
        refreshSelectedExtras();
        showEmptySummary();
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

    private StringConverter<Extra> extraConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Extra extra) {
                return extra == null ? "" : extra.getName() + " (" + formatPrice(extra.getUnitPrice()) + ")";
            }

            @Override
            public Extra fromString(String value) {
                return null;
            }
        };
    }

    private StringConverter<BouquetTemplate> templateConverter() {
        return new StringConverter<>() {
            @Override public String toString(BouquetTemplate template) { return template == null ? "" : template.getName() + " - " + template.getOccasion(); }
            @Override public BouquetTemplate fromString(String value) { return null; }
        };
    }

    private String formatPrice(long price) {
        return "BDT " + price;
    }

    private boolean isExtraSelected(Extra extra) {
        for (Extra selectedExtra : selectedExtras) {
            if (selectedExtra == extra || (extra.getId() > 0 && selectedExtra.getId() == extra.getId())) {
                return true;
            }
        }
        return false;
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setVisible(!message.isBlank());
        messageLabel.setManaged(!message.isBlank());
    }
}
