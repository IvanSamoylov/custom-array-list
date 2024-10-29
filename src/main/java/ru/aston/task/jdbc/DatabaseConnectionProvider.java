package ru.aston.task.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Создает пул подключений к базе данных. При старте создает таблицу и инициализирует ее тестовыми значениями.
 */
public class DatabaseConnectionProvider {
    private static volatile DatabaseConnectionProvider instance;
    private final HikariDataSource dataSource;
    private final String initialDbScript = "schema.sql";

    private DatabaseConnectionProvider() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/jdbctask");
        config.setUsername("postgres");
        config.setPassword("12345");
        config.setDriverClassName("org.postgresql.Driver");

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);
        loadInitialData();
    }

    public static DatabaseConnectionProvider getInstance() {
        if (instance != null) {
            return instance;
        }
        synchronized (DatabaseConnectionProvider.class) {
            if (instance == null) {
                instance = new DatabaseConnectionProvider();
            }
            return instance;
        }
    }

    protected ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }

    private void loadInitialData() {
        InputStream inputStream = getClassLoader().getResourceAsStream(initialDbScript);
        if (inputStream == null) {
            throw new IllegalArgumentException("Script file not found: " + initialDbScript);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String line;
            StringBuilder sql = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sql.append(line);
                    if (line.trim().endsWith(";")) {
                        statement.execute(sql.toString());
                        sql.setLength(0);
                    }
                }
            }
            if (!sql.isEmpty()) {
                statement.execute(sql.toString());
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

}