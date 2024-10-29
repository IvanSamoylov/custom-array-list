package ru.aston.task.jdbc.dao;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class CustomerDAOTest {
    @Test
    public void testConstructor() {
        DataSource dataSource = mock(DataSource.class);

        CustomerDAO customerDAO = new CustomerDAO(dataSource);

        assertNotNull(customerDAO);
    }
}