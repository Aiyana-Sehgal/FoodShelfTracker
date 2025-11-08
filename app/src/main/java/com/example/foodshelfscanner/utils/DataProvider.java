package com.example.foodshelfscanner.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.foodshelfscanner.models.FoodItem;
import com.example.foodshelfscanner.models.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataProvider {

    private static final String TAG = "DataProvider";
    private final DbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor(); // For background tasks
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // To post results to main thread

    public DataProvider(Context context) {
        this.dbHelper = new DbHelper();
    }

    // Callback for food items
    public interface FoodItemCallback {
        void onItemsReady(List<FoodItem> items);
    }

    // Callback for recipes
    public interface RecipeCallback {
        void onRecipesReady(List<Recipe> recipes);
    }

    public void getPantryItems(final FoodItemCallback callback) {
        executor.execute(() -> {
            List<FoodItem> items = dbHelper.getItems("Pantry");
            mainThreadHandler.post(() -> callback.onItemsReady(items));
        });
    }

    public void getFreezerItems(final FoodItemCallback callback) {
        executor.execute(() -> {
            List<FoodItem> items = dbHelper.getItems("Freezer");
            mainThreadHandler.post(() -> callback.onItemsReady(items));
        });
    }

    public void getRecipeSuggestions(List<FoodItem> availableItems, final RecipeCallback callback) {
        executor.execute(() -> {
            // Fetch all recipes from the database
            List<Recipe> allRecipes = dbHelper.getAllRecipes();
            List<Recipe> suggestedRecipes = new ArrayList<>();

            // Basic logic: suggest recipes that use at least one available ingredient
            for (Recipe recipe : allRecipes) {
                for (String ingredient : recipe.getIngredients()) {
                    boolean hasIngredient = false;
                    for (FoodItem availableItem : availableItems) {
                        if (ingredient.toLowerCase().contains(availableItem.getName().toLowerCase())) {
                            suggestedRecipes.add(recipe);
                            hasIngredient = true;
                            break;
                        }
                    }
                    if (hasIngredient) {
                        break;
                    }
                }
            }

            // If no suggestions, return all recipes
            if (suggestedRecipes.isEmpty()) {
                suggestedRecipes.addAll(allRecipes);
            }

            mainThreadHandler.post(() -> callback.onRecipesReady(suggestedRecipes));
        });
    }
}
