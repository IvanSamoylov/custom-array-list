package ru.aston.task.jdbc.models;

import ru.aston.task.jdbc.models.annotations.Id;

import java.math.BigDecimal;

public class OrderEntry {
    @Id
    private int id;
    private int orderId;
    private int productId;
    private BigDecimal price;
    private int quantity;

    public OrderEntry(int orderId, int productId, BigDecimal price, int quantity) {
        this.orderId = orderId;
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public OrderEntry(int id, int orderId, int productId, BigDecimal price, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.price = price;
        this.quantity = quantity;
    }

    public OrderEntry() {}


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

