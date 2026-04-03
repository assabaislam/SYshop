package com.example.SYshop.activities;

import android.os.Bundle;
import com.example.SYshop.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileActivity extends BaseActivity  {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBackToHome();
        bottomNavigation = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigateToHome(true);
                return true;
            } else if (id == R.id.nav_orders) {
                navigateTo(OrdersActivity.class, true);
                return true;
            } else if (id == R.id.nav_favorites) {
                navigateTo(FavoritesActivity.class, true);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }
}