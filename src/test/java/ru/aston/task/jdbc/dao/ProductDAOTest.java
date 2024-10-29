package ru.aston.task.jdbc.dao;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ProductDAOTest {
    @Test
    public void testConstructor() {
        DataSource dataSource = mock(DataSource.class);

        ProductDAO productDAO = new ProductDAO(dataSource);

        assertNotNull(productDAO);
    }
}