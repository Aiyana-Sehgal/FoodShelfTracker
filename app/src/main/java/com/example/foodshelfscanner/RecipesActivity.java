package com.example.foodshelfscanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.adapters.RecipeAdapter;
import com.example.foodshelfscanner.models.FoodItem;
import com.example.foodshelfscanner.models.Recipe;
import com.example.foodshelfscanner.utils.AddItemActivity;
import com.example.foodshelfscanner.utils.DataProvider;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class RecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerRecipes;
    private RecipeAdapter recipeAdapter;
    private BottomNavigationView bottomNavigation;
    private DataProvider dataProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        dataProvider = new DataProvider(this);

        initViews();
        setupRecyclerView();
        setupBottomNavigation();
    }

    private void initViews() {
        recyclerRecipes = findViewById(R.id.recyclerRecipes);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupRecyclerView() {
        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Get all items from pantry and freezer to suggest recipes
        dataProvider.getPantryItems(pantryItems -> {
            List<FoodItem> allItems = new ArrayList<>(pantryItems);
            dataProvider.getFreezerItems(freezerItems -> {
                allItems.addAll(freezerItems);
                if (allItems.isEmpty()) {
                    runOnUiThread(() -> Toast.makeText(this, "No items to suggest recipes from", Toast.LENGTH_LONG).show());
                    return;
                }
                // Get recipe suggestions from Spoonacular
                dataProvider.getRecipeSuggestions(allItems, recipes -> {
                    runOnUiThread(() -> {
                        recipeAdapter = new RecipeAdapter(this, recipes);
                        recyclerRecipes.setAdapter(recipeAdapter);
                    });
                });
            });
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_recipes);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(RecipesActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_recipes) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                Toast.makeText(this, "Settings coming soon", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}
