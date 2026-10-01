package com.mycompany.dentacareclinic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/dentacare_db";
    private static final String USER = "root";     // Default XAMPP username
    private static final String PASSWORD = "";     // Default XAMPP password is empty

    public static Connection connectDB() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection connect = DriverManager.getConnection(URL, USER, PASSWORD);
            return connect;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}