package com.example.ragsearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootTest
public class SupabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testSupabaseConnection() throws Exception {
        System.out.println("Testing Supabase connection...");

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ SUCCESS: Connected to Supabase database!");
            System.out.println("Database: " + conn.getMetaData().getDatabaseProductName());
            System.out.println("Version: " + conn.getMetaData().getDatabaseProductVersion());
        } catch (Exception e) {
            System.out.println("❌ FAILED: Could not connect to Supabase");
            System.out.println("Error: " + e.getMessage());
            throw e;
        }

        // Test a simple query
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        System.out.println("✅ SUCCESS: Query executed, result: " + result);
    }
}