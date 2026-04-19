package com.example.SYshop.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.adapters.OrderItemsAdapter;
import com.example.SYshop.database.OrderSyncRepository;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.models.OrderItem;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.Navigator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderDetailsActivity extends BaseActivity {

    private ImageView backBtn;
    private TextView orderIdText, orderStatusText, orderDateText, orderTotalText, itemsCountText, emptyText;
    private RecyclerView itemsRecycler;

    private OrderItemsAdapter adapter;
    private final List<OrderItem> orderItems = new ArrayList<>();
    private OrderSyncRepository orderSyncRepository;
    private ProductRepository productRepository;
    private ProductCacheRepository productCacheRepository;

    private String orderId;
    private String totalPrice;
    private String status;
    private long createdAt;
    private int itemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);
        setupBackToHome();

        orderSyncRepository = new OrderSyncRepository();
        productRepository = new ProductRepository();
        productCacheRepository = new ProductCacheRepository(this);

        initViews();
        readIntent();
        setupRecycler();
        setupHeader();
        setupClicks();
        loadOrderItems();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        orderIdText = findViewById(R.id.orderIdText);
        orderStatusText = findViewById(R.id.orderStatusText);
        orderDateText = findViewById(R.id.orderDateText);
        orderTotalText = findViewById(R.id.orderTotalText);
        itemsCountText = findViewById(R.id.itemsCountText);
        emptyText = findViewById(R.id.emptyText);
        itemsRecycler = findViewById(R.id.itemsRecycler);
    }

    private void readIntent() {
        orderId = getIntent().getStringExtra("orderId");
        totalPrice = getIntent().getStringExtra("totalPrice");
        status = getIntent().getStringExtra("status");
        createdAt = getIntent().getLongExtra("createdAt", 0L);
        itemsCount = getIntent().getIntExtra("itemsCount", 0);
    }

    private void setupRecycler() {
        adapter = new OrderItemsAdapter(this, orderItems, item -> {
            Product product = new Product(
                    item.getProductId(),
                    item.getCategory(),
                    item.getTag(),
                    item.getName(),
                    item.getPrice(),
                    item.getDescription(),
                    item.getImageRes(),
                    item.getRating(),
                    item.getReviewCount()
            );
            product.setImageUrl(item.getImageUrl());
            Navigator.openProductDetails(this, product);
        });
        itemsRecycler.setLayoutManager(new LinearLayoutManager(this));
        itemsRecycler.setAdapter(adapter);
    }

    private void setupHeader() {
        String safeOrderId = orderId == null ? "" : orderId;
        String shortOrderId = safeOrderId.length() > 8 ? safeOrderId.substring(0, 8) : safeOrderId;
        orderIdText.setText(getString(R.string.order_number_format, shortOrderId));
        orderStatusText.setText(getLocalizedStatus(status));
        orderTotalText.setText(totalPrice == null ? "$0.00" : totalPrice);
        itemsCountText.setText(getString(R.string.items_count, itemsCount));

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        orderDateText.setText(createdAt > 0 ? sdf.format(new Date(createdAt)) : getString(R.string.order_unknown_date));
    }

    private void setupClicks() {
        backBtn.setOnClickListener(v -> finish());
    }

    private void loadOrderItems() {
        emptyText.setVisibility(View.VISIBLE);
        emptyText.setText(R.string.order_loading_items);
        itemsRecycler.setVisibility(View.GONE);

        orderSyncRepository.loadOrderItems(orderId, new OrderSyncRepository.LoadOrderItemsCallback() {
            @Override
            public void onLoaded(List<Map<String, Object>> items) {
                orderItems.clear();

                for (Map<String, Object> map : items) {
                    Product snapshotProduct = Product.fromMap(map);
                    int productId = snapshotProduct != null ? snapshotProduct.getId() : getInt(firstNonNull(map.get("productId"), map.get("id")));
                    String category = snapshotProduct != null ? snapshotProduct.getCategory() : getString(map.get("category"));
                    String tag = snapshotProduct != null ? snapshotProduct.getTag() : getString(map.get("tag"));
                    String name = snapshotProduct != null ? snapshotProduct.getName() : getString(map.get("name"));
                    String price = snapshotProduct != null ? snapshotProduct.getPrice() : getString(map.get("price"));
                    String description = snapshotProduct != null ? snapshotProduct.getDescription() : getString(map.get("description"));
                    int imageRes = snapshotProduct != null ? snapshotProduct.getPreferredLocalImageRes() : getInt(map.get("image_res"));
                    String imageUrl = snapshotProduct != null ? snapshotProduct.getImageUrl() : getString(firstNonNull(map.get("imageUrl"), map.get("image_url")));
                    double rating = snapshotProduct != null ? snapshotProduct.getRating() : getDouble(map.get("rating"));
                    int reviewCount = snapshotProduct != null ? snapshotProduct.getReviewCount() : getInt(firstNonNull(map.get("reviewCount"), map.get("review_count")));
                    int quantity = getInt(map.get("quantity"));

                    orderItems.add(new OrderItem(
                            productId,
                            category,
                            tag,
                            name,
                            price,
                            description,
                            imageRes,
                            imageUrl,
                            rating,
                            reviewCount,
                            quantity
                    ));
                }

                adapter.notifyDataSetChanged();
                enrichOrderItemsWithProductData();
                updateItemsUI();
            }

            @Override
            public void onError(String message) {
                emptyText.setVisibility(View.VISIBLE);
                itemsRecycler.setVisibility(View.GONE);
                emptyText.setText(message == null || message.trim().isEmpty() ? getString(R.string.order_items_empty) : message);
                Toast.makeText(OrderDetailsActivity.this, emptyText.getText(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateItemsUI() {
        if (orderItems.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            emptyText.setText(R.string.order_items_empty);
            itemsRecycler.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            itemsRecycler.setVisibility(View.VISIBLE);
        }
    }

    private String getString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private int getInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    private double getDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }

    private void enrichOrderItemsWithProductData() {
        for (int i = 0; i < orderItems.size(); i++) {
            OrderItem item = orderItems.get(i);
            final int index = i;

            Product cachedProduct = productRepository.getCachedProduct(item.getProductId());
            if (cachedProduct == null) {
                cachedProduct = productCacheRepository.getCachedProductById(item.getProductId());
            }
            if (cachedProduct != null) {
                applyProductImageData(item, cachedProduct);
                adapter.notifyItemChanged(index);
            }

            productRepository.loadProductById(item.getProductId(), new ProductRepository.LoadProductCallback() {
                @Override
                public void onLoaded(Product product) {
                    if (product == null || index >= orderItems.size()) {
                        return;
                    }

                    OrderItem currentItem = orderItems.get(index);
                    if (currentItem.getProductId() != product.getId()) {
                        return;
                    }

                    applyProductImageData(currentItem, product);
                    adapter.notifyItemChanged(index);
                }

                @Override
                public void onError(String message) {
                    // Keep best available snapshot data.
                }
            });
        }
    }

    private void applyProductImageData(OrderItem target, Product source) {
        if (target == null || source == null) {
            return;
        }

        target.setImageData(source.getPreferredLocalImageRes(), source.getImageUrl());
    }

    private String getLocalizedStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.trim().isEmpty()) {
            return getString(R.string.order_status_placed);
        }

        String normalizedStatus = rawStatus.trim().toLowerCase(Locale.ROOT);
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
}
