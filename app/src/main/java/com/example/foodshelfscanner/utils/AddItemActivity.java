package com.example.foodshelfscanner.utils;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.example.foodshelfscanner.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Locale;

public class AddItemActivity extends AppCompatActivity {

    private static final String TAG = "AddItemActivity";
    private TextInputEditText etItemName, etBrand, etQuantity, etExpiryDate;
    private AutoCompleteTextView actvCategory, actvLocation;
    private MaterialButton btnScanBarcode, btnSave;
    private String scannedBarcode = "";
    private DataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize DataProvider
        dataProvider = new DataProvider(this);

        // Initialize UI
        etItemName = findViewById(R.id.etItemName);
        etBrand = findViewById(R.id.etBrand);
        etQuantity = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        actvCategory = findViewById(R.id.actvCategory);
        actvLocation = findViewById(R.id.actvLocation);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnSave = findViewById(R.id.btnSave);

        // Category dropdown
        String[] categories = {"Dairy", "Snacks", "Beverages", "Spices", "Frozen", "Others"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        actvCategory.setAdapter(catAdapter);

        // Location dropdown
        String[] locations = {"Fridge", "Freezer", "Pantry", "Cupboard"};
        ArrayAdapter<String> locAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations);
        actvLocation.setAdapter(locAdapter);

        // Date picker
        etExpiryDate.setOnClickListener(v -> showDatePicker());

        // Barcode scanner
        btnScanBarcode.setOnClickListener(v -> {
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.setCaptureActivity(CaptureActivityPortrait.class);
            integrator.setOrientationLocked(false);
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
            integrator.setPrompt("Scan a barcode");
            integrator.initiateScan();
        });

        // Save to DB
        btnSave.setOnClickListener(v -> saveItemToDatabase());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                scannedBarcode = result.getContents();
                Toast.makeText(this, "Scanned: " + scannedBarcode, Toast.LENGTH_LONG).show();
                fetchProductDetails(scannedBarcode);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(
                this,
                (DatePicker view, int y, int m, int d) ->
                        etExpiryDate.setText(String.format(Locale.getDefault(), "%d/%d/%d", d, m + 1, y)),
                year, month, day);
        picker.show();
    }

    // Fetch product data from OpenFoodFacts
    private void fetchProductDetails(String barcode) {
        new Thread(() -> {
            try {
                URL url = new URL("https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);
                reader.close();

                JSONObject jsonObject = new JSONObject(response.toString());
                JSONObject product = jsonObject.optJSONObject("product");

                if (product != null) {
                    final String productName = product.optString("product_name", "");
                    final String brand = product.optString("brands", "");
                    final String category = product.optString("categories_tags", "");

                    runOnUiThread(() -> {
                        etItemName.setText(productName);
                        etBrand.setText(brand);
                        actvCategory.setText(category.replace("_", " ").replace("en:", ""), false);

                        // Get expiry date suggestion from Gemini
                        dataProvider.getExpiryDateSuggestion(productName, shelfLife -> {
                            if (shelfLife != -1) {
                                // Prefill the expiry date
                                Calendar calendar = Calendar.getInstance();
                                calendar.add(Calendar.DAY_OF_YEAR, shelfLife);
                                etExpiryDate.setText(String.format(Locale.getDefault(), "%d/%d/%d",
                                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR)));
                            }
                        });
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "No product found!", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error fetching product details: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Error fetching data!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Save data into MySQL via JDBC
    private void saveItemToDatabase() {
        String itemName = etItemName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();

        if (itemName.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try (Connection conn = new DbHelper().getConnection()) {
                String query = "INSERT INTO food_items (name, brand, category, quantity, expiry_date, location, barcode) VALUES (?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, itemName);
                stmt.setString(2, brand);
                stmt.setString(3, category);
                stmt.setString(4, quantity);
                stmt.setString(5, expiryDate);
                stmt.setString(6, location);
                stmt.setString(7, scannedBarcode);

                int rows = stmt.executeUpdate();

                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Item saved successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after saving
                    } else {
                        Toast.makeText(this, "Failed to save item!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (SQLException e) {
                Log.e(TAG, "Database error: " + e.getMessage());
                runOnUiThread(() -> Toast.makeText(this, "Database connection failed!", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
