package com.squad83;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    // TAMU CSCE 315 AWS Database URL for Squad 83
    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/squad_83_db";
    private static final String USER = "squad_83";
    private static final String PASSWORD = "plazmapug";

    public static Connection connect() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Successfully connected to the AWS database!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
            return null;
        }
    }
}