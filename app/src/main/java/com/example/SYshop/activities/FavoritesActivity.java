package com.example.SYshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.adapters.FavoriteAdapter;
import com.example.SYshop.database.FavoriteCacheRepository;
import com.example.SYshop.database.FavoriteSyncRepository;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.AuthManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FavoritesActivity extends BaseActivity implements FavoriteAdapter.OnFavoriteChangedListener {

    private BottomNavigationView bottomNavigation;
    private TextView cartBadgeText, favoritesEmptyText;
    private ImageView cartBtn;
    private RecyclerView favoritesRecycler;

    private FavoriteAdapter favoriteAdapter;
    private final List<Product> favoriteList = new ArrayList<>();
    private FavoriteCacheRepository favoriteCacheRepository;
    private FavoriteSyncRepository favoriteSyncRepository;
    private ProductCacheRepository productCacheRepository;
    private ProductRepository productRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setupBackToHome();

        if (!AuthManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        favoriteCacheRepository = new FavoriteCacheRepository(this);
        favoriteSyncRepository = new FavoriteSyncRepository(this);
        productCacheRepository = new ProductCacheRepository(this);
        productRepository = new ProductRepository();

        initViews();
        setupBottomNavigation();
        setupClicks();
        loadFavoritesFromCache();
        syncFavoritesFromCloud();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        cartBtn = findViewById(R.id.cartBtn);
        favoritesEmptyText = findViewById(R.id.favoritesEmptyText);
        favoritesRecycler = findViewById(R.id.favoritesRecycler);
    }

    private void loadFavoritesFromCache() {
        favoriteList.clear();

        List<Product> managerFavorites = FavoriteManager.getFavoriteItems();
        List<Product> cachedFavorites = favoriteCacheRepository.getAllFavorites();

        if (cachedFavorites != null && !cachedFavorites.isEmpty()) {
            favoriteList.addAll(cachedFavorites);
        }

        if (managerFavorites != null && !managerFavorites.isEmpty()) {
            for (Product product : managerFavorites) {
                if (!containsFavorite(product.getId())) {
                    favoriteList.add(product);
                }
            }
        }

        enrichFavoritesWithCachedProducts();

        updateFavoritesUI();

        if (favoriteAdapter == null) {
            favoriteAdapter = new FavoriteAdapter(this, favoriteList, this);
            favoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
            favoritesRecycler.setAdapter(favoriteAdapter);
        } else {
            favoriteAdapter.notifyDataSetChanged();
        }

        refreshFavoritesFromProducts();
    }

    private void syncFavoritesFromCloud() {
        favoriteSyncRepository.loadFavoritesFromCloud(new FavoriteSyncRepository.LoadFavoritesCallback() {
            @Override
            public void onLoaded(List<Product> favorites) {
                favoriteList.clear();
                favoriteList.addAll(favorites);
                FavoriteManager.replaceFavorites(favorites);
                enrichFavoritesWithCachedProducts();
                updateFavoritesUI();
                if (favoriteAdapter != null) {
                    favoriteAdapter.notifyDataSetChanged();
                }
                refreshFavoritesFromProducts();
            }

            @Override
            public void onError(String message) {
                if (message != null && !message.trim().isEmpty()) {
                    Toast.makeText(FavoritesActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateFavoritesUI() {
        boolean isEmpty = favoriteList.isEmpty();
        favoritesEmptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        favoritesRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setupClicks() {
        cartBtn.setOnClickListener(v -> navigateTo(CartActivity.class, false));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_favorites);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigateToHome(true);
                return true;
            } else if (id == R.id.nav_orders) {
                navigateTo(OrdersActivity.class, true);
                return true;
            } else if (id == R.id.nav_favorites) {
                return true;
            } else if (id == R.id.nav_profile) {
                navigateTo(ProfileActivity.class, true);
                return true;
            }

            return false;
        });
    }

    private void updateCartBadge() {
        int count = CartManager.getCartCount();

        if (count > 0) {
            cartBadgeText.setVisibility(View.VISIBLE);
            cartBadgeText.setText(String.valueOf(count));
        } else {
            cartBadgeText.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
        loadFavoritesFromCache();
        syncFavoritesFromCloud();
    }

    @Override
    public void onFavoriteChanged() {
        loadFavoritesFromCache();
        syncFavoritesFromCloud();
    }

    private void enrichFavoritesWithCachedProducts() {
        Map<Integer, Product> bestProductsById = new HashMap<>();

        for (Product product : productCacheRepository.getCachedProducts()) {
            bestProductsById.put(product.getId(), product);
        }

        for (Product product : productRepository.getCachedProducts()) {
            bestProductsById.put(product.getId(), product);
        }

        for (Product favorite : favoriteList) {
            Product bestMatch = bestProductsById.get(favorite.getId());
            if (bestMatch != null) {
                copyProductData(favorite, bestMatch);
            }
        }
    }

    private void refreshFavoritesFromProducts() {
        for (int i = 0; i < favoriteList.size(); i++) {
            Product favorite = favoriteList.get(i);
            final int index = i;

            productRepository.loadProductById(favorite.getId(), new ProductRepository.LoadProductCallback() {
                @Override
                public void onLoaded(Product product) {
                    if (product == null || index >= favoriteList.size()) {
                        return;
                    }

                    Product current = favoriteList.get(index);
                    if (current.getId() != product.getId()) {
                        return;
                    }

                    copyProductData(current, product);
                    favoriteCacheRepository.saveFavorite(current);
                    if (favoriteAdapter != null) {
                        favoriteAdapter.notifyItemChanged(index);
                    }
                }

                @Override
                public void onError(String message) {
                    // Keep the best locally available data.
                }
            });
        }
    }

    private void copyProductData(Product target, Product source) {
        target.setCategory(source.getCategory());
        target.setTag(source.getTag());
        target.setName(source.getName());
        target.setPrice(source.getPrice());
        target.setDescription(source.getDescription());
        target.setImageName(source.getPreferredLocalImageName());
        target.setImageRes(source.getPreferredLocalImageRes());
        target.setImagesList(source.getImagesList());
        target.setImageUrl(source.getImageUrl());
        target.setHasOffer(source.hasOffer());
        target.setDiscountPercent(source.getDiscountPercent());
        target.setOldPrice(source.getOldPrice());
        target.setRating(source.getRating());
        target.setReviewCount(source.getReviewCount());
    }

    private boolean containsFavorite(int productId) {
        for (Product favorite : favoriteList) {
            if (favorite.getId() == productId) {
                return true;
            }
        }
        return false;
    }
}
