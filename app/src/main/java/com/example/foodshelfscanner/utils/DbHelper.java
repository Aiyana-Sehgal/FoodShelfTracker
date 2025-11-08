package com.example.foodshelfscanner.utils;

import android.util.Log;

import com.example.foodshelfscanner.models.FoodItem;
import com.example.foodshelfscanner.models.Recipe;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DbHelper {

    private static final String TAG = "DbHelper";
    private static final String DB_URL = "jdbc:mysql://192.168.42.1:3306/food_shelf_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "android_user";
    private static final String DB_PASSWORD = "Android@123";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Could not load JDBC driver", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // ✅ Save item with DATE field
    public boolean saveItem(String name,
                            String brand,
                            String category,
                            String quantity,
                            Date expiryDate,
                            String location) {
        final String sql = "INSERT INTO food_items " +
                "(name, brand, category, quantity, expiry_date, location, added_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURDATE())";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setString(2, brand);
            stmt.setString(3, category);
            stmt.setString(4, quantity);
            stmt.setDate(5, expiryDate);
            stmt.setString(6, location);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            Log.e(TAG, "Error saving item to database: " + e.getMessage(), e);
            return false;
        }
    }

    // ✅ Delete item
    public boolean deleteItem(int id) {
        final String sql = "DELETE FROM food_items WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Log.e(TAG, "Error deleting item: " + e.getMessage(), e);
            return false;
        }
    }

    // ✅ Get all items from one location
    public List<FoodItem> getItems(String location) {
        List<FoodItem> items = new ArrayList<>();

        final String sql = "SELECT id, name, brand, category, quantity, location, " +
                "added_date, expiry_date, " +
                "DATEDIFF(expiry_date, CURDATE()) AS shelf_life_days, " +
                "DATEDIFF(expiry_date, added_date) AS total_days " +
                "FROM food_items WHERE location = ? " +
                "ORDER BY expiry_date ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Date added = rs.getDate("added_date");
                String addedIso = (added != null) ? added.toString() : "";

                FoodItem item = new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("quantity"),
                        rs.getString("location"),
                        addedIso,
                        rs.getInt("shelf_life_days"),
                        rs.getInt("total_days"),
                        -1
                );
                items.add(item);
            }
        } catch (SQLException e) {
            Log.e(TAG, "Error getting items: " + e.getMessage(), e);
        }

        return items;
    }

    // ✅ NEW: Get ALL items (Fridge + Freezer + Pantry)
    public List<FoodItem> getAllItems() {
        List<FoodItem> items = new ArrayList<>();

        final String sql = "SELECT id, name, brand, category, quantity, location, " +
                "added_date, expiry_date, " +
                "DATEDIFF(expiry_date, CURDATE()) AS shelf_life_days, " +
                "DATEDIFF(expiry_date, added_date) AS total_days " +
                "FROM food_items " +
                "ORDER BY location ASC, expiry_date ASC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Date added = rs.getDate("added_date");
                String addedIso = (added != null) ? added.toString() : "";

                FoodItem item = new FoodItem(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("brand"),
                        rs.getString("quantity"),
                        rs.getString("location"),
                        addedIso,
                        rs.getInt("shelf_life_days"),
                        rs.getInt("total_days"),
                        -1
                );
                items.add(item);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error getting all items: " + e.getMessage(), e);
        }

        return items;
    }

    // ✅ Get all recipes (unchanged)
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        final String sql = "SELECT * FROM recipes";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String[] ing = rs.getString("ingredients").split("\\|");
                String[] tags = rs.getString("tags").split("\\|");
                String[] steps = rs.getString("steps").split("\\|");

                Recipe recipe = new Recipe(
                        rs.getString("name"),
                        rs.getString("uses_items"),
                        Arrays.asList(ing),
                        Arrays.asList(tags),
                        Arrays.asList(steps),
                        rs.getString("cook_time"),
                        rs.getString("calories"),
                        rs.getInt("serves"),
                        android.R.drawable.ic_menu_gallery
                );
                recipes.add(recipe);
            }

        } catch (SQLException e) {
            Log.e(TAG, "Error getting recipes: " + e.getMessage(), e);
        }

        return recipes;
    }
}
