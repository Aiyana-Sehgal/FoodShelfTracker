package com.example.foodshelfscanner.utils;

import android.util.Log;

import com.example.foodshelfscanner.models.FoodItem;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DbHelper {

    private static final String TAG = "DbHelper";
    private static final String DB_URL = "jdbc:mysql://10.3.68.106:3306/food_shelf_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "foodapp";
    private static final String DB_PASSWORD = "12345";

    // Statically load the JDBC driver
    static {
        try {
            // Use the older, more compatible driver class name
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Could not load JDBC driver", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public List<FoodItem> getItems(String location) {
        List<FoodItem> items = new ArrayList<>();
        String query = "SELECT * FROM food_items WHERE location = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                // Create a FoodItem from the database record
                FoodItem item = new FoodItem(
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("quantity"),
                        rs.getString("location"),
                        rs.getString("added_date"),
                        rs.getInt("expiry_date"),
                        rs.getInt("total_days"),
                        -1 // No image resource from DB
                );
                items.add(item);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error getting items from database: " + e.getMessage());
        }

        return items;
    }
}
