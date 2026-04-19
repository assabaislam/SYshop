package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.SYshop.database.FavoriteCacheRepository;
import com.example.SYshop.database.FavoriteSyncRepository;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;
import com.example.SYshop.utils.ProductImageLoader;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> favoriteList;
    private final OnFavoriteChangedListener listener;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final FavoriteSyncRepository favoriteSyncRepository;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged();
    }

    public FavoriteAdapter(Context context, List<Product> favoriteList, OnFavoriteChangedListener listener) {
        this.context = context;
        this.favoriteList = favoriteList;
        this.listener = listener;
        this.favoriteCacheRepository = new FavoriteCacheRepository(context);
        this.favoriteSyncRepository = new FavoriteSyncRepository(context);
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
        
        ProductImageLoader.loadCenterCrop(
                holder.favoriteItemImage,
                product.getImageUrl(),
                product.getPreferredLocalImageRes()
        );

        holder.removeFavoriteBtn.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                Product current = favoriteList.get(adapterPosition);
                FavoriteManager.removeFavoriteById(current.getId());
                favoriteCacheRepository.removeFavorite(current.getId());
                favoriteSyncRepository.removeFavorite(current, null);
                favoriteList.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);

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
