package com.example.foodshelfscanner.adapters;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.example.foodshelfscanner.utils.DbHelper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodViewHolder> {

    private final Context context;
    private final List<FoodItem> foodItems;
    private final OnItemDeletedListener listener;
    private final DbHelper dbHelper;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    public interface OnItemDeletedListener {
        void onItemDeleted();
    }

    public FoodItemAdapter(Context context, List<FoodItem> foodItems, OnItemDeletedListener listener) {
        this.context = context;
        this.foodItems = foodItems;
        this.listener = listener;
        this.dbHelper = new DbHelper();
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
        holder.ivFoodAvatar.setImageResource(android.R.drawable.ic_menu_gallery); // Placeholder image

        // Button listeners
        holder.btnConsume.setOnClickListener(v -> deleteItem(item, position));
        holder.btnDiscard.setOnClickListener(v -> deleteItem(item, position));
    }

    private void deleteItem(FoodItem item, int position) {
        executor.execute(() -> {
            boolean success = dbHelper.deleteItem(item.getId());
            mainThreadHandler.post(() -> {
                if (success) {
                    foodItems.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, foodItems.size());
                    Toast.makeText(context, "Removed: " + item.getName(), Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onItemDeleted();
                    }
                } else {
                    Toast.makeText(context, "Failed to remove item", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
