package com.example.SYshop.models;

public class Order {

    private String orderId;
    private String totalPrice;
    private String status;
    private long createdAt;
    private int itemsCount;

    public Order(String orderId, String totalPrice, String status, long createdAt, int itemsCount) {
        this.orderId = orderId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.itemsCount = itemsCount;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getItemsCount() {
        return itemsCount;
    }
}