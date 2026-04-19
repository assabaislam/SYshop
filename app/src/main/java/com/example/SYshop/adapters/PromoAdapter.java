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

public class PromoAdapter extends RecyclerView.Adapter<PromoAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> promoList;
    private final ProductAdapter.OnCartUpdatedListener cartUpdatedListener;
    private final OnFavoriteChangedListener favoriteChangedListener;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final FavoriteSyncRepository favoriteSyncRepository;
    private final CartSyncRepository cartSyncRepository;

    public interface OnFavoriteChangedListener {
        void onFavoriteChanged();
    }

    public PromoAdapter(Context context,
                        List<Product> promoList,
                        ProductAdapter.OnCartUpdatedListener cartUpdatedListener,
                        OnFavoriteChangedListener favoriteChangedListener) {
        this.context = context;
        this.promoList = new ArrayList<>(promoList);
        this.cartUpdatedListener = cartUpdatedListener;
        this.favoriteChangedListener = favoriteChangedListener;
        this.favoriteCacheRepository = new FavoriteCacheRepository(context);
        this.favoriteSyncRepository = new FavoriteSyncRepository(context);
        this.cartSyncRepository = new CartSyncRepository(context);
        setHasStableIds(true);
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
        int fallbackRes = product.getPreferredLocalImageRes();

        boolean showOldPrice = product.hasOffer() && !TextUtils.isEmpty(product.getOldPrice().trim());
        String promoTag = TextUtils.isEmpty(product.getTag().trim())
                ? context.getString(R.string.product_tag_promo)
                : product.getTag();

        holder.txtPromoTag.setText(promoTag);
        holder.txtPromoName.setText(product.getName());
        holder.txtPromoPrice.setText(product.getPrice());
        if (showOldPrice) {
            holder.txtPromoOldPrice.setVisibility(View.VISIBLE);
            holder.txtPromoOldPrice.setText(product.getOldPrice());
            holder.txtPromoOldPrice.setPaintFlags(holder.txtPromoOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.txtPromoOldPrice.setVisibility(View.GONE);
            holder.txtPromoOldPrice.setText("");
        }

        ProductImageLoader.loadCenterCrop(holder.imgPromoProduct, product.getImageUrl(), fallbackRes);

        if (FavoriteManager.isFavorite(product) || favoriteCacheRepository.isFavoriteCached(product.getId())) {
            holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_not_fill);
        }

        holder.btnPromoAdd.setOnClickListener(v -> {
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

        holder.imgPromoFav.setOnClickListener(v -> {
            if (!AuthManager.requireLogin(context)) {
                return;
            }

            boolean isNowFavorite = FavoriteManager.toggleFavorite(product);

            if (isNowFavorite) {
                favoriteCacheRepository.saveFavorite(product);
                favoriteSyncRepository.addFavorite(product, null);
                holder.imgPromoFav.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(context, product.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteCacheRepository.removeFavorite(product.getId());
                favoriteSyncRepository.removeFavorite(product, null);
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

    @Override
    public long getItemId(int position) {
        return promoList.get(position).getId();
    }

    public void submitProducts(List<Product> products) {
        List<Product> nextProducts = products == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(products);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PromoDiffCallback(promoList, nextProducts));
        promoList.clear();
        promoList.addAll(nextProducts);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView txtPromoTag, txtPromoName, txtPromoPrice, txtPromoOldPrice, btnPromoAdd;
        final ImageView imgPromoProduct, imgPromoFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPromoTag = itemView.findViewById(R.id.txtPromoTag);
            txtPromoName = itemView.findViewById(R.id.txtPromoName);
            txtPromoPrice = itemView.findViewById(R.id.txtPromoPrice);
            txtPromoOldPrice = itemView.findViewById(R.id.txtPromoOldPrice);
            btnPromoAdd = itemView.findViewById(R.id.btnPromoAdd);
            imgPromoProduct = itemView.findViewById(R.id.imgPromoProduct);
            imgPromoFav = itemView.findViewById(R.id.imgPromoFav);
        }
    }

    private static class PromoDiffCallback extends DiffUtil.Callback {
        private final List<Product> oldList;
        private final List<Product> newList;

        PromoDiffCallback(List<Product> oldList, List<Product> newList) {
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
