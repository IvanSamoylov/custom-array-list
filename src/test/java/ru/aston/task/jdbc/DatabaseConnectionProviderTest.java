package ru.aston.task.jdbc;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;


import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DatabaseConnectionProviderTest {

    private static final String TEST_SQL_SCRIPT = "CREATE TABLE test_table (id INT PRIMARY KEY);";

    @BeforeEach
    public void resetSingleton() {
        try {
            var instanceField = DatabaseConnectionProvider.class.getDeclaredField("instance");
            instanceField.setAccessible(true);
            instanceField.set(null, null);
        } catch (Exception e) {
            fail("Failed to reset singleton instance before each test");
        }
    }

    @Test
    public void testSingletonInstance() {
        DatabaseConnectionProvider instance1 = DatabaseConnectionProvider.getInstance();
        DatabaseConnectionProvider instance2 = DatabaseConnectionProvider.getInstance();

        assertNotNull(instance1);
        assertSame(instance1, instance2, "DatabaseConnectionProvider should return the same instance (Singleton)");
    }

    @Test
    public void testDataSourceInitialization() {
        DatabaseConnectionProvider instance = DatabaseConnectionProvider.getInstance();
        DataSource dataSource = instance.getDataSource();

        assertNotNull(dataSource, "DataSource should be initialized in DatabaseConnectionProvider");
        assertTrue(dataSource instanceof HikariDataSource, "DataSource should be an instance of HikariDataSource");
    }

}
