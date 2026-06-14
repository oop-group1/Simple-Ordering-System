package com.ordering.system.model;

public class Order {
    private int orderId;
    private int userId;
    private double totalAmount;

    public Order(int orderId, int userId, double totalAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.totalAmount = totalAmount;
    }

    public int getOrderId() { return orderId; }
    public int getUserId() { return userId; }
    public double getTotalAmount() { return totalAmount; }
}