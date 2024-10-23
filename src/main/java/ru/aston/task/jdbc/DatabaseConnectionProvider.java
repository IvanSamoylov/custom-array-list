package ru.aston.task.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseConnectionProvider {
    private static volatile DatabaseConnectionProvider instance;
    private final HikariDataSource dataSource;

    private DatabaseConnectionProvider() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:~/test");
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName("org.h2.Driver");

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

    private void loadInitialData()  {

        var loader = DbApplication.class.getClassLoader();
        String scriptFile = "data.sql";
        InputStream inputStream = loader.getResourceAsStream(scriptFile);
        if (inputStream == null) {
            throw new IllegalArgumentException("Script file not found: " + scriptFile);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String line;
            StringBuilder sql = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // Игнорируем комментарии и пустые строки
                if (!line.trim().startsWith("--") && !line.trim().isEmpty()) {
                    sql.append(line);
                    if (line.trim().endsWith(";")) {
                        statement.execute(sql.toString());
                        sql.setLength(0); // Очищаем StringBuilder
                    }
                }
            }
            // Выполняем оставшийся SQL, если он есть
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

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}