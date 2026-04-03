package com.example.SYshop.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.ViewPager2;

import com.example.SYshop.adapters.ProductImagePagerAdapter;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.managers.FavoriteManager;
import com.example.SYshop.models.Product;
import com.example.SYshop.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailsActivity extends BaseActivity {

    private ViewPager2 productViewPager;
    private TextView imageIndicatorText;
    private ImageView backBtn, detailFavBtn;

    private TextView detailTag, detailName, detailPrice, detailDescription;
    private TextView bottomPrice;
    private MaterialButton addToCartBtn;

    // ⭐ Rating UI
    private TextView ratingValue, ratingBigValue, reviewCount;
    private RatingBar detailRatingBar;
    private ProgressBar progress5Star, progress4Star, progress3Star, progress2Star, progress1Star;

    // Data
    private String category, tag, name, price, description;
    private int image;
    private int productId;
    private List<Integer> imagesList;

    // ⭐ Rating data
    private double rating;
    private int reviewsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        setupBackToHome();
        initViews();
        loadData();
        setupFavoriteState();
        setupClicks();
        setupViewPager();
    }

    private void initViews() {
        productViewPager = findViewById(R.id.productViewPager);
        imageIndicatorText = findViewById(R.id.imageIndicatorText);

        detailTag = findViewById(R.id.detailTag);
        detailName = findViewById(R.id.detailName);
        detailPrice = findViewById(R.id.detailPrice);
        detailDescription = findViewById(R.id.detailDescription);
        bottomPrice = findViewById(R.id.bottomPrice);

        addToCartBtn = findViewById(R.id.addToCartBtn);
        backBtn = findViewById(R.id.backBtn);
        detailFavBtn = findViewById(R.id.detailFavBtn);

        // ⭐ Rating views
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

    private void loadData() {
        productId = getIntent().getIntExtra("product_id", -1);
        category = getIntent().getStringExtra("category");
        tag = getIntent().getStringExtra("tag");
        name = getIntent().getStringExtra("name");
        price = getIntent().getStringExtra("price");
        description = getIntent().getStringExtra("description");
        image = getIntent().getIntExtra("image", 0);
        imagesList = getIntent().getIntegerArrayListExtra("images_list");

        // ⭐ Rating data (safe defaults)
        rating = getIntent().getDoubleExtra("rating", 4.5);
        reviewsCount = getIntent().getIntExtra("reviews_count", 128);

        if (imagesList == null) {
            imagesList = new ArrayList<>();
            if (image != 0) imagesList.add(image);
        }

        // Product UI
        detailTag.setText(tag);
        detailName.setText(name);
        detailPrice.setText(price);
        detailDescription.setText(description);
        bottomPrice.setText(price);

        // ⭐ Rating UI
        String ratingText = String.valueOf(rating);
        ratingValue.setText(ratingText);
        ratingBigValue.setText(ratingText);
        detailRatingBar.setRating((float) rating);
        reviewCount.setText("Based on " + reviewsCount + " reviews");

        setupRatingBreakdown();
    }

    private void setupRatingBreakdown() {
        // Fake distribution (you can make it dynamic later)
        progress5Star.setProgress(70);
        progress4Star.setProgress(20);
        progress3Star.setProgress(7);
        progress2Star.setProgress(2);
        progress1Star.setProgress(1);
    }

    private void setupViewPager() {
        ProductImagePagerAdapter adapter =
                new ProductImagePagerAdapter(this, imagesList);

        productViewPager.setAdapter(adapter);
        updateIndicator(0);

        productViewPager.registerOnPageChangeCallback(
                new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        updateIndicator(position);
                    }
                });
    }

    private void updateIndicator(int position) {
        String text = (position + 1) + " of " + imagesList.size();
        imageIndicatorText.setText(text);
    }

    private Product getCurrentProduct() {
        return new Product(
                productId,
                category,
                tag,
                name,
                price,
                description,
                imagesList,
                rating,
                reviewsCount
        );
    }

    private void setupFavoriteState() {
        if (FavoriteManager.isFavoriteById(productId)) {
            detailFavBtn.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            detailFavBtn.setImageResource(R.drawable.ic_favorite_fil);
        }
    }

    private void setupClicks() {

        addToCartBtn.setOnClickListener(v -> {
            Product product = getCurrentProduct();
            CartManager.addToCart(product);

            Toast.makeText(this,
                    name + " added to cart",
                    Toast.LENGTH_SHORT).show();

            // Button animation
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

        detailFavBtn.setOnClickListener(v -> {
            Product product = getCurrentProduct();
            FavoriteManager.toggleFavorite(product);

            if (FavoriteManager.isFavoriteById(productId)) {
                detailFavBtn.setImageResource(R.drawable.ic_favorite_filled);
                Toast.makeText(this,
                        name + " added to favorites",
                        Toast.LENGTH_SHORT).show();
            } else {
                detailFavBtn.setImageResource(R.drawable.ic_favorite_fil);
                Toast.makeText(this,
                        name + " removed from favorites",
                        Toast.LENGTH_SHORT).show();
            }

            animateHeart(detailFavBtn);
        });

        backBtn.setOnClickListener(v -> navigateToHome(true));
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
}