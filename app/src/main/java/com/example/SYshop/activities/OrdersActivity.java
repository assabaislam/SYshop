package com.example.SYshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.SYshop.managers.CartManager;
import com.example.SYshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class OrdersActivity extends BaseActivity {

    private BottomNavigationView bottomNavigation;
    private TextView cartBadgeText;
    private ImageView cartBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        setupBackToHome();

        initViews();
        setupClicks();
        setupBottomNavigation();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        cartBtn = findViewById(R.id.cartBtn);
    }

    private void setupClicks() {
        cartBtn.setOnClickListener(v -> navigateTo(CartActivity.class, false));
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_orders);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigateToHome(true);
                return true;
            } else if (id == R.id.nav_orders) {
                return true;
            } else if (id == R.id.nav_favorites) {
                navigateTo(FavoritesActivity.class, true);
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
    }
}