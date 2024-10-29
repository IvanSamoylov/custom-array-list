package ru.aston.task.jdbc.dao;

import ru.aston.task.jdbc.models.OrderEntry;

import javax.sql.DataSource;

public class OrderEntryDAO extends SimpleDao<OrderEntry> {
    public OrderEntryDAO(DataSource dataSource) {
        super(dataSource);
    }
}
