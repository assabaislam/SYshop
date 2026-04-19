package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.models.Category;
import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(String categoryName);
    }

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategorySelectedListener listener) {
        this.context = context;
        this.categoryList = categoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.txtCategory.setText(getDisplayName(category.getName()));
        holder.imgCategory.setImageResource(category.getImageRes() != 0 ? category.getImageRes() : R.drawable.classic_watch1);

        if (category.isSelected()) {
            holder.imageCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.badge_bg));
            holder.imageCard.setStrokeColor(ContextCompat.getColor(context, R.color.accent_coral_dark));
            holder.imageCard.setStrokeWidth(dpToPx(2));
            holder.txtCategory.setTextColor(ContextCompat.getColor(context, R.color.accent_coral_dark));
            holder.txtCategory.setAlpha(1f);
        } else {
            holder.imageCard.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            holder.imageCard.setStrokeColor(ContextCompat.getColor(context, R.color.card_stroke));
            holder.imageCard.setStrokeWidth(dpToPx(1));
            holder.txtCategory.setTextColor(ContextCompat.getColor(context, R.color.text_gray_dark));
            holder.txtCategory.setAlpha(0.92f);
        }

        holder.itemView.setOnClickListener(v -> {
            for (Category c : categoryList) {
                c.setSelected(false);
            }
            category.setSelected(true);
            notifyDataSetChanged();

            if (listener != null) {
                listener.onCategorySelected(category.getName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public void clearSelection() {
        boolean changed = false;
        for (Category category : categoryList) {
            if (category.isSelected()) {
                category.setSelected(false);
                changed = true;
            }
        }

        if (changed) {
            notifyDataSetChanged();
        }
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getDisplayName(String categoryName) {
        if (categoryName == null) {
            return "";
        }

        switch (categoryName.toLowerCase(Locale.ROOT)) {
            case "shoes":
                return context.getString(R.string.category_shoes);
            case "watches":
                return context.getString(R.string.category_watches);
            case "bags":
                return context.getString(R.string.category_bags);
            case "audio":
                return context.getString(R.string.category_audio);
            default:
                return categoryName;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView imageCard;
        ImageView imgCategory;
        TextView txtCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCard = itemView.findViewById(R.id.imageCard);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}
