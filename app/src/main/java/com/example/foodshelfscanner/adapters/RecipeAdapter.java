package com.example.foodshelfscanner.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.R;
import com.example.foodshelfscanner.RecipeDetailActivity;
import com.example.foodshelfscanner.models.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private Context context;
    private List<Recipe> recipes;

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);

        holder.tvRecipeName.setText(recipe.getName());
        holder.tvRecipeUses.setText("Uses: " + recipe.getUsesItems());
        holder.ivRecipeThumb.setImageResource(recipe.getImageResource());

        // Add ingredient chips
        holder.chipGroupIngredients.removeAllViews();
        if (recipe.getIngredients() != null) {
            for (String ingredient : recipe.getIngredients()) {
                Chip chip = new Chip(context);
                chip.setText(ingredient);
                chip.setChipBackgroundColorResource(R.color.accent);
                holder.chipGroupIngredients.addView(chip);
            }
        }

        // Click listener for recipe card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            context.startActivity(intent);
        });

        // Button listeners
        holder.btnAddMealPlan.setOnClickListener(v ->
                Toast.makeText(context, "Added to Meal Plan: " + recipe.getName(), Toast.LENGTH_SHORT).show()
        );

        holder.btnCreateShoppingList.setOnClickListener(v ->
                Toast.makeText(context, "Shopping List Created", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRecipeThumb;
        TextView tvRecipeName, tvRecipeUses;
        ChipGroup chipGroupIngredients;
        Button btnAddMealPlan, btnCreateShoppingList;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRecipeThumb = itemView.findViewById(R.id.ivRecipeThumb);
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvRecipeUses = itemView.findViewById(R.id.tvRecipeUses);
            chipGroupIngredients = itemView.findViewById(R.id.chipGroupIngredients);
            btnAddMealPlan = itemView.findViewById(R.id.btnAddMealPlan);
            btnCreateShoppingList = itemView.findViewById(R.id.btnCreateShoppingList);
        }
    }
}
