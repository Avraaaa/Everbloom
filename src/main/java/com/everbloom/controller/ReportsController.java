package com.everbloom.controller;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.PopularItem;
import com.everbloom.model.SalesReportRow;
import com.everbloom.repository.ReportRepository;
import com.everbloom.service.ReportService;
import com.everbloom.util.MoneyFormatter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.SQLException;
import java.time.LocalDate;

public class ReportsController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label messageLabel;
    @FXML private TableView<SalesReportRow> salesTable;
    @FXML private TableColumn<SalesReportRow, String> salesDateColumn;
    @FXML private TableColumn<SalesReportRow, Long> salesOrderCountColumn;
    @FXML private TableColumn<SalesReportRow, String> salesRevenueColumn;
    @FXML private TableView<PopularItem> flowerTable;
    @FXML private TableColumn<PopularItem, String> flowerNameColumn;
    @FXML private TableColumn<PopularItem, Long> flowerQuantityColumn;
    @FXML private TableColumn<PopularItem, String> flowerRevenueColumn;
    @FXML private TableView<PopularItem> extraTable;
    @FXML private TableColumn<PopularItem, String> extraNameColumn;
    @FXML private TableColumn<PopularItem, Long> extraQuantityColumn;
    @FXML private TableColumn<PopularItem, String> extraRevenueColumn;

    private ReportService reportService;

    @FXML
    private void initialize() {
        reportService = new ReportService(new ReportRepository(new DatabaseConnection()));
        startDatePicker.setValue(LocalDate.now().minusDays(29));
        endDatePicker.setValue(LocalDate.now());
        configureTables();
        runReports();
    }

    @FXML
    private void runReports() {
        try {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            salesTable.setItems(FXCollections.observableArrayList(reportService.findSalesByDate(startDate, endDate)));
            flowerTable.setItems(FXCollections.observableArrayList(reportService.findPopularFlowers(startDate, endDate)));
            extraTable.setItems(FXCollections.observableArrayList(reportService.findPopularExtras(startDate, endDate)));
            showMessage("");
        } catch (IllegalArgumentException exception) {
            clearTables();
            showMessage(exception.getMessage());
        } catch (SQLException exception) {
            clearTables();
            showMessage("Unable to load reports.");
        }
    }

    private void configureTables() {
        salesDateColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getDate()));
        salesOrderCountColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getOrderCount()));
        salesRevenueColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatPrice(cell.getValue().getRevenue())));
        salesTable.setPlaceholder(new Label("No sales were recorded for this date range."));
        salesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        configurePopularTable(flowerTable, flowerNameColumn, flowerQuantityColumn, flowerRevenueColumn, "No flower sales were recorded for this date range.");
        configurePopularTable(extraTable, extraNameColumn, extraQuantityColumn, extraRevenueColumn, "No extra sales were recorded for this date range.");
    }

    private void configurePopularTable(TableView<PopularItem> table, TableColumn<PopularItem, String> nameColumn,
                                       TableColumn<PopularItem, Long> quantityColumn, TableColumn<PopularItem, String> revenueColumn,
                                       String emptyMessage) {
        nameColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(cell.getValue().getName()));
        quantityColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getQuantity()));
        revenueColumn.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatPrice(cell.getValue().getRevenue())));
        table.setPlaceholder(new Label(emptyMessage));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private void clearTables() {
        salesTable.getItems().clear();
        flowerTable.getItems().clear();
        extraTable.getItems().clear();
    }

    private String formatPrice(long price) { return MoneyFormatter.format(price); }
    private void showMessage(String message) { messageLabel.setText(message); messageLabel.setVisible(!message.isBlank()); messageLabel.setManaged(!message.isBlank()); }
}
