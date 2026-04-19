package com.example.SYshop.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.adapters.OrderAdapter;
import com.example.SYshop.database.OrderSyncRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.Order;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrdersActivity extends BaseActivity {

    private BottomNavigationView bottomNavigation;
    private ImageView cartBtn;
    private TextView cartBadgeText;
    private TextView screenTitle;
    private TextView ordersEmptyText;
    private TextView ordersSummaryText;
    private RecyclerView ordersRecycler;
    private LinearLayout emptyStateLayout;

    private OrderSyncRepository orderSyncRepository;
    private OrderAdapter orderAdapter;
    private final List<Order> orderList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        setupBackToHome();

        orderSyncRepository = new OrderSyncRepository();

        initViews();
        setupRecycler();
        setupBottomNavigation();
        setupClicks();
        loadOrders();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        cartBtn = findViewById(R.id.cartBtn);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        screenTitle = findViewById(R.id.screenTitle);
        ordersEmptyText = findViewById(R.id.ordersEmptyText);
        ordersSummaryText = findViewById(R.id.ordersSummaryText);
        ordersRecycler = findViewById(R.id.ordersRecycler);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
    }

    private void setupRecycler() {
        orderAdapter = new OrderAdapter(this, orderList, new OrderAdapter.OnOrderClickListener() {
            @Override
            public void onOrderClick(Order order) {
                Intent intent = new Intent(OrdersActivity.this, OrderDetailsActivity.class);
                intent.putExtra("orderId", order.getOrderId());
                intent.putExtra("totalPrice", order.getTotalPrice());
                intent.putExtra("status", order.getStatus());
                intent.putExtra("createdAt", order.getCreatedAt());
                intent.putExtra("itemsCount", order.getItemsCount());
                startActivity(intent);
            }

            @Override
            public void onShareOrder(Order order) {
                shareOrder(order);
            }
        });
        ordersRecycler.setLayoutManager(new LinearLayoutManager(this));
        ordersRecycler.setAdapter(orderAdapter);
    }

    private void shareOrder(Order order) {
        if (order == null) {
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, buildOrderShareMessage(order));

        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.order_share_chooser)));
        } catch (ActivityNotFoundException exception) {
            android.widget.Toast.makeText(this, getString(R.string.order_share_unavailable), android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    private String buildOrderShareMessage(Order order) {
        String safeOrderId = order.getOrderId() == null ? "" : order.getOrderId().trim();
        String shortOrderId = safeOrderId.length() > 8 ? safeOrderId.substring(0, 8) : safeOrderId;
        return getString(
                R.string.order_share_message_with_status,
                shortOrderId,
                order.getItemsCount(),
                order.getTotalPrice(),
                getLocalizedStatus(order.getStatus())
        );
    }

    private String getLocalizedStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return getString(R.string.order_status_placed);
        }

        String normalizedStatus = rawStatus.trim().toLowerCase(java.util.Locale.ROOT);
        switch (normalizedStatus) {
            case "placed":
                return getString(R.string.order_status_placed);
            case "pending":
                return getString(R.string.order_status_pending);
            case "confirmed":
                return getString(R.string.order_status_confirmed);
            case "shipped":
                return getString(R.string.order_status_shipped);
            case "delivered":
                return getString(R.string.order_status_delivered);
            case "cancelled":
            case "canceled":
                return getString(R.string.order_status_cancelled);
            default:
                return rawStatus;
        }
    }

    private void loadOrders() {
        ordersEmptyText.setText("Loading orders...");
        emptyStateLayout.setVisibility(View.VISIBLE);
        ordersRecycler.setVisibility(View.GONE);

        orderSyncRepository.loadOrders(new OrderSyncRepository.LoadOrdersCallback() {
            @Override
            public void onLoaded(List<Map<String, Object>> orders) {
                orderList.clear();

                for (Map<String, Object> order : orders) {
                    String orderId = value(order.get("orderId"));
                    String totalPrice = value(order.get("totalPrice"));
                    String status = value(order.get("status"));
                    long createdAt = longValue(order.get("createdAt"));
                    int itemsCount = getInt(order.get("itemsCount"));

                    orderList.add(new Order(orderId, totalPrice, status, createdAt, itemsCount));
                }

                orderAdapter.notifyDataSetChanged();
                updateSummary();
                updateOrdersUI();
            }

            @Override
            public void onError(String message) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                ordersRecycler.setVisibility(View.GONE);
                ordersEmptyText.setText(
                        message == null || message.trim().isEmpty()
                                ? getString(R.string.orders_empty)
                                : message
                );
            }
        });
    }

    private void updateOrdersUI() {
        if (orderList.isEmpty()) {emptyStateLayout.setVisibility(View.VISIBLE);
            ordersRecycler.setVisibility(View.GONE);
            ordersEmptyText.setText(getString(R.string.orders_empty));
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            ordersRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void updateSummary() {
        if (orderList.isEmpty()) {
            ordersSummaryText.setText(getString(R.string.orders_summary_empty));
        } else {
            ordersSummaryText.setText(getString(R.string.orders_summary_count, orderList.size()));
        }
    }

    private void setupClicks() {
        cartBtn.setOnClickListener(v -> {
            startActivity(new Intent(OrdersActivity.this, CartActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
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

    private String value(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private long longValue(Object o) {
        if (o instanceof Number) {
            return ((Number) o).longValue();
        }
        return 0L;
    }

    private int getInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
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

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
        loadOrders();
    }
}
