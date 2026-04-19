package com.example.SYshop.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.database.FavoriteCacheRepository;
import com.example.SYshop.database.FavoriteSyncRepository;
import com.example.SYshop.database.CartSyncRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.AuthManager;
import com.example.SYshop.utils.Navigator;
import com.example.SYshop.utils.ProductImageLoader;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final List<Product> fullProductList;
    private final OnCartUpdatedListener cartUpdatedListener;
    private final OnFavoriteChangedListener favoriteChangedListener;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final FavoriteSyncRepository favoriteSyncRepository;
    private final CartSyncRepository cartSyncRepository;

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
        this.favoriteCacheRepository = new FavoriteCacheRepository(context);
        this.favoriteSyncRepository = new FavoriteSyncRepository(context);
        this.cartSyncRepository = new CartSyncRepository(context);
        setHasStableIds(true);
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
        int fallbackRes = product.getPreferredLocalImageRes();

        holder.txtTag.setText(product.getTag());
        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(product.getPrice());
        boolean showOldPrice = product.hasOffer() && !TextUtils.isEmpty(product.getOldPrice().trim());
        boolean showTag = !TextUtils.isEmpty(product.getTag().trim()) && !"all".equalsIgnoreCase(product.getTag().trim());

        holder.txtTag.setVisibility(showTag ? View.VISIBLE : View.GONE);
        if (showOldPrice) {
            holder.txtOldPrice.setVisibility(View.VISIBLE);
            holder.txtOldPrice.setText(product.getOldPrice());
            holder.txtOldPrice.setPaintFlags(holder.txtOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.txtOldPrice.setVisibility(View.GONE);
            holder.txtOldPrice.setText("");
        }

        ProductImageLoader.loadCenterCrop(holder.imgProduct, product.getImageUrl(), fallbackRes);

        if (FavoriteManager.isFavorite(product) || favoriteCacheRepository.isFavoriteCached(product.getId())) {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.imgFav.setImageResource(R.drawable.ic_favorite_not_fill);
        }

        holder.btnAdd.setOnClickListener(v -> {
            if (!AuthManager.requireLogin(context)) {
                return;
            }

            CartManager.addToCart(product);
            cartSyncRepository.addOrIncreaseCartItem(product, null);

            Toast.makeText(context, product.getName() + " added to cart", Toast.LENGTH_SHORT).show();

            if (cartUpdatedListener != null) {
                cartUpdatedListener.onCartUpdated();
            }
        });

        holder.imgFav.setOnClickListener(v -> {
            if (!AuthManager.requireLogin(context)) {
                return;
            }

            boolean isNowFavorite = FavoriteManager.toggleFavorite(product);

            if (isNowFavorite) {
                favoriteCacheRepository.saveFavorite(product);
                favoriteSyncRepository.addFavorite(product, null);
                holder.imgFav.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(context, product.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteCacheRepository.removeFavorite(product.getId());
                favoriteSyncRepository.removeFavorite(product, null);
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

    @Override
    public long getItemId(int position) {
        return productList.get(position).getId();
    }

    public void submitProducts(List<Product> products) {
        fullProductList.clear();
        if (products != null) {
            fullProductList.addAll(products);
        }
        applyFilters();
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
        String searchText = currentQuery.toLowerCase().trim();
        List<Product> filteredProducts = new ArrayList<>();

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
                filteredProducts.add(product);
            }
        }

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ProductDiffCallback(productList, filteredProducts));
        productList.clear();
        productList.addAll(filteredProducts);
        diffResult.dispatchUpdatesTo(this);
    }

    public boolean isEmpty() {
        return productList.isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTag, txtName, txtPrice, txtOldPrice, btnAdd;
        ImageView imgProduct, imgFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTag = itemView.findViewById(R.id.txtTag);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtOldPrice = itemView.findViewById(R.id.txtOldPrice);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            imgFav = itemView.findViewById(R.id.imgFav);
        }
    }

    private static class ProductDiffCallback extends DiffUtil.Callback {
        private final List<Product> oldList;
        private final List<Product> newList;

        ProductDiffCallback(List<Product> oldList, List<Product> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Product oldItem = oldList.get(oldItemPosition);
            Product newItem = newList.get(newItemPosition);
            return oldItem.getId() == newItem.getId()
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice().equals(newItem.getPrice())
                    && oldItem.getTag().equals(newItem.getTag())
                    && oldItem.getImageUrl().equals(newItem.getImageUrl())
                    && oldItem.getImageRes() == newItem.getImageRes();
        }
    }
}
