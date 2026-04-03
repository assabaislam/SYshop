package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.models.Category;
import com.example.SYshop.R;

import java.util.List;

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
        holder.txtCategory.setText(category.getName());

        if (category.isSelected()) {
            holder.txtCategory.setBackgroundResource(R.drawable.bg_category_selected);
            holder.txtCategory.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.txtCategory.setBackgroundResource(R.drawable.bg_category_normal);
            holder.txtCategory.setTextColor(ContextCompat.getColor(context, R.color.navy_button));
        }

        holder.txtCategory.setOnClickListener(v -> {
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategory = itemView.findViewById(R.id.txtCategory);
        }
    }
}