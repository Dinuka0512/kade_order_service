package com.dinuka.dev.order_service.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() throws SQLException {
        createDatabaseIfNotExists();

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }

    private void createDatabaseIfNotExists() throws SQLException {
        String dbName = url.substring(url.lastIndexOf('/') + 1);
        String serverUrl = url.substring(0, url.lastIndexOf('/') + 1) + "postgres";

        try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
             Statement stmt = conn.createStatement()) {
            try {
                stmt.executeUpdate("CREATE DATABASE \"" + dbName + "\"");
                System.out.println("Database '" + dbName + "' created.");
            } catch (SQLException e) {
                System.out.println("Database '" + dbName + "' already exists (or creation skipped).");
            }
        }
    }
}
