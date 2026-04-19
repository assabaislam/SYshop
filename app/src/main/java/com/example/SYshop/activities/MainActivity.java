package com.example.SYshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.adapters.CategoryAdapter;
import com.example.SYshop.adapters.OfferSliderAdapter;
import com.example.SYshop.adapters.ProductAdapter;
import com.example.SYshop.adapters.PromoAdapter;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.Category;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.AuthManager;
import com.example.SYshop.utils.Navigator;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity {

    private RecyclerView offerRecycler, categoryRecycler, productRecycler, promoRecycler;
    private CategoryAdapter categoryAdapter;
    private OfferSliderAdapter offerSliderAdapter;
    private ProductAdapter productAdapter;
    private PromoAdapter promoAdapter;

    private List<Category> categoryList;
    private List<Product> productList;
    private List<Product> promoList;
    private final List<Product> offerBannerList = new ArrayList<>();

    private LinearLayout headerLayout;
    private LinearLayout offerIndicatorLayout;
    private NestedScrollView mainScrollView;
    private View cartContainer;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView welcomeText, logoText;
    private ImageView searchIconSmall;
    private ImageView cartBtn;
    private TextView seeAllText;
    private TextView promoSeeAllText;
    private TextView categorySeeAllText;
    private TextView cartBadgeText;
    private TextView noProductsText;
    private TextView productsSectionTitle;
    private BottomNavigationView bottomNavigation;

    private ProductCacheRepository productCacheRepository;
    private ProductRepository productRepository;
    private final Handler offerAutoSlideHandler = new Handler(Looper.getMainLooper());
    private Runnable offerAutoSlideRunnable;
    private PagerSnapHelper offerSnapHelper;
    private int currentOfferPosition = 0;

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
        refreshFavoriteIcons();
        if (ProductRepository.consumeDirtyFlag()) {
            loadProductsFromFirebase();
        }
        startOfferAutoSlide();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }

    @Override
    protected void onPause() {
        stopOfferAutoSlide();
        super.onPause();
    }

    private void handleExit() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        productCacheRepository = new ProductCacheRepository(this);
        productRepository = new ProductRepository();

        initViews();
        setupCategories();
        setupOfferSlider();
        setupProductsRecycler();
        setupClicks();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
        updateNoProductsState();

        loadProductsFromFirebase();
    }

    private void initViews() {
        offerRecycler = findViewById(R.id.offerRecycler);
        categoryRecycler = findViewById(R.id.categoryRecycler);
        productRecycler = findViewById(R.id.productRecycler);
        promoRecycler = findViewById(R.id.promoRecycler);

        headerLayout = findViewById(R.id.headerLayout);
        offerIndicatorLayout = findViewById(R.id.offerIndicatorLayout);
        mainScrollView = findViewById(R.id.mainScrollView);
        cartContainer = findViewById(R.id.cartContainer);

        welcomeText = findViewById(R.id.welcomeText);
        logoText = findViewById(R.id.logoText);
        searchIconSmall = findViewById(R.id.searchIconSmall);

        cartBtn = findViewById(R.id.cartBtn);
        seeAllText = findViewById(R.id.seeAllText);
        promoSeeAllText = findViewById(R.id.promoSeeAllText);
        categorySeeAllText = findViewById(R.id.categorySeeAllText);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        noProductsText = findViewById(R.id.noProductsText);
        productsSectionTitle = findViewById(R.id.productsSectionTitle);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupCategories() {
        categoryList = new ArrayList<>();

        categoryList.add(new Category("Shoes", false, R.drawable.sneakers_urban));
        categoryList.add(new Category("Watches", false, R.drawable.classic_watch1));
        categoryList.add(new Category("Bags", false, R.drawable.leather_bag));
        categoryList.add(new Category("Audio", false, R.drawable.wireless_headset));

        categoryAdapter = new CategoryAdapter(this, categoryList, categoryName -> {
            if (productAdapter != null) {
                productAdapter.setCategory(categoryName);
                productAdapter.setTag("All");
                productAdapter.setSearchQuery("");
                productsSectionTitle.setText(R.string.popular_products);
                updateNoProductsState();
            }
        });

        categoryRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupOfferSlider() {
        offerSliderAdapter = new OfferSliderAdapter(this, offerBannerList,
                product -> Navigator.openProductDetails(MainActivity.this, product));

        LinearLayoutManager offerLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        offerRecycler.setLayoutManager(offerLayoutManager);
        offerRecycler.setAdapter(offerSliderAdapter);
        offerRecycler.setItemAnimator(null);

        offerSnapHelper = new PagerSnapHelper();
        offerSnapHelper.attachToRecyclerView(offerRecycler);

        offerRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    stopOfferAutoSlide();
                } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    updateOfferCurrentPosition();
                    startOfferAutoSlide();
                }
            }
        });
    }

    private void setupProductsRecycler() {
        productList = new ArrayList<>();
        promoList = new ArrayList<>();

        productAdapter = new ProductAdapter(
                this,
                productList,
                this::updateCartBadge,
                this::refreshFavoriteIcons
        );

        promoAdapter = new PromoAdapter(
                this,
                promoList,
                this::updateCartBadge,
                this::refreshFavoriteIcons
        );

        productRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        productRecycler.setHasFixedSize(true);
        productRecycler.setItemViewCacheSize(20);
        productRecycler.setItemAnimator(new DefaultItemAnimator());
        productRecycler.setAdapter(productAdapter);

        LinearLayoutManager promoLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        promoLayoutManager.setReverseLayout(false);
        promoLayoutManager.setStackFromEnd(false);
        promoRecycler.setLayoutManager(promoLayoutManager);
        promoRecycler.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        promoRecycler.setHasFixedSize(true);
        promoRecycler.setItemViewCacheSize(20);
        promoRecycler.setAdapter(promoAdapter);
    }

    private void loadProductsFromFirebase() {
        List<Product> cachedProducts = productRepository.getCachedProducts();
        if (!cachedProducts.isEmpty() && productList.isEmpty()) {
            onProductsReady(cachedProducts);
        }

        if (productList.isEmpty()) {
            List<Product> localCachedProducts = productCacheRepository.getCachedProducts();
            if (!localCachedProducts.isEmpty()) {
                onProductsReady(localCachedProducts);
            }
        }

        productRepository.loadProducts(new ProductRepository.LoadProductsCallback() {
            @Override
            public void onLoaded(List<Product> products) {
                if (products == null || products.isEmpty()) {
                    loadFallbackProducts();
                    onProductsReady(new ArrayList<>(productList));
                    return;
                }

                onProductsReady(products);
            }

            @Override
            public void onError(String message) {
                if (!productList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Showing latest available products", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadFallbackProducts();
                onProductsReady(new ArrayList<>(productList));
                Toast.makeText(MainActivity.this, "Loaded fallback products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadFallbackProducts() {
        productList.clear();

        productList.add(new Product(
                1,
                "Shoes",
                "Best seller",
                "Sneakers Urban X",
                "$89",
                "Comfortable everyday sneakers with a modern urban design. Great for walking, casual outfits, and daily use.",
                Arrays.asList(R.drawable.sneakers_urban, R.drawable.running_shoes)
        ));

        productList.add(new Product(
                2,
                "Watches",
                "New",
                "Smart Watch Pro",
                "$149",
                "A stylish smartwatch with fitness tracking, notifications, and long battery life.",
                R.drawable.smart_watch_pro
        ));

        productList.add(new Product(
                3,
                "Bags",
                "Promo",
                "Leather Bag",
                "$99",
                "Elegant leather bag with spacious storage, durable material, and premium finish.",
                R.drawable.leather_bag
        ));

        productList.add(new Product(
                4,
                "Audio",
                "Top rated",
                "Wireless Headset",
                "$129",
                "High-quality wireless headset with clear sound, soft ear cushions, and noise isolation.",
                R.drawable.wireless_headset
        ));

        productList.add(new Product(
                5,
                "Audio",
                "Promo",
                "Mini Speaker",
                "$79",
                "Portable speaker with strong bass and compact design.",
                R.drawable.mini_speaker
        ));

        productList.add(new Product(
                6,
                "Shoes",
                "Promo",
                "Running Shoes",
                "$109",
                "Lightweight running shoes with extra comfort for sport and daily wear.",
                R.drawable.running_shoes
        ));

        productList.add(new Product(
                7,
                "Watches",
                "Promo",
                "Classic Watch",
                "$119",
                "Elegant classic watch with premium metal finish.",
                Arrays.asList(R.drawable.classic_watch1, R.drawable.classic_watch2, R.drawable.classic_watch3),
                4.1,
                15
        ));

        productList.add(new Product(
                8,
                "Bags",
                "New",
                "Hand made bag",
                "$50",
                "A small handmade handbag with a unique woven design, combining elegance and craftsmanship. Perfect for carrying essentials with a stylish touch.",
                R.drawable.hayat_sac,
                true,
                30,
                "$70",
                5,
                67
        ));
    }

    private void onProductsReady(List<Product> products) {
        productList.clear();
        productList.addAll(products);
        productAdapter.submitProducts(productList);

        setupOfferBanners();
        setupPromoProducts();
        updateNoProductsState();
        productCacheRepository.replaceAllProducts(productList);
    }

    private void setupOfferBanners() {
        offerBannerList.clear();
        for (Product product : productList) {
            if (product.hasOffer() || product.getTag().equalsIgnoreCase("Promo")) {
                offerBannerList.add(product);
            }
        }

        if (offerBannerList.isEmpty()) {
            int fallbackCount = Math.min(productList.size(), 4);
            for (int i = 0; i < fallbackCount; i++) {
                offerBannerList.add(productList.get(i));
            }
        }

        offerSliderAdapter.submitOffers(offerBannerList);
        currentOfferPosition = 0;
        offerRecycler.scrollToPosition(0);
        renderOfferIndicators(offerBannerList.size(), 0);
        startOfferAutoSlide();
    }

    private void setupPromoProducts() {
        promoList.clear();

        for (Product product : productList) {
            if (product.getTag().equalsIgnoreCase("Promo")) {
                promoList.add(product);
            }
        }

        promoAdapter.submitProducts(promoList);
        promoRecycler.post(() -> {
            if (promoRecycler.getLayoutManager() != null) {
                promoRecycler.getLayoutManager().scrollToPosition(0);
            }
        });
    }

    private void setupClicks() {
        cartBtn.setOnClickListener(v -> {
            if (!AuthManager.requireLogin(MainActivity.this)) {
                return;
            }
            startActivity(new Intent(MainActivity.this, CartActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        searchIconSmall.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        seeAllText.setOnClickListener(v -> {
            if (productAdapter != null) {
                productAdapter.setCategory("All");
                productAdapter.setTag("All");
                productAdapter.setSearchQuery("");
                productsSectionTitle.setText(R.string.popular_products);
                updateNoProductsState();
                if (mainScrollView != null) {
                    mainScrollView.smoothScrollTo(0, productRecycler.getTop());
                }
            }
        });

        promoSeeAllText.setOnClickListener(v -> {
            if (productAdapter != null) {
                productAdapter.setCategory("All");
                productAdapter.setTag("Promo");
                productAdapter.setSearchQuery("");
                productsSectionTitle.setText(R.string.promo_products);
                updateNoProductsState();
                if (mainScrollView != null) {
                    mainScrollView.smoothScrollTo(0, productRecycler.getTop());
                }
            }
        });

        categorySeeAllText.setOnClickListener(v -> {
            if (categoryAdapter != null) {
                categoryAdapter.clearSelection();
            }

            if (productAdapter != null) {
                productAdapter.setCategory("All");
                productAdapter.setTag("All");
                productAdapter.setSearchQuery("");
                productsSectionTitle.setText(R.string.popular_products);
                updateNoProductsState();
            }
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_orders) {
                if (!AuthManager.requireLogin(MainActivity.this)) {
                    return false;
                }
                startActivity(new Intent(MainActivity.this, OrdersActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_favorites) {
                if (!AuthManager.requireLogin(MainActivity.this)) {
                    return false;
                }
                startActivity(new Intent(MainActivity.this, FavoritesActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_profile) {
                if (!AuthManager.requireLogin(MainActivity.this)) {
                    return false;
                }
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }

            return false;
        });
    }

    private void refreshFavoriteIcons() {
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        if (promoAdapter != null) {
            promoAdapter.notifyDataSetChanged();
        }
    }

    private void animateCartBadge() {
        cartBadgeText.setScaleX(0.6f);
        cartBadgeText.setScaleY(0.6f);
        cartBadgeText.setAlpha(0.6f);

        cartBadgeText.animate()
                .scaleX(1.15f)
                .scaleY(1.15f)
                .alpha(1f)
                .setDuration(140)
                .withEndAction(() -> cartBadgeText.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void updateCartBadge() {
        int cartCount = CartManager.getCartCount();

        if (cartCount > 0) {
            boolean wasHidden = cartBadgeText.getVisibility() != View.VISIBLE;
            cartBadgeText.setVisibility(View.VISIBLE);
            cartBadgeText.setText(String.valueOf(cartCount));
            if (wasHidden) animateCartBadge();
        } else {
            cartBadgeText.setVisibility(View.GONE);
        }
    }

    private void updateNoProductsState() {
        if (productAdapter != null && productAdapter.isEmpty()) {
            noProductsText.setVisibility(View.VISIBLE);
            productRecycler.setVisibility(View.GONE);
            seeAllText.setVisibility(View.GONE);
        } else {
            noProductsText.setVisibility(View.GONE);
            productRecycler.setVisibility(View.VISIBLE);
            seeAllText.setVisibility(View.VISIBLE);
        }
    }

    private void renderOfferIndicators(int count, int selectedPosition) {
        if (offerIndicatorLayout == null) {
            return;
        }

        offerIndicatorLayout.removeAllViews();

        if (count <= 1) {
            offerIndicatorLayout.setVisibility(View.GONE);
            return;
        }

        offerIndicatorLayout.setVisibility(View.VISIBLE);

        for (int i = 0; i < count; i++) {
            View indicator = new View(this);
            int width = i == selectedPosition ? dpToPx(20) : dpToPx(8);
            int height = dpToPx(8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMarginStart(dpToPx(4));
            params.setMarginEnd(dpToPx(4));
            indicator.setLayoutParams(params);
            indicator.setBackgroundResource(i == selectedPosition
                    ? R.drawable.bg_offer_indicator_active
                    : R.drawable.bg_offer_indicator_inactive);
            offerIndicatorLayout.addView(indicator);
        }
    }

    private void updateOfferCurrentPosition() {
        if (offerRecycler == null || offerRecycler.getLayoutManager() == null || offerSnapHelper == null) {
            return;
        }

        View snapView = offerSnapHelper.findSnapView(offerRecycler.getLayoutManager());
        if (snapView == null) {
            return;
        }

        int position = offerRecycler.getLayoutManager().getPosition(snapView);
        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        currentOfferPosition = position;
        renderOfferIndicators(offerBannerList.size(), currentOfferPosition);
    }

    private void startOfferAutoSlide() {
        stopOfferAutoSlide();

        if (offerBannerList.size() <= 1 || offerRecycler == null) {
            return;
        }

        offerAutoSlideRunnable = new Runnable() {
            @Override
            public void run() {
                if (offerBannerList.size() <= 1) {
                    return;
                }

                currentOfferPosition = (currentOfferPosition + 1) % offerBannerList.size();
                offerRecycler.smoothScrollToPosition(currentOfferPosition);
                renderOfferIndicators(offerBannerList.size(), currentOfferPosition);
                offerAutoSlideHandler.postDelayed(this, 4200);
            }
        };

        offerAutoSlideHandler.postDelayed(offerAutoSlideRunnable, 4200);
    }

    private void stopOfferAutoSlide() {
        if (offerAutoSlideRunnable != null) {
            offerAutoSlideHandler.removeCallbacks(offerAutoSlideRunnable);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}
