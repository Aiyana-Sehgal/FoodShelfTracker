package com.example.foodshelfscanner.utils;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodshelfscanner.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AddItemActivity extends AppCompatActivity {

    private TextInputEditText etItemName, etBrand, etQuantity, etExpiryDate;
    private AutoCompleteTextView actvCategory, actvLocation;
    private MaterialButton btnSave;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private DbHelper dbHelper;
    private Calendar selectedExpiryDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        dbHelper = new DbHelper();
        selectedExpiryDate = Calendar.getInstance();

        etItemName   = findViewById(R.id.etItemName);
        etBrand      = findViewById(R.id.etBrand);
        etQuantity   = findViewById(R.id.etQuantity);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        actvCategory = findViewById(R.id.actvCategory);
        actvLocation = findViewById(R.id.actvLocation);
        btnSave      = findViewById(R.id.btnSave);

        String[] categories = {"Dairy", "Snacks", "Beverages", "Spices", "Frozen", "Produce", "Meat", "Bakery", "Others"};
        actvCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories));

        String[] locations = {"Fridge", "Freezer", "Pantry", "Cupboard"};
        actvLocation.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations));

        etExpiryDate.setOnClickListener(v -> showDatePicker());
        btnSave.setOnClickListener(v -> saveItemToDatabase());
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        int y = now.get(Calendar.YEAR);
        int m = now.get(Calendar.MONTH);
        int d = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog picker = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    selectedExpiryDate.set(year, month, dayOfMonth);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    etExpiryDate.setText(sdf.format(selectedExpiryDate.getTime()));
                }, y, m, d);
        picker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        picker.show();
    }

    private void saveItemToDatabase() {
        String itemName = etItemName.getText().toString().trim();
        String brand = etBrand.getText().toString().trim();
        String category = actvCategory.getText().toString().trim();
        String quantity = etQuantity.getText().toString().trim();
        String location = actvLocation.getText().toString().trim();

        if (itemName.isEmpty() || category.isEmpty() || etExpiryDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        java.sql.Date expiryDateForDb = new java.sql.Date(selectedExpiryDate.getTimeInMillis());

        executor.execute(() -> {
            boolean success = dbHelper.saveItem(itemName, brand, category, quantity, expiryDateForDb, location);
            mainThreadHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "Item saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to save item. Check logs.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
