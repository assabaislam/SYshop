package com.example.SYshop.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.FrameLayout;

import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.Category;
import com.example.SYshop.adapters.CategoryAdapter;
import com.example.SYshop.models.Product;
import com.example.SYshop.adapters.ProductAdapter;
import com.example.SYshop.adapters.PromoAdapter;
import com.example.SYshop.R;
import com.example.SYshop.utils.Navigator;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView categoryRecycler, productRecycler, promoRecycler;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private PromoAdapter promoAdapter;

    private List<Category> categoryList;
    private List<Product> productList;
    private List<Product> promoList;

    private LinearLayout headerLayout;
    private NestedScrollView mainScrollView;
    private View searchContainer;
    private View cartContainer;
    private boolean doubleBackToExitPressedOnce = false;
    private TextView welcomeText, logoText;
    private ImageView searchIconSmall;
    private ImageView cartBtn;
    private Button shopNowBtn;
    private TextView seeAllText;
    private TextView promoSeeAllText;
    private TextView cartBadgeText;
    private TextView noProductsText;
    private TextView productsSectionTitle;
    private TextView offerSmallText;
    private TextView offerTitleText;
    private TextView offerPriceText;
    private ImageView offerImage;
    private EditText searchEdit;
    private ImageView clearSearchBtn;
    private BottomNavigationView bottomNavigation;

    private Product featuredOfferProduct;

    private final int maxHeaderHeight = 150;
    private final int minHeaderHeight = 92;
    private boolean isSearchCollapsed = false;
    private int currentHeaderHeight = 150;

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        refreshFavoriteIcons();
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }
    }
    private void handleExit() {
        if (doubleBackToExitPressedOnce) {
            finish();
            return;
        }

        doubleBackToExitPressedOnce = true;
        android.widget.Toast.makeText(this, R.string.press_again_to_exit, android.widget.Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleExit();
            }
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        initViews();
        setupCategories();
        setupProducts();
        setupFeaturedOffer();
        setupPromoProducts();
        setupSearch();
        setupClicks();
        setupScrollEffect();
        updateFloatingSearchPosition(maxHeaderHeight);
        updateCartBadge();
        updateNoProductsState();
    }

    private void initViews() {
        categoryRecycler = findViewById(R.id.categoryRecycler);
        productRecycler = findViewById(R.id.productRecycler);
        promoRecycler = findViewById(R.id.promoRecycler);

        headerLayout = findViewById(R.id.headerLayout);
        mainScrollView = findViewById(R.id.mainScrollView);
        searchContainer = findViewById(R.id.searchContainer);
        cartContainer = findViewById(R.id.cartContainer);

        welcomeText = findViewById(R.id.welcomeText);
        logoText = findViewById(R.id.logoText);
        searchIconSmall = findViewById(R.id.searchIconSmall);

        cartBtn = findViewById(R.id.cartBtn);
        shopNowBtn = findViewById(R.id.shopNowBtn);
        seeAllText = findViewById(R.id.seeAllText);
        promoSeeAllText = findViewById(R.id.promoSeeAllText);
        cartBadgeText = findViewById(R.id.cartBadgeText);
        noProductsText = findViewById(R.id.noProductsText);
        productsSectionTitle = findViewById(R.id.productsSectionTitle);
        searchEdit = findViewById(R.id.searchEdit);
        clearSearchBtn = findViewById(R.id.clearSearchBtn);

        offerSmallText = findViewById(R.id.offerSmallText);
        offerTitleText = findViewById(R.id.offerTitleText);
        offerPriceText = findViewById(R.id.offerPriceText);
        offerImage = findViewById(R.id.offerImage);

        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupCategories() {
        categoryList = new ArrayList<>();

        categoryList.add(new Category("All", true));
        categoryList.add(new Category("Shoes", false));
        categoryList.add(new Category("Watches", false));
        categoryList.add(new Category("Bags", false));
        categoryList.add(new Category("Audio", false));

        categoryAdapter = new CategoryAdapter(this, categoryList, categoryName -> {
            if (productAdapter != null) {
                productAdapter.setCategory(categoryName);

                if (categoryName.equalsIgnoreCase("All")) {
                    productAdapter.setTag("All");
                    productsSectionTitle.setText(R.string.popular_products);
                }

                updateNoProductsState();
            }
        });

        categoryRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        categoryRecycler.setAdapter(categoryAdapter);
    }

    private void setupProducts() {
        productList = new ArrayList<>();

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

        productAdapter = new ProductAdapter(
                this,
                productList,
                this::updateCartBadge,
                this::refreshFavoriteIcons
        );
        productRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        productRecycler.setHasFixedSize(true);
        productRecycler.setItemViewCacheSize(20);
        productRecycler.setAdapter(productAdapter);
    }

    private void setupFeaturedOffer() {
        featuredOfferProduct = null;

        for (Product product : productList) {
            if (product.hasOffer()) {
                featuredOfferProduct = product;
                break;
            }
        }

        if (featuredOfferProduct != null) {
            offerSmallText.setText(R.string.special_offer);
            offerTitleText.setText(featuredOfferProduct.getDiscountPercent() + "% off");
            offerPriceText.setText(featuredOfferProduct.getPrice() + " instead of " + featuredOfferProduct.getOldPrice());
            offerImage.setImageResource(featuredOfferProduct.getImageRes());
        }
    }

    private void setupPromoProducts() {
        promoList = new ArrayList<>();

        for (Product product : productList) {
            if (product.getTag().equalsIgnoreCase("Promo")) {
                promoList.add(product);
            }
        }

        promoAdapter = new PromoAdapter(
                this,
                promoList,
                this::updateCartBadge,
                this::refreshFavoriteIcons
        );
        promoRecycler.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        promoRecycler.setHasFixedSize(true);
        promoRecycler.setItemViewCacheSize(20);
        promoRecycler.setAdapter(promoAdapter);
    }

    private void setupSearch() {
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productAdapter != null) {
                    productAdapter.setSearchQuery(s.toString());
                    updateNoProductsState();
                }

                if (s.length() > 0) {
                    clearSearchBtn.setVisibility(View.VISIBLE);
                } else {
                    clearSearchBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        clearSearchBtn.setOnClickListener(v -> {
            searchEdit.setText("");
        });
    }

    private void setupClicks() {
        cartBtn.setOnClickListener(v -> {
            startActivity(new android.content.Intent(MainActivity.this, CartActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        searchIconSmall.setOnClickListener(v -> {
            if (mainScrollView != null) {
                mainScrollView.smoothScrollTo(0, 0);
            }
            showExpandedSearch();
        });

        shopNowBtn.setOnClickListener(v -> {
            if (featuredOfferProduct != null) {
                Navigator.openProductDetails(MainActivity.this, featuredOfferProduct);

            }
        });

        seeAllText.setOnClickListener(v -> {
            if (productAdapter != null) {
                productAdapter.setCategory("All");
                productAdapter.setTag("All");
                searchEdit.setText("");
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
                searchEdit.setText("");
                productsSectionTitle.setText(R.string.promo_products);
                updateNoProductsState();
                if (mainScrollView != null) {
                    mainScrollView.smoothScrollTo(0, productRecycler.getTop());
                }
            }
        });

        bottomNavigation.setSelectedItemId(R.id.nav_home);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_orders) {
                startActivity(new android.content.Intent(MainActivity.this, OrdersActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_favorites) {
                startActivity(new android.content.Intent(MainActivity.this, FavoritesActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new android.content.Intent(MainActivity.this, ProfileActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                return true;
            }

            return false;
        });
    }
    private void updateFloatingSearchPosition(int currentHeaderHeightDp) {
        FrameLayout.LayoutParams iconParams =
                (FrameLayout.LayoutParams) searchIconSmall.getLayoutParams();

        int iconSizeDp = 44;
        int halfIconDp = iconSizeDp / 2;

        iconParams.topMargin = dpToPx(currentHeaderHeightDp - halfIconDp);
        searchIconSmall.setLayoutParams(iconParams);
    }
    private void refreshFavoriteIcons() {
        if (productAdapter != null) {
            productAdapter.notifyDataSetChanged();
        }

        if (promoAdapter != null) {
            promoAdapter.notifyDataSetChanged();
        }
    }
    private void setupScrollEffect() {
        if (mainScrollView == null) return;

        mainScrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener)
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {

                    int newHeight = maxHeaderHeight - (scrollY / 4);

                    if (newHeight < minHeaderHeight) newHeight = minHeaderHeight;
                    if (newHeight > maxHeaderHeight) newHeight = maxHeaderHeight;

                    if (newHeight != currentHeaderHeight) {
                        currentHeaderHeight = newHeight;

                        LinearLayout.LayoutParams params =
                                (LinearLayout.LayoutParams) headerLayout.getLayoutParams();
                        params.height = dpToPx(newHeight);
                        headerLayout.setLayoutParams(params);

                        updateFloatingSearchPosition(newHeight);
                    }

                    float progress = Math.min(1f, scrollY / 150f);

                    float logoSize = 27f - (4f * progress);
                    float welcomeSize = 15f - (2f * progress);

                    logoText.setTextSize(TypedValue.COMPLEX_UNIT_SP, logoSize);
                    welcomeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, welcomeSize);

                    cartContainer.setTranslationY(-2f * progress);

                    if (scrollY > 80 && !isSearchCollapsed) {
                        isSearchCollapsed = true;
                        showCollapsedSearchIcon();
                    } else if (scrollY <= 80 && isSearchCollapsed) {
                        isSearchCollapsed = false;
                        showExpandedSearch();
                    }
                });
    }

    private void showCollapsedSearchIcon() {
        searchContainer.animate().cancel();
        searchIconSmall.animate().cancel();

        if (searchContainer.getVisibility() == View.VISIBLE) {
            searchContainer.animate()
                    .alpha(0f)
                    .translationY(-12f)
                    .setDuration(120)
                    .withEndAction(() -> searchContainer.setVisibility(View.GONE))
                    .start();
        }

        if (searchIconSmall.getVisibility() != View.VISIBLE) {
            searchIconSmall.setVisibility(View.VISIBLE);
            searchIconSmall.setAlpha(0f);
            searchIconSmall.setScaleX(0.85f);
            searchIconSmall.setScaleY(0.85f);

            searchIconSmall.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120)
                    .start();
        }
    }

    private void showExpandedSearch() {
        searchContainer.animate().cancel();
        searchIconSmall.animate().cancel();

        if (searchContainer.getVisibility() != View.VISIBLE) {
            searchContainer.setVisibility(View.VISIBLE);
            searchContainer.setAlpha(0f);
            searchContainer.setTranslationY(-12f);

            searchContainer.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(120)
                    .start();
        }

        if (searchIconSmall.getVisibility() == View.VISIBLE) {
            searchIconSmall.animate()
                    .alpha(0f)
                    .scaleX(0.85f)
                    .scaleY(0.85f)
                    .setDuration(100)
                    .withEndAction(() -> searchIconSmall.setVisibility(View.GONE))
                    .start();
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
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
            boolean wasHidden = cartBadgeText.getVisibility() != android.view.View.VISIBLE;
            cartBadgeText.setVisibility(android.view.View.VISIBLE);
            cartBadgeText.setText(String.valueOf(cartCount));
            if (wasHidden) animateCartBadge();
        } else {
            cartBadgeText.setVisibility(android.view.View.GONE);
        }
    }

    private void updateNoProductsState() {
        if (productAdapter != null && productAdapter.isEmpty()) {
            noProductsText.setVisibility(android.view.View.VISIBLE);
            productRecycler.setVisibility(android.view.View.GONE);
            seeAllText.setVisibility(android.view.View.GONE);
        } else {
            noProductsText.setVisibility(android.view.View.GONE);
            productRecycler.setVisibility(android.view.View.VISIBLE);
            seeAllText.setVisibility(android.view.View.VISIBLE);
        }
    }
}