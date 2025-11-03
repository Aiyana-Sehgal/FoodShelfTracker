package com.example.foodshelfscanner.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.example.foodshelfscanner.BuildConfig;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import com.example.foodshelfscanner.models.FoodItem;
import com.example.foodshelfscanner.models.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DataProvider {

    private static final String TAG = "DataProvider";
    private final DbHelper dbHelper;
    private GenerativeModelFutures generativeModel;
    private final Executor executor = Executors.newSingleThreadExecutor(); // For background tasks
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper()); // To post results to main thread

    public DataProvider(Context context) {
        this.dbHelper = new DbHelper();

        // Asynchronously initialize the generative model to avoid blocking the main thread
        executor.execute(() -> {
            try {
                if (TextUtils.isEmpty(BuildConfig.GEMINI_API_KEY) || "null".equals(BuildConfig.GEMINI_API_KEY)) {
                    Log.e(TAG, "Gemini API key is missing. AI features will be disabled.");
                    return;
                }
                GenerativeModel gm = new GenerativeModel("gemini-pro", BuildConfig.GEMINI_API_KEY);
                this.generativeModel = GenerativeModelFutures.from(gm);
            } catch (Exception e) {
                Log.e(TAG, "Failed to initialize Gemini AI model. AI features will be disabled.", e);
            }
        });
    }

    // Callback for food items
    public interface FoodItemCallback {
        void onItemsReady(List<FoodItem> items);
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

    public void getRecipeSuggestions(List<FoodItem> items, final RecipeCallback callback) {
        if (generativeModel == null) {
            Log.e(TAG, "Gemini AI model is not initialized. Cannot get recipe suggestions.");
            mainThreadHandler.post(() -> callback.onRecipesReady(new ArrayList<>()));
            return;
        }

        // Create a prompt for the Gemini API
        StringBuilder prompt = new StringBuilder("Suggest some recipes based on these ingredients, prioritizing the ones that expire soon:\n");
        for (FoodItem item : items) {
            prompt.append(item.getName()).append(" (expires in ").append(item.getShelfLifeDays()).append(" days)\n");
        }

        Content content = new Content.Builder().addText(prompt.toString()).build();

        ListenableFuture<GenerateContentResponse> response = generativeModel.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    // Parse the response and create a list of recipes
                    List<Recipe> recipes = parseRecipesFromResponse(result.getText());
                    mainThreadHandler.post(() -> callback.onRecipesReady(recipes));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing recipe suggestions: " + e.getMessage());
                    mainThreadHandler.post(() -> callback.onRecipesReady(new ArrayList<>()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error getting recipe suggestions: " + t.getMessage());
                mainThreadHandler.post(() -> callback.onRecipesReady(new ArrayList<>()));
            }
        }, executor);
    }

    public void getExpiryDateSuggestion(String itemName, final ExpiryDateCallback callback) {
        if (generativeModel == null) {
            Log.e(TAG, "Gemini AI model is not initialized. Cannot get expiry date suggestion.");
            mainThreadHandler.post(() -> callback.onExpiryDateReady(-1));
            return;
        }

        // Create a prompt for the Gemini API
        String prompt = "How long does " + itemName + " typically last in Chennai, India? Reply with just the number of days.";

        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> response = generativeModel.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    // Parse the response and extract the shelf life
                    int shelfLife = parseShelfLifeFromResponse(result.getText());
                    mainThreadHandler.post(() -> callback.onExpiryDateReady(shelfLife));
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing expiry date suggestion: " + e.getMessage());
                    mainThreadHandler.post(() -> callback.onExpiryDateReady(-1));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Error getting expiry date suggestion: " + t.getMessage());
                mainThreadHandler.post(() -> callback.onExpiryDateReady(-1));
            }
        }, executor);
    }

    private List<Recipe> parseRecipesFromResponse(String response) throws JSONException {
        List<Recipe> recipes = new ArrayList<>();

        if (response != null && !response.isEmpty()) {
            try {
                // Try to parse as JSON first (in case Gemini returns structured data)
                JSONArray recipesArray = new JSONArray(response);
                for (int i = 0; i < recipesArray.length(); i++) {
                    JSONObject recipeObject = recipesArray.getJSONObject(i);
                    String name = recipeObject.optString("name", "Recipe " + (i + 1));
                    String ingredients = recipeObject.optString("ingredients", "");
                    String steps = recipeObject.optString("steps", "");

                    List<String> ingredientList = new ArrayList<>();
                    List<String> stepList = new ArrayList<>();
                    List<String> tagList = new ArrayList<>();

                    if (!ingredients.isEmpty()) {
                        String[] ingArray = ingredients.split(",");
                        for (String ing : ingArray) {
                            ingredientList.add(ing.trim());
                        }
                    }

                    if (!steps.isEmpty()) {
                        String[] stepArray = steps.split("\\.");
                        for (String step : stepArray) {
                            if (!step.trim().isEmpty()) {
                                stepList.add(step.trim());
                            }
                        }
                    }

                    tagList.add("AI Generated");

                    Recipe recipe = new Recipe(
                            name,
                            "Based on your ingredients",
                            ingredientList,
                            tagList,
                            stepList,
                            "30-45 mins",
                            "250-400 cal",
                            4,
                            android.R.drawable.ic_menu_gallery // Using a safe, built-in drawable
                    );
                    recipes.add(recipe);
                }
            } catch (Exception e) {
                // If JSON parsing fails, parse as plain text from Gemini
                recipes = parseTextRecipes(response);
            }
        }

        // If no recipes were parsed or response was empty, provide fallback recipes
        if (recipes.isEmpty()) {
            recipes = getFallbackRecipes();
        }

        return recipes;
    }

    private List<Recipe> parseTextRecipes(String response) {
        List<Recipe> recipes = new ArrayList<>();

        // Split response by common recipe separators
        String[] lines = response.split("\n");
        String currentRecipeName = "";
        List<String> currentIngredients = new ArrayList<>();
        List<String> currentSteps = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // Look for recipe titles (usually numbered or have keywords)
            if (line.matches("^\\d+\\..*") || line.toLowerCase().contains("recipe") ||
                    line.toLowerCase().contains("dish") || line.matches("^[A-Z][a-zA-Z\\s]+:?$")) {

                // Save previous recipe if it exists
                if (!currentRecipeName.isEmpty()) {
                    Recipe recipe = createRecipeFromParts(currentRecipeName, currentIngredients, currentSteps);
                    recipes.add(recipe);
                }

                // Start new recipe
                currentRecipeName = line.replaceAll("^\\d+\\.\\s*", "").replaceAll(":?$", "");
                currentIngredients = new ArrayList<>();
                currentSteps = new ArrayList<>();
            } else if (line.toLowerCase().contains("ingredient") ||
                    line.matches("^-\\s.*") || line.matches("^\\*\\s.*")) {
                // This looks like an ingredient
                String ingredient = line.replaceAll("^[-*]\\s*", "");
                currentIngredients.add(ingredient);
            } else if (line.toLowerCase().contains("step") ||
                    line.matches("^\\d+\\.\\s.*")) {
                // This looks like a step
                String step = line.replaceAll("^\\d+\\.\\s*", "");
                currentSteps.add(step);
            } else {
                // Add as step if we have a recipe name
                if (!currentRecipeName.isEmpty()) {
                    currentSteps.add(line);
                }
            }
        }

        // Don't forget the last recipe
        if (!currentRecipeName.isEmpty()) {
            Recipe recipe = createRecipeFromParts(currentRecipeName, currentIngredients, currentSteps);
            recipes.add(recipe);
        }

        return recipes;
    }

    private Recipe createRecipeFromParts(String name, List<String> ingredients, List<String> steps) {
        List<String> tags = new ArrayList<>();
        tags.add("AI Generated");
        tags.add("Quick");

        if (ingredients.isEmpty()) {
            ingredients.add("Check recipe details");
        }
        if (steps.isEmpty()) {
            steps.add("Follow traditional cooking method");
        }

        return new Recipe(
                name,
                "Based on your available ingredients",
                ingredients,
                tags,
                steps,
                "30-45 mins",
                "250-400 cal",
                4,
                android.R.drawable.ic_menu_gallery // Using a safe, built-in drawable
        );
    }

    private List<Recipe> getFallbackRecipes() {
        List<Recipe> fallbackRecipes = new ArrayList<>();

        // Recipe 1: Basic Rice Dal
        List<String> ingredients1 = new ArrayList<>();
        ingredients1.add("Rice (1 cup)");
        ingredients1.add("Lentils (1/2 cup)");
        ingredients1.add("Turmeric (1 tsp)");
        ingredients1.add("Salt (to taste)");
        ingredients1.add("Oil (2 tbsp)");

        List<String> steps1 = new ArrayList<>();
        steps1.add("Wash and cook rice separately");
        steps1.add("Cook lentils with turmeric and salt");
        steps1.add("Heat oil and add seasoning");
        steps1.add("Mix rice and dal, serve hot");

        List<String> tags1 = new ArrayList<>();
        tags1.add("Vegetarian");
        tags1.add("Healthy");
        tags1.add("Easy");

        fallbackRecipes.add(new Recipe(
                "Simple Dal Rice",
                "Rice, Lentils",
                ingredients1,
                tags1,
                steps1,
                "25 mins",
                "320 cal",
                4,
                android.R.drawable.ic_menu_gallery // Using a safe, built-in drawable
        ));

        // Recipe 2: Mixed Vegetable Curry
        List<String> ingredients2 = new ArrayList<>();
        ingredients2.add("Mixed vegetables (2 cups)");
        ingredients2.add("Onions (1 medium)");
        ingredients2.add("Tomatoes (2 medium)");
        ingredients2.add("Spices (as needed)");
        ingredients2.add("Oil (2 tbsp)");

        List<String> steps2 = new ArrayList<>();
        steps2.add("Heat oil in a pan");
        steps2.add("Sauté onions until golden");
        steps2.add("Add tomatoes and spices");
        steps2.add("Add vegetables and cook until tender");
        steps2.add("Serve with rice or bread");

        List<String> tags2 = new ArrayList<>();
        tags2.add("Vegetarian");
        tags2.add("Nutritious");
        tags2.add("Colorful");

        fallbackRecipes.add(new Recipe(
                "Mixed Vegetable Curry",
                "Vegetables, Onions, Tomatoes",
                ingredients2,
                tags2,
                steps2,
                "30 mins",
                "180 cal",
                4,
                android.R.drawable.ic_menu_gallery // Using a safe, built-in drawable
        ));

        // Recipe 3: Quick Pasta
        List<String> ingredients3 = new ArrayList<>();
        ingredients3.add("Pasta (200g)");
        ingredients3.add("Garlic (3 cloves)");
        ingredients3.add("Oil (3 tbsp)");
        ingredients3.add("Salt and pepper");
        ingredients3.add("Any available vegetables");

        List<String> steps3 = new ArrayList<>();
        steps3.add("Boil pasta until al dente");
        steps3.add("Heat oil, sauté garlic");
        steps3.add("Add vegetables if available");
        steps3.add("Toss with cooked pasta");
        steps3.add("Season and serve");

        List<String> tags3 = new ArrayList<>();
        tags3.add("Quick");
        tags3.add("Italian");
        tags3.add("Simple");

        fallbackRecipes.add(new Recipe(
                "Simple Garlic Pasta",
                "Pasta, Garlic",
                ingredients3,
                tags3,
                steps3,
                "15 mins",
                "350 cal",
                2,
                android.R.drawable.ic_menu_gallery // Using a safe, built-in drawable
        ));

        return fallbackRecipes;
    }

    private int parseShelfLifeFromResponse(String response) {
        // This is a placeholder for parsing the response from the Gemini API.
        // It expects a numerical answer.
        try {
            if (response != null) {
                String numberOnly = response.replaceAll("[^0-9]", "").trim();
                if (!numberOnly.isEmpty()) {
                    return Integer.parseInt(numberOnly);
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Could not parse shelf life from response: " + response);
        }
        return 30; // Placeholder/default
    }

    public interface RecipeCallback {
        void onRecipesReady(List<Recipe> recipes);
    }

    public interface ExpiryDateCallback {
        void onExpiryDateReady(int shelfLife);
    }
}
