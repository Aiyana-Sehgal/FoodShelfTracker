package com.example.foodshelfscanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.R;
import com.example.foodshelfscanner.models.FoodItem;

import java.util.List;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder> {

    private Context context;
    private List<FoodItem> foodItems;

    public FoodItemAdapter(Context context, List<FoodItem> foodItems) {
        this.context = context;
        this.foodItems = foodItems;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);

        holder.tvItemName.setText(item.getName());
        holder.tvFoodType.setText(item.getType());
        holder.tvAddedTime.setText(item.getAddedTime() + " • " + item.getLocation());
        holder.tvBrandQuantity.setText("Brand: " + item.getBrand() + " • " + item.getQuantity());
        holder.tvShelfLife.setText(item.getShelfLifeDays() + "d");
        holder.progressShelfLife.setProgress(item.getProgressPercentage());

        // Handle image resource safely with category-specific icons
        int imageResource = item.getImageResource();
        if (imageResource > 0) {
            try {
                holder.ivFoodAvatar.setImageResource(imageResource);
            } catch (Exception e) {
                // If the resource is invalid, use category-specific default
                holder.ivFoodAvatar.setImageResource(getCategoryIcon(item.getType()));
            }
        } else {
            // Use category-specific icon when no image resource is provided
            holder.ivFoodAvatar.setImageResource(getCategoryIcon(item.getType()));
        }

        // Button listeners
        holder.btnConsume.setOnClickListener(v ->
                Toast.makeText(context, "Consumed: " + item.getName(), Toast.LENGTH_SHORT).show()
        );

        holder.btnDiscard.setOnClickListener(v ->
                Toast.makeText(context, "Discarded: " + item.getName(), Toast.LENGTH_SHORT).show()
        );
    }

    private int getCategoryIcon(String category) {
        if (category == null) return R.drawable.ic_food_default;

        String categoryLower = category.toLowerCase();
        if (categoryLower.contains("fruit")) {
            return R.drawable.ic_fruit;
        } else if (categoryLower.contains("vegetable")) {
            return R.drawable.ic_vegetable;
        } else if (categoryLower.contains("meat") || categoryLower.contains("seafood")) {
            return R.drawable.ic_meat;
        } else if (categoryLower.contains("dairy")) {
            return R.drawable.ic_dairy;
        } else {
            return R.drawable.ic_food_default;
        }
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFoodAvatar;
        TextView tvItemName, tvFoodType, tvAddedTime, tvBrandQuantity, tvShelfLife;
        ProgressBar progressShelfLife;
        Button btnConsume, btnDiscard;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            ivFoodAvatar = itemView.findViewById(R.id.ivFoodAvatar);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvFoodType = itemView.findViewById(R.id.tvFoodType);
            tvAddedTime = itemView.findViewById(R.id.tvAddedTime);
            tvBrandQuantity = itemView.findViewById(R.id.tvBrandQuantity);
            tvShelfLife = itemView.findViewById(R.id.tvShelfLife);
            progressShelfLife = itemView.findViewById(R.id.progressShelfLife);
            btnConsume = itemView.findViewById(R.id.btnConsume);
            btnDiscard = itemView.findViewById(R.id.btnDiscard);
        }
    }
}
