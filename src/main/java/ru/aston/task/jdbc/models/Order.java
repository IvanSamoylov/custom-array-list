package ru.aston.task.jdbc.models;

import ru.aston.task.jdbc.models.annotations.Id;
import ru.aston.task.jdbc.models.annotations.OneToMany;

import java.util.List;

public class Order {
    @Id
    private int id;
    private int customerId;
    @OneToMany("OrderEntry")
    private List<OrderEntry> orderEntries;

    public Order() {
    }

    public Order(int id, int customerId, List<OrderEntry> orderEntries) {
        this.id = id;
        this.customerId = customerId;
        this.orderEntries = orderEntries;
    }

    public Order(int customerId, List<OrderEntry> orderEntries) {
        this.customerId = customerId;
        this.orderEntries = orderEntries;
    }

    @Override
    public String toString() {
        return String.format("Order id [%d] for customer id [%d]", id, customerId);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public List<OrderEntry> getOrderEntries() {
        return orderEntries;
    }

    public void setOrderEntries(List<OrderEntry> orderEntries) {
        this.orderEntries = orderEntries;
    }
}
