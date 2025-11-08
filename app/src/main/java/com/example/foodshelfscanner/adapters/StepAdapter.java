package com.example.foodshelfscanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodshelfscanner.R;

import java.util.List;

public class StepAdapter extends RecyclerView.Adapter<StepAdapter.StepViewHolder> {

    private final Context context;
    private final List<String> steps;

    public StepAdapter(Context context, List<String> steps) {
        this.context = context;
        this.steps = steps;
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        String step = steps.get(position);
        holder.tvStepNumber.setText((position + 1) + ".");
        holder.tvStepText.setText(step);
    }

    @Override
    public int getItemCount() {
        return steps != null ? steps.size() : 0;
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        TextView tvStepNumber, tvStepText;

        public StepViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStepNumber = itemView.findViewById(R.id.tvStepNumber);
            tvStepText = itemView.findViewById(R.id.tvStepText);
        }
    }
}
