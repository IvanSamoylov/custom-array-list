package ru.aston.task.jdbc.dao;

import ru.aston.task.jdbc.models.Product;

import javax.sql.DataSource;

public class ProductDAO extends SimpleDao<Product> {


    public ProductDAO(DataSource dataSource) {
        super(dataSource);
    }

}
