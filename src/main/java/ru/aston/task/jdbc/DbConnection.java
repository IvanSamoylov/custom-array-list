package ru.aston.task.jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class DbConnection {

    private HikariDataSource dataSource;

    public DbConnection() {
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

    }



    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close () {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}