package com.dinuka.dev.order_service.dto;

public class DashboardStatsResponse {

    private double revenue;
    private long orders;
    private long pendingOrders;
    private long products;
    private long customers;
    private double avgOrderValue;

    public DashboardStatsResponse() {}

    public DashboardStatsResponse(double revenue, long orders, long pendingOrders, long products, long customers, double avgOrderValue) {
        this.revenue = revenue;
        this.orders = orders;
        this.pendingOrders = pendingOrders;
        this.products = products;
        this.customers = customers;
        this.avgOrderValue = avgOrderValue;
    }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public long getOrders() { return orders; }
    public void setOrders(long orders) { this.orders = orders; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }

    public long getProducts() { return products; }
    public void setProducts(long products) { this.products = products; }

    public long getCustomers() { return customers; }
    public void setCustomers(long customers) { this.customers = customers; }

    public double getAvgOrderValue() { return avgOrderValue; }
    public void setAvgOrderValue(double avgOrderValue) { this.avgOrderValue = avgOrderValue; }
}
