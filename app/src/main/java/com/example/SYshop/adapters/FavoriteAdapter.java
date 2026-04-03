package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> favoriteList;
    private final OnFavoriteChangedListener listener;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged();
    }

    public FavoriteAdapter(Context context, List<Product> favoriteList, OnFavoriteChangedListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteAdapter.ViewHolder holder, int position) {
        Product product = favoriteList.get(position);

        holder.favoriteItemName.setText(product.getName());
        holder.favoriteItemTag.setText(product.getTag());
        holder.favoriteItemPrice.setText(product.getPrice());
        
        Glide.with(context)
                .load(product.getImageRes())
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.favoriteItemImage);

        holder.removeFavoriteBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                FavoriteManager.removeFavorite(adapterPosition);
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onFavoriteChanged();
                }
            }
        });

        holder.itemView.setOnClickListener(v -> Navigator.openProductDetails(context, product));
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView favoriteItemImage, removeFavoriteBtn;
        TextView favoriteItemName, favoriteItemTag, favoriteItemPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            favoriteItemImage = itemView.findViewById(R.id.favoriteItemImage);
            favoriteItemName = itemView.findViewById(R.id.favoriteItemName);
            favoriteItemTag = itemView.findViewById(R.id.favoriteItemTag);
            favoriteItemPrice = itemView.findViewById(R.id.favoriteItemPrice);
            removeFavoriteBtn = itemView.findViewById(R.id.removeFavoriteBtn);
        }
    }
}