package com.example.SYshop.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.SYshop.R;
import com.example.SYshop.adapters.ProductImagePagerAdapter;
import com.example.SYshop.database.CartSyncRepository;
import com.example.SYshop.database.FavoriteCacheRepository;
import com.example.SYshop.database.FavoriteSyncRepository;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.AuthManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsActivity extends BaseActivity {

    private ViewPager2 productViewPager;
    private TextView imageIndicatorText;
    private ImageView backBtn, detailFavBtn, topCartBtn, shareBtn;
    private TextView topCartBadgeText;

    private TextView detailTag, detailName, detailPrice, detailDescription;
    private TextView quantityText;
    private MaterialButton addToCartBtn;
    private TextView minusBtn;
    private TextView plusBtn;

    private TextView ratingValue, ratingBigValue, reviewCount;
    private RatingBar detailRatingBar;
    private ProgressBar progress5Star, progress4Star, progress3Star, progress2Star, progress1Star;

    private int productId;
    private Product currentProduct;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    private FavoriteCacheRepository favoriteCacheRepository;
    private FavoriteSyncRepository favoriteSyncRepository;
    private CartSyncRepository cartSyncRepository;
    private ProductCacheRepository productCacheRepository;
    private ProductRepository productRepository;
    private int selectedQuantity = 1;

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        setupBackToHome();

        productRepository = new ProductRepository();
        productCacheRepository = new ProductCacheRepository(this);
        cartSyncRepository = new CartSyncRepository(this);
        favoriteCacheRepository = new FavoriteCacheRepository(this);
        favoriteSyncRepository = new FavoriteSyncRepository(this);

        initViews();
        updateCartBadge();
        refreshCartState(this::updateCartBadge);
        setupClicks();
        loadProduct();
    }

    private void initViews() {
        productViewPager = findViewById(R.id.productViewPager);
        imageIndicatorText = findViewById(R.id.imageIndicatorText);

        detailTag = findViewById(R.id.detailTag);
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);
        detailDescription = findViewById(R.id.detailDescription);

        addToCartBtn = findViewById(R.id.addToCartBtn);
        backBtn = findViewById(R.id.backBtn);
        detailFavBtn = findViewById(R.id.detailFavBtn);
        topCartBtn = findViewById(R.id.topCartBtn);
        shareBtn = findViewById(R.id.shareBtn);
        topCartBadgeText = findViewById(R.id.topCartBadgeText);
        quantityText = findViewById(R.id.quantityText);
        minusBtn = findViewById(R.id.minusBtn);
        plusBtn = findViewById(R.id.plusBtn);

        ratingValue = findViewById(R.id.ratingValue);
        ratingBigValue = findViewById(R.id.ratingBigValue);
        detailRatingBar = findViewById(R.id.detailRatingBar);
        reviewCount = findViewById(R.id.reviewCount);

        progress5Star = findViewById(R.id.progress5Star);
        progress4Star = findViewById(R.id.progress4Star);
        progress3Star = findViewById(R.id.progress3Star);
        progress2Star = findViewById(R.id.progress2Star);
        progress1Star = findViewById(R.id.progress1Star);
    }

    private void loadProduct() {
        productId = getIntent().getIntExtra("product_id", -1);

        if (productId <= 0) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setLoading(true);

        Product intentProduct = readProductFromIntent();
        if (intentProduct != null) {
            renderProduct(intentProduct);
        }

        Product cachedProduct = productRepository.getCachedProduct(productId);
        if (cachedProduct != null) {
            renderProduct(mergeProducts(currentProduct, cachedProduct));
        } else {
            Product localCachedProduct = productCacheRepository.getCachedProductById(productId);
            if (localCachedProduct != null) {
                renderProduct(mergeProducts(currentProduct, localCachedProduct));
            }
        }

        productRepository.loadProductById(productId, new ProductRepository.LoadProductCallback() {
            @Override
            public void onLoaded(Product product) {
                setLoading(false);
                renderProduct(mergeProducts(currentProduct, product));
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                if (currentProduct != null) {
                    Toast.makeText(ProductDetailsActivity.this, "Showing saved product details", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(
                        ProductDetailsActivity.this,
                        message == null || message.trim().isEmpty() ? "Unable to load product" : message,
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        });
    }

    private Product readProductFromIntent() {
        String name = getIntent().getStringExtra("product_name");
        String category = getIntent().getStringExtra("product_category");
        String tag = getIntent().getStringExtra("product_tag");
        String price = getIntent().getStringExtra("product_price");
        String description = getIntent().getStringExtra("product_description");
        String imageUrl = getIntent().getStringExtra("product_image_url");
        String imageName = getIntent().getStringExtra("product_image_name");
        int imageRes = getIntent().getIntExtra("product_image_res", 0);
        double rating = getIntent().getDoubleExtra("product_rating", 0.0);
        int reviewCount = getIntent().getIntExtra("product_review_count", 0);

        boolean hasUsefulSnapshot = !isBlank(name)
                || !isBlank(imageUrl)
                || !isBlank(imageName)
                || imageRes != 0;

        if (!hasUsefulSnapshot) {
            return null;
        }

        Product snapshot = new Product(
                productId,
                safeValue(category),
                safeValue(tag),
                safeValue(name),
                safeValue(price),
                safeValue(description),
                imageRes,
                rating > 0 ? rating : 4.5,
                reviewCount
        );
        snapshot.setImageName(safeValue(imageName));
        snapshot.setImageUrl(safeValue(imageUrl));
        return snapshot;
    }

    private Product mergeProducts(Product preferred, Product fallback) {
        if (preferred == null) {
            return fallback;
        }
        if (fallback == null) {
            return preferred;
        }

        Product merged = new Product(
                preferred.getId() > 0 ? preferred.getId() : fallback.getId(),
                !isBlank(preferred.getCategory()) ? preferred.getCategory() : fallback.getCategory(),
                !isBlank(preferred.getTag()) ? preferred.getTag() : fallback.getTag(),
                !isBlank(preferred.getName()) ? preferred.getName() : fallback.getName(),
                !isBlank(preferred.getPrice()) ? preferred.getPrice() : fallback.getPrice(),
                !isBlank(preferred.getDescription()) ? preferred.getDescription() : fallback.getDescription(),
                preferred.getPreferredLocalImageRes() != 0 ? preferred.getPreferredLocalImageRes() : fallback.getPreferredLocalImageRes(),
                preferred.getRating() > 0 ? preferred.getRating() : fallback.getRating(),
                preferred.getReviewCount() > 0 ? preferred.getReviewCount() : fallback.getReviewCount()
        );

        merged.setDocumentId(!isBlank(fallback.getDocumentId()) ? fallback.getDocumentId() : preferred.getDocumentId());
        merged.setImageName(!isBlank(preferred.getPreferredLocalImageName())
                ? preferred.getPreferredLocalImageName()
                : fallback.getPreferredLocalImageName());
        merged.setImageUrl(!isBlank(preferred.getImageUrl()) ? preferred.getImageUrl() : fallback.getImageUrl());
        merged.setHasOffer(preferred.hasOffer() || fallback.hasOffer());
        merged.setDiscountPercent(preferred.getDiscountPercent() > 0 ? preferred.getDiscountPercent() : fallback.getDiscountPercent());
        merged.setOldPrice(!isBlank(preferred.getOldPrice()) ? preferred.getOldPrice() : fallback.getOldPrice());
        merged.setImagesList(!preferred.getImagesList().isEmpty() ? preferred.getImagesList() : fallback.getImagesList());
        return merged;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeValue(String value) {
        return value == null ? "" : value;
    }

    private void renderProduct(Product product) {
        if (product == null) {
            return;
        }

        currentProduct = product;
        selectedQuantity = 1;

        detailTag.setText(product.getTag());
        detailName.setText(product.getName());
        detailPrice.setText(product.getPrice());
        detailDescription.setText(product.getDescription());

        String ratingText = String.format(java.util.Locale.US, "%.1f", product.getRating());
        ratingValue.setText(ratingText);
        ratingBigValue.setText(ratingText);
        detailRatingBar.setRating((float) product.getRating());
        reviewCount.setText("Based on " + product.getReviewCount() + " reviews");

        setupRatingBreakdown(product);
        setupViewPager(product);
        setupFavoriteState();
        updateQuantityUi();
        setLoading(false);
    }

    private void setupRatingBreakdown(Product product) {
        int totalReviews = Math.max(product.getReviewCount(), 1);
        int fiveStar = clamp((int) Math.round((product.getRating() / 5.0) * 100));
        int fourStar = clamp((int) Math.round((product.getRating() / 5.0) * 75));
        int threeStar = clamp((int) Math.round((product.getRating() / 5.0) * 45));
        int twoStar = clamp((int) Math.round((product.getRating() / 5.0) * 20));
        int oneStar = clamp((int) Math.round(100.0 / totalReviews));

        progress5Star.setProgress(fiveStar);
        progress4Star.setProgress(fourStar);
        progress3Star.setProgress(threeStar);
        progress2Star.setProgress(twoStar);
        progress1Star.setProgress(oneStar);
    }

    private void setupViewPager(Product product) {
        List<Integer> imagesList = getDisplayImages(product);

        ProductImagePagerAdapter adapter = new ProductImagePagerAdapter(
                this,
                imagesList,
                product.getImageUrl(),
                position -> openImageViewer(imagesList, product.getImageUrl(), position)
        );

        productViewPager.setAdapter(adapter);
        updateIndicator(0);

        if (pageChangeCallback == null) {
            pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    updateIndicator(position);
                }
            };
            productViewPager.registerOnPageChangeCallback(pageChangeCallback);
        }
    }

    private void updateIndicator(int position) {
        int total = productViewPager.getAdapter() == null ? 0 : productViewPager.getAdapter().getItemCount();
        String text = (position + 1) + " of " + Math.max(total, 1);
        imageIndicatorText.setText(text);
    }

    private void setupFavoriteState() {
        updateFavoriteButtonIcon(FavoriteManager.isFavoriteById(productId) || favoriteCacheRepository.isFavoriteCached(productId));
    }

    private void setupClicks() {
        addToCartBtn.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product is still loading", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!AuthManager.requireLogin(this)) {
                return;
            }

            CartManager.addToCart(currentProduct, selectedQuantity);
            cartSyncRepository.addOrIncreaseCartItem(currentProduct, selectedQuantity, null);
            updateCartBadge();

            Toast.makeText(this, selectedQuantity + " x " + currentProduct.getName() + " added to cart", Toast.LENGTH_SHORT).show();

            addToCartBtn.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> addToCartBtn.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start())
                    .start();
        });

        minusBtn.setOnClickListener(v -> {
            if (selectedQuantity > 1) {
                selectedQuantity--;
                updateQuantityUi();
            }
        });

        plusBtn.setOnClickListener(v -> {
            selectedQuantity++;
            updateQuantityUi();
        });

        detailFavBtn.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(this, "Product is still loading", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!AuthManager.requireLogin(this)) {
                return;
            }

            boolean isNowFavorite = FavoriteManager.toggleFavorite(currentProduct);

            if (isNowFavorite) {
                favoriteCacheRepository.saveFavorite(currentProduct);
                favoriteSyncRepository.addFavorite(currentProduct, null);
                updateFavoriteButtonIcon(true);
                Toast.makeText(this, currentProduct.getName() + " added to favorites", Toast.LENGTH_SHORT).show();
            } else {
                favoriteCacheRepository.removeFavorite(currentProduct.getId());
                favoriteSyncRepository.removeFavorite(currentProduct, null);
                updateFavoriteButtonIcon(false);
                Toast.makeText(this, currentProduct.getName() + " removed from favorites", Toast.LENGTH_SHORT).show();
            }

            animateHeart(detailFavBtn);
        });

        backBtn.setOnClickListener(v -> finish());

        topCartBtn.setOnClickListener(v -> {
            if (!AuthManager.requireLogin(this)) {
                return;
            }
            startActivity(new Intent(this, CartActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        shareBtn.setOnClickListener(v -> shareCurrentProduct());
    }

    private List<Integer> getDisplayImages(Product product) {
        List<Integer> imagesList = new ArrayList<>(product.getImagesList());
        if (imagesList.isEmpty()) {
            imagesList.add(product.getPreferredLocalImageRes());
        }
        return imagesList;
    }

    private void openImageViewer(List<Integer> imagesList, String imageUrl, int position) {
        Intent intent = new Intent(this, ProductImageViewerActivity.class);
        intent.putIntegerArrayListExtra(ProductImageViewerActivity.EXTRA_IMAGE_RES_LIST, new ArrayList<>(imagesList));
        intent.putExtra(ProductImageViewerActivity.EXTRA_IMAGE_URL, imageUrl);
        intent.putExtra(ProductImageViewerActivity.EXTRA_START_POSITION, Math.max(position, 0));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void updateFavoriteButtonIcon(boolean isFavorite) {
        if (isFavorite) {
            detailFavBtn.setImageResource(R.drawable.ic_favorite_filled);
            detailFavBtn.clearColorFilter();
        } else {
            detailFavBtn.setImageResource(R.drawable.ic_favorite_not_fill);
            detailFavBtn.setColorFilter(ContextCompat.getColor(this, R.color.black_soft));
        }
    }

    private void updateQuantityUi() {
        quantityText.setText(String.valueOf(selectedQuantity));
        minusBtn.setAlpha(selectedQuantity > 1 ? 1f : 0.45f);
    }

    private void updateCartBadge() {
        int cartCount = CartManager.getCartCount();
        applyCartBadgeCount(cartCount);
    }

    private void applyCartBadgeCount(int cartCount) {
        if (cartCount > 0) {
            topCartBadgeText.setVisibility(View.VISIBLE);
            topCartBadgeText.setText(cartCount > 99 ? "99+" : String.valueOf(cartCount));
            topCartBadgeText.bringToFront();
        } else {
            topCartBadgeText.setVisibility(View.GONE);
        }
    }

    private void animateHeart(ImageView imageView) {
        imageView.animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(120)
                .withEndAction(() -> imageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    private void setLoading(boolean isLoading) {
        addToCartBtn.setEnabled(!isLoading && currentProduct != null);
        detailFavBtn.setEnabled(!isLoading && currentProduct != null);
        shareBtn.setEnabled(!isLoading && currentProduct != null);
        topCartBtn.setEnabled(true);
        minusBtn.setEnabled(!isLoading && currentProduct != null);
        plusBtn.setEnabled(!isLoading && currentProduct != null);
        backBtn.setEnabled(true);
        float contentAlpha = isLoading && currentProduct == null ? 0.55f : 1f;
        detailName.setAlpha(contentAlpha);
        detailDescription.setAlpha(contentAlpha);
        detailPrice.setAlpha(contentAlpha);
    }

    private void shareCurrentProduct() {
        if (currentProduct == null) {
            Toast.makeText(this, "Product is still loading", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.product_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, buildProductShareMessage(currentProduct));

        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.product_share_chooser)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, getString(R.string.product_share_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildProductShareMessage(Product product) {
        String tag = product.getTag() == null ? "" : product.getTag().trim();
        String description = product.getDescription() == null ? "" : product.getDescription().trim();
        if (description.length() > 120) {
            description = description.substring(0, 120).trim() + "...";
        }

        if (!tag.isEmpty()) {
            return getString(
                    R.string.product_share_message_with_tag,
                    product.getName(),
                    product.getPrice(),
                    tag,
                    description
            );
        }

        return getString(
                R.string.product_share_message,
                product.getName(),
                product.getPrice(),
                description
        );
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
