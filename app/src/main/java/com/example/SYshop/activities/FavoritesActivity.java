package com.example.SYshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.managers.CartManager;
import com.example.SYshop.adapters.FavoriteAdapter;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class FavoritesActivity extends BaseActivity implements FavoriteAdapter.OnFavoriteChangedListener {

    private BottomNavigationView bottomNavigation;
    private TextView cartBadgeText, favoritesEmptyText;
    private ImageView cartBtn;
    private RecyclerView favoritesRecycler;

    private FavoriteAdapter favoriteAdapter;
    private List<Product> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        setupBackToHome();

        initViews();
        loadFavorites();
        setupBottomNavigation();
        setupClicks();
        updateCartBadge();

    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        cartBtn = findViewById(R.id.cartBtn);
        favoritesEmptyText = findViewById(R.id.favoritesEmptyText);
        favoritesRecycler = findViewById(R.id.favoritesRecycler);
    }

    private void loadFavorites() {
        favoriteList = FavoriteManager.getFavoriteItems();
        updateFavoritesUI();

        favoriteAdapter = new FavoriteAdapter(this, favoriteList, this);
        favoritesRecycler.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecycler.setAdapter(favoriteAdapter);
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
        updateFavoritesUI();
        if (favoriteAdapter != null) {
            favoriteAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFavoriteChanged() {
        updateFavoritesUI();
    }
}