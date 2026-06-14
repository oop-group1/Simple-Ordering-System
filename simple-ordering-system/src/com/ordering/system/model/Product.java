package com.ordering.system.model;

public class Product {
    private int itemId;
    private String itemName;
    private String itemDesc;
    private double itemPrice;
    private int itemQty;

    public Product(int itemId, String itemName, String itemDesc, double itemPrice, int itemQty) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.itemDesc = itemDesc;
        this.itemPrice = itemPrice;
        this.itemQty = itemQty;
    }

    // Getters and Setters (Encapsulation)
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemDesc() { return itemDesc; }
    public void setItemDesc(String itemDesc) { this.itemDesc = itemDesc; }
    public double getItemPrice() { return itemPrice; }
    public void setItemPrice(double itemPrice) { this.itemPrice = itemPrice; }
    public int getItemQty() { return itemQty; }
    public void setItemQty(int itemQty) { this.itemQty = itemQty; }

    @Override
    public String toString() {
        return String.format("ID: %d | Name: %-15s | Price: $%.2f | Stock: %d", 
                              itemId, itemName, itemPrice, itemQty);
    }
}