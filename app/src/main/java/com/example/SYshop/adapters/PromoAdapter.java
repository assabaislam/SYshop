package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> promoList;
    private final ProductAdapter.OnCartUpdatedListener cartUpdatedListener;
    private final OnFavoriteChangedListener favoriteChangedListener;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged();
    }

    public PromoAdapter(Context context,
                        List<Product> promoList,
                        ProductAdapter.OnCartUpdatedListener cartUpdatedListener,
                        OnFavoriteChangedListener favoriteChangedListener) {
        this.context = context;
        this.promoList = promoList;
        this.cartUpdatedListener = cartUpdatedListener;
        this.favoriteChangedListener = favoriteChangedListener;
    }

    @NonNull
    @Override
    public PromoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_promo_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PromoAdapter.ViewHolder holder, int position) {
        Product product = promoList.get(position);

        holder.txtPromoTag.setText(product.getTag());
        holder.txtPromoName.setText(product.getName());
        holder.txtPromoPrice.setText(product.getPrice());

        Glide.with(context)
                .load(product.getImageRes())
                .centerCrop()
                .override(400, 400)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgPromoProduct);

        if (FavoriteManager.isFavorite(product)) {
            holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_not_fill);
        }

        holder.btnPromoAdd.setOnClickListener(v -> {
            CartManager.addToCart(product);
            Toast.makeText(context, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();

            if (cartUpdatedListener != null) {
                cartUpdatedListener.onCartUpdated();
            }
        });

        holder.imgPromoFav.setOnClickListener(v -> {
            FavoriteManager.toggleFavorite(product);

            if (FavoriteManager.isFavorite(product)) {
                holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(context, product.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_not_fill);
                Toast.makeText(context, product.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
            }

            if (favoriteChangedListener != null) {
                favoriteChangedListener.onFavoriteChanged();
            }
        });

        holder.itemView.setOnClickListener(v -> Navigator.openProductDetails(context, product));
    }

    @Override
    public int getItemCount() {
        return promoList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtPromoTag, txtPromoName, txtPromoPrice, btnPromoAdd;
        final ImageView imgPromoProduct, imgPromoFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPromoTag = itemView.findViewById(R.id.txtPromoTag);
            txtPromoName = itemView.findViewById(R.id.txtPromoName);
            txtPromoPrice = itemView.findViewById(R.id.txtPromoPrice);
            btnPromoAdd = itemView.findViewById(R.id.btnPromoAdd);
            imgPromoProduct = itemView.findViewById(R.id.imgPromoProduct);
            imgPromoFav = itemView.findViewById(R.id.imgPromoFav);
        }
    }
}