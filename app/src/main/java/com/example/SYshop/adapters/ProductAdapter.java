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

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final List<Product> fullProductList;
    private final OnCartUpdatedListener cartUpdatedListener;
    private final OnFavoriteChangedListener favoriteChangedListener;

    private String currentQuery = "";
    private String currentCategory = "All";
    private String currentTag = "All";

    public interface OnCartUpdatedListener {
        void onCartUpdated();
    }

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged();
    }

    public ProductAdapter(Context context,
                          List<Product> productList,
                          OnCartUpdatedListener cartUpdatedListener,
                          OnFavoriteChangedListener favoriteChangedListener) {
        this.context = context;
        this.productList = new ArrayList<>(productList);
        this.fullProductList = new ArrayList<>(productList);
        this.cartUpdatedListener = cartUpdatedListener;
        this.favoriteChangedListener = favoriteChangedListener;
    }

    @NonNull
    @Override
    public ProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.txtTag.setText(product.getTag());
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(product.getPrice());

        Glide.with(context)
                .load(product.getImageRes())
                .centerCrop() // Fix: Cover the designated frame completely
                .override(400, 400)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.imgProduct);

        if (FavoriteManager.isFavorite(product)) {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_not_fill);
        }

        holder.btnAdd.setOnClickListener(v -> {
            CartManager.addToCart(product);
            Toast.makeText(context, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();

            if (cartUpdatedListener != null) {
                cartUpdatedListener.onCartUpdated();
            }
        });

        holder.imgFav.setOnClickListener(v -> {
            FavoriteManager.toggleFavorite(product);

            if (FavoriteManager.isFavorite(product)) {
                holder.imgFav.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(context, product.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                holder.imgFav.setImageResource(R.drawable.ic_favorite_not_fill);
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
        return productList.size();
    }

    public void setSearchQuery(String query) {
        currentQuery = query == null ? "" : query;
        applyFilters();
    }

    public void setCategory(String category) {
        currentCategory = category == null ? "All" : category;
        applyFilters();
    }

    public void setTag(String tag) {
        currentTag = tag == null ? "All" : tag;
        applyFilters();
    }

    private void applyFilters() {
        productList.clear();

        String searchText = currentQuery.toLowerCase().trim();

        for (Product product : fullProductList) {
            boolean matchesCategory = currentCategory.equalsIgnoreCase("All")
                    || product.getCategory().equalsIgnoreCase(currentCategory);

            boolean matchesTag = currentTag.equalsIgnoreCase("All")
                    || product.getTag().equalsIgnoreCase(currentTag);

            boolean matchesSearch = searchText.isEmpty()
                    || product.getName().toLowerCase().contains(searchText)
                    || product.getTag().toLowerCase().contains(searchText)
                    || product.getDescription().toLowerCase().contains(searchText)
                    || product.getCategory().toLowerCase().contains(searchText);

            if (matchesCategory && matchesTag && matchesSearch) {
                productList.add(product);
            }
        }

        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return productList.isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTag, txtName, txtPrice, btnAdd;
        ImageView imgProduct, imgFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTag = itemView.findViewById(R.id.txtTag);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFav = itemView.findViewById(R.id.imgFav);

        }
    }
}