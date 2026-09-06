package com.everbloom.model;

public class SalesReportRow {

    private final String date;
    private final long orderCount;
    private final long revenue;

    public SalesReportRow(String date, long orderCount, long revenue) {
        this.date = date;
        this.orderCount = orderCount;
        this.revenue = revenue;
    }

    public String getDate() { return date; }
    public long getOrderCount() { return orderCount; }
    public long getRevenue() { return revenue; }
}
