package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;

import java.util.List;

public class ProductImagesAdapter extends RecyclerView.Adapter<ProductImagesAdapter.ViewHolder> {

    private Context context;
    private List<Integer> imagesList;
    private OnImageClickListener listener;

    public interface OnImageClickListener {
        void onImageClick(int imageRes);
    }

    public ProductImagesAdapter(Context context, List<Integer> imagesList, OnImageClickListener listener) {
        this.context = context;
        this.imagesList = imagesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int imageRes = imagesList.get(position);
        holder.imgSmallProduct.setImageResource(imageRes);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(imageRes);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagesList != null ? imagesList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSmallProduct;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgSmallProduct = itemView.findViewById(R.id.imgSmallProduct);
        }
    }
}