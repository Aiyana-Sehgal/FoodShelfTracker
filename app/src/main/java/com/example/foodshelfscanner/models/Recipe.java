package com.example.foodshelfscanner.models;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private String name;
    private String usesItems;
    private List<String> ingredients;
    private List<String> tags;
    private List<String> steps;
    private String cookTime;
    private String calories;
    private int serves;
    private int imageResource;

    public Recipe(String name, String usesItems, List<String> ingredients,
                  List<String> tags, List<String> steps, String cookTime,
                  String calories, int serves, int imageResource) {
        this.name = name;
        this.usesItems = usesItems;
        this.ingredients = ingredients;
        this.tags = tags;
        this.steps = steps;
        this.cookTime = cookTime;
        this.calories = calories;
        this.serves = serves;
        this.imageResource = imageResource;
    }

    // Getters
    public String getName() { return name; }
    public String getUsesItems() { return usesItems; }
    public List<String> getIngredients() { return ingredients; }
    public List<String> getTags() { return tags; }
    public List<String> getSteps() { return steps; }
    public String getCookTime() { return cookTime; }
    public String getCalories() { return calories; }
    public int getServes() { return serves; }
    public int getImageResource() { return imageResource; }
}
