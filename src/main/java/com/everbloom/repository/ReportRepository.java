package com.everbloom.repository;

import com.everbloom.database.DatabaseConnection;
import com.everbloom.model.PopularItem;
import com.everbloom.model.SalesReportRow;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReportRepository {

    private final DatabaseConnection databaseConnection;

    public ReportRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<SalesReportRow> findSalesByDate(String startDate, String endDate) throws SQLException {
        String sql = "SELECT date(placed_at) AS sales_date, COUNT(*) AS order_count, COALESCE(SUM(total_snapshot), 0) AS revenue "
                + "FROM orders WHERE date(placed_at) BETWEEN ? AND ? GROUP BY date(placed_at) ORDER BY sales_date";
        List<SalesReportRow> rows = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, startDate);
            statement.setString(2, endDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new SalesReportRow(resultSet.getString("sales_date"), resultSet.getLong("order_count"), resultSet.getLong("revenue")));
                }
            }
        }
        return rows;
    }

    public List<PopularItem> findPopularFlowers(String startDate, String endDate) throws SQLException {
        String sql = "SELECT af.flower_name_snapshot AS item_name, SUM(af.quantity) AS item_quantity, "
                + "SUM(af.line_total_snapshot) AS item_revenue FROM arrangement_flowers af "
                + "JOIN arrangements a ON af.arrangement_id = a.arrangement_id "
                + "JOIN orders o ON a.order_id = o.order_id WHERE date(o.placed_at) BETWEEN ? AND ? "
                + "GROUP BY af.flower_name_snapshot ORDER BY item_quantity DESC, item_revenue DESC";
        return findPopularItems(sql, startDate, endDate);
    }

    public List<PopularItem> findPopularExtras(String startDate, String endDate) throws SQLException {
        String sql = "SELECT ae.extra_name_snapshot AS item_name, SUM(ae.quantity) AS item_quantity, "
                + "SUM(ae.line_total_snapshot) AS item_revenue FROM arrangement_extras ae "
                + "JOIN arrangements a ON ae.arrangement_id = a.arrangement_id "
                + "JOIN orders o ON a.order_id = o.order_id WHERE date(o.placed_at) BETWEEN ? AND ? "
                + "GROUP BY ae.extra_name_snapshot ORDER BY item_quantity DESC, item_revenue DESC";
        return findPopularItems(sql, startDate, endDate);
    }

    private List<PopularItem> findPopularItems(String sql, String startDate, String endDate) throws SQLException {
        List<PopularItem> items = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, startDate);
            statement.setString(2, endDate);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    items.add(new PopularItem(resultSet.getString("item_name"), resultSet.getLong("item_quantity"), resultSet.getLong("item_revenue")));
                }
            }
        }
        return items;
    }
}
