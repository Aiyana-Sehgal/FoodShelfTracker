package com.example.foodshelfscanner;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.adapters.StepAdapter;
import com.example.foodshelfscanner.models.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class RecipeDetailActivity extends AppCompatActivity {

    private ImageView ivRecipeBanner;
    private TextView tvRecipeTitle, tvRecipeSubtitle, tvCookTime, tvCalories;
    private ChipGroup chipGroupTags, chipGroupIngredients;
    private RecyclerView recyclerSteps;
    private Button btnStartCooking, btnSave;
    private Toolbar toolbar;

    private Recipe recipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        initViews();
        loadRecipeData();
        setupToolbar();
        setupButtons();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivRecipeBanner = findViewById(R.id.ivRecipeBanner);
        tvRecipeTitle = findViewById(R.id.tvRecipeTitle);
        tvRecipeSubtitle = findViewById(R.id.tvRecipeSubtitle);
        tvCookTime = findViewById(R.id.tvCookTime);
        tvCalories = findViewById(R.id.tvCalories);
        chipGroupTags = findViewById(R.id.chipGroupTags);
        chipGroupIngredients = findViewById(R.id.chipGroupIngredients);
        recyclerSteps = findViewById(R.id.recyclerSteps);
        btnStartCooking = findViewById(R.id.btnStartCooking);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadRecipeData() {
        recipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (recipe == null) {
            Toast.makeText(this, "Error: Recipe not found", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set data
        ivRecipeBanner.setImageResource(recipe.getImageResource());
        tvRecipeTitle.setText(recipe.getName());
        tvRecipeSubtitle.setText("Uses: " + recipe.getUsesItems());
        tvCookTime.setText(recipe.getCookTime());
        tvCalories.setText(recipe.getCalories());

        // Add tags
        chipGroupTags.removeAllViews();
        if (recipe.getTags() != null) {
            for (String tag : recipe.getTags()) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.background);
                chipGroupTags.addView(chip);
            }
        }

        // Add ingredients
        chipGroupIngredients.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                Chip chip = new Chip(this);
                chip.setText(ingredient);
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getResources().getColor(android.R.color.white));
                chipGroupIngredients.addView(chip);
            }
        }

        // Setup steps
        setupStepsRecyclerView();
    }

    private void setupStepsRecyclerView() {
        recyclerSteps.setLayoutManager(new LinearLayoutManager(this));
        if (recipe.getSteps() != null) {
            StepAdapter stepAdapter = new StepAdapter(this, recipe.getSteps());
            recyclerSteps.setAdapter(stepAdapter);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupButtons() {
        btnStartCooking.setOnClickListener(v ->
                Toast.makeText(this, "Starting cooking mode for: " + recipe.getName(),
                        Toast.LENGTH_SHORT).show()
        );

        btnSave.setOnClickListener(v ->
                Toast.makeText(this, "Recipe saved!", Toast.LENGTH_SHORT).show()
        );
    }
}
