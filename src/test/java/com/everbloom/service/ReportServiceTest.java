package com.everbloom.service;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.database.DatabaseInitializer;
import com.everbloom.model.Bouquet;
import com.everbloom.model.BouquetBuilder;
import com.everbloom.model.Customer;
import com.everbloom.model.Extra;
import com.everbloom.model.Flower;
import com.everbloom.model.Order;
import com.everbloom.model.PopularItem;
import com.everbloom.model.SalesReportRow;
import com.everbloom.repository.OrderRepository;
import com.everbloom.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void aggregatesSalesAndPopularFlowersAndExtrasForDateRange() throws Exception {
        DatabaseConnection databaseConnection = new DatabaseConnection(temporaryDirectory.resolve("report-test.db"));
        new DatabaseInitializer(databaseConnection).initialize();
        OrderService orderService = new OrderService(new OrderRepository(databaseConnection));
        ReportService reportService = new ReportService(new ReportRepository(databaseConnection));
        orderService.placeOrder(createOrder("EB-REPORT-1", 2, List.of(new Extra(2, "Greeting Card", "Card", 1200, true))));
        orderService.placeOrder(createOrder("EB-REPORT-2", 3, List.of(new Extra(4, "Chocolate Box", "Chocolate", 6500, true))));

        LocalDate reportDate = LocalDate.now(ZoneOffset.UTC);
        List<SalesReportRow> salesRows = reportService.findSalesByDate(reportDate, reportDate);
        List<PopularItem> popularFlowers = reportService.findPopularFlowers(reportDate, reportDate);
        List<PopularItem> popularExtras = reportService.findPopularExtras(reportDate, reportDate);

        assertEquals(1, salesRows.size());
        assertEquals(2, salesRows.getFirst().getOrderCount());
        assertEquals(22700, salesRows.getFirst().getRevenue());
        assertEquals("Rose", popularFlowers.getFirst().getName());
        assertEquals(5, popularFlowers.getFirst().getQuantity());
        assertEquals(2, popularExtras.size());
        assertTrue(popularExtras.stream().anyMatch(item -> item.getName().equals("Greeting Card") && item.getQuantity() == 1));
    }

    private Order createOrder(String orderNumber, int flowerQuantity, List<Extra> extras) {
        Customer customer = new Customer(1, "Nadia Rahman", "01710000001", null, null);
        Flower rose = new Flower(1, "Rose", "Red", 3000, 80, true);
        Bouquet bouquet = new BouquetBuilder().forCustomer(customer).forOccasion("Birthday").addFlower(rose, flowerQuantity).build();
        long extrasTotal = 0;
        for (Extra extra : extras) {
            extrasTotal += extra.getUnitPrice();
        }
        long total = bouquet.getFlowerSubtotal() + extrasTotal;
        return new Order(orderNumber, customer, bouquet, extras, "PICKUP", null, "Standard Pricing", total, 0, total);
    }
}
