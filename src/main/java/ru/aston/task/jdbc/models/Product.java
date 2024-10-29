package ru.aston.task.jdbc.models;

import ru.aston.task.jdbc.models.annotations.Id;

import java.math.BigDecimal;

public class Product {

    @Id
    private int id;
    private String code;
    private String name;
    private BigDecimal price;

    private Product() {

    }

    public Product(String code, String name, double price) {
        this.code = code;
        this.name = name;
        this.price = BigDecimal.valueOf(price);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price.doubleValue();
    }

    public void setPrice(double price) {
        this.price = BigDecimal.valueOf(price);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
