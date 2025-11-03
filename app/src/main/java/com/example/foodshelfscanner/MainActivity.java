package com.example.foodshelfscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.adapters.FoodItemAdapter;
import com.example.foodshelfscanner.models.FoodItem;
import com.example.foodshelfscanner.utils.AddItemActivity;
import com.example.foodshelfscanner.utils.DataProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerPantry, recyclerFreezer;
    private FoodItemAdapter pantryAdapter, freezerAdapter;
    private BottomNavigationView bottomNavigation;
    private Button btnScanBarcode, btnManualEntry;
    private DataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataProvider = new DataProvider(this);

        initViews();
        setupRecyclerViews();
        setupBottomNavigation();
        setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void initViews() {
        recyclerPantry = findViewById(R.id.recyclerPantry);
        recyclerFreezer = findViewById(R.id.recyclerFreezer);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        btnScanBarcode = findViewById(R.id.btnScanBarcode);
        btnManualEntry = findViewById(R.id.btnManualEntry);
    }

    private void setupRecyclerViews() {
        // Pantry RecyclerView
        recyclerPantry.setLayoutManager(new LinearLayoutManager(this));

        // Freezer RecyclerView
        recyclerFreezer.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshData() {
        // Pantry RecyclerView
        dataProvider.getPantryItems(items -> {
            pantryAdapter = new FoodItemAdapter(this, items);
            recyclerPantry.setAdapter(pantryAdapter);
        });

        // Freezer RecyclerView
        dataProvider.getFreezerItems(items -> {
            freezerAdapter = new FoodItemAdapter(this, items);
            recyclerFreezer.setAdapter(freezerAdapter);
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_scan) {
                startActivity(new Intent(MainActivity.this, AddItemActivity.class));
                return true;
            } else if (itemId == R.id.nav_recipes) {
                startActivity(new Intent(MainActivity.this, RecipesActivity.class));
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void setupButtons() {
        btnScanBarcode.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Barcode Scanner...", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(MainActivity.this, AddItemActivity.class));
        });

        btnManualEntry.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, AddItemActivity.class));
        });
    }
}
