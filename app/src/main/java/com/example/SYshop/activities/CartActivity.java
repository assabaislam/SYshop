package com.example.SYshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.adapters.CartAdapter;
import com.example.SYshop.models.CartItem;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.R;

import java.util.List;

public class CartActivity extends BaseActivity implements CartAdapter.OnCartChangedListener {

    private ImageView backBtn;
    private TextView cartCountText, cartEmptyText, totalPriceText;
    private RecyclerView cartRecycler;

    private CartAdapter cartAdapter;
    private List<CartItem> cartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupBackToHome();
        initViews();
        loadCartItems();
        setupClicks();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        cartCountText = findViewById(R.id.cartCountText);
        cartEmptyText = findViewById(R.id.cartEmptyText);
        totalPriceText = findViewById(R.id.totalPriceText);
        cartRecycler = findViewById(R.id.cartRecycler);
    }

    private void loadCartItems() {
        cartList = CartManager.getCartItems();
        updateCartUI();

        cartAdapter = new CartAdapter(this, cartList, this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);
    }

    private void updateCartUI() {
        cartCountText.setText(getString(R.string.items_count, CartManager.getCartCount()));

        if (cartList.isEmpty()) {
            cartEmptyText.setVisibility(View.VISIBLE);
            cartRecycler.setVisibility(View.GONE);
            totalPriceText.setText(getString(R.string.total_price, "0"));
        } else {
            cartEmptyText.setVisibility(View.GONE);
            cartRecycler.setVisibility(View.VISIBLE);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        int total = 0;

        for (CartItem item : cartList) {
            String priceStr = item.getProduct().getPrice().replace("$", "");
            try {
                int price = Integer.parseInt(priceStr);
                total += price * item.getQuantity();
            } catch (NumberFormatException e) {
                // Handle or log error
            }
        }

        totalPriceText.setText(getString(R.string.total_price, String.valueOf(total)));
    }

    private void setupClicks() {
        backBtn.setOnClickListener(v -> navigateToHome(true));
    }


    @Override
    public void onCartChanged() {
        updateCartUI();
    }
}