package com.example.SYshop.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.R;
import com.example.SYshop.adapters.ProductAdapter;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.ProductImageLoader;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class SearchActivity extends BaseActivity {

    private ImageView backBtn;
    private EditText searchInput;
    private TextView topSearchesTitle;
    private TextView seeAllText;
    private TextView emptyResultsText;
    private TextView resultsTitleText;
    private View categorySection;
    private View topSearchesSection;
    private View resultsHeader;
    private LinearLayout categoryContainer;
    private ChipGroup topSearchesGroup;
    private RecyclerView bestSellingRecycler;

    private ProductAdapter productAdapter;
    private final List<Product> allProducts = new ArrayList<>();
    private ProductRepository productRepository;
    private ProductCacheRepository productCacheRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        productRepository = new ProductRepository();
        productCacheRepository = new ProductCacheRepository(this);

        initViews();
        setupRecycler();
        setupActions();
        loadProducts();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        searchInput = findViewById(R.id.searchInput);
        topSearchesTitle = findViewById(R.id.topSearchesTitle);
        seeAllText = findViewById(R.id.seeAllText);
        emptyResultsText = findViewById(R.id.emptyResultsText);
        resultsTitleText = findViewById(R.id.resultsTitleText);
        categorySection = findViewById(R.id.categorySection);
        topSearchesSection = findViewById(R.id.topSearchesSection);
        resultsHeader = findViewById(R.id.resultsHeader);
        categoryContainer = findViewById(R.id.categoryContainer);
        topSearchesGroup = findViewById(R.id.topSearchesGroup);
        bestSellingRecycler = findViewById(R.id.bestSellingRecycler);
    }

    private void setupRecycler() {
        productAdapter = new ProductAdapter(
                this,
                new ArrayList<>(),
                () -> { },
                () -> productAdapter.notifyDataSetChanged()
        );

        bestSellingRecycler.setLayoutManager(new GridLayoutManager(this, 2));
        bestSellingRecycler.setHasFixedSize(true);
        bestSellingRecycler.setNestedScrollingEnabled(false);
        bestSellingRecycler.setItemAnimator(new DefaultItemAnimator());
        bestSellingRecycler.setAdapter(productAdapter);
    }

    private void setupActions() {
        backBtn.setOnClickListener(v -> finish());

        seeAllText.setOnClickListener(v -> {
            searchInput.setText("");
            searchInput.clearFocus();
            hideKeyboard();
        });

        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER);
            if (isSearchAction) {
                submitSearch(getNormalizedQuery(searchInput.getText()));
                hideKeyboard();
                searchInput.clearFocus();
                return true;
            }
            return false;
        });

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleTyping(getNormalizedQuery(s));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        searchInput.requestFocus();
        searchInput.post(this::showKeyboard);
    }

    private void loadProducts() {
        List<Product> memoryCached = productRepository.getCachedProducts();
        if (!memoryCached.isEmpty()) {
            onProductsReady(memoryCached);
        } else {
            List<Product> localCached = productCacheRepository.getCachedProducts();
            if (!localCached.isEmpty()) {
                onProductsReady(localCached);
            }
        }

        productRepository.loadProducts(new ProductRepository.LoadProductsCallback() {
            @Override
            public void onLoaded(List<Product> products) {
                onProductsReady(products);
            }

            @Override
            public void onError(String message) {
                handleTyping(getNormalizedQuery(searchInput.getText()));
            }
        });
    }

    private void onProductsReady(List<Product> products) {
        allProducts.clear();
        if (products != null) {
            allProducts.addAll(products);
        }

        productAdapter.submitProducts(allProducts);
        productAdapter.setCategory("All");
        productAdapter.setTag("All");
        renderCategoryStrip();
        handleTyping(getNormalizedQuery(searchInput.getText()));
    }

    private void renderCategoryStrip() {
        categoryContainer.removeAllViews();

        Map<String, Product> categorySamples = new LinkedHashMap<>();
        for (Product product : allProducts) {
            if (!categorySamples.containsKey(product.getCategory())) {
                categorySamples.put(product.getCategory(), product);
            }
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        for (Map.Entry<String, Product> entry : categorySamples.entrySet()) {
            View itemView = inflater.inflate(R.layout.item_search_category, categoryContainer, false);
            ImageView categoryImage = itemView.findViewById(R.id.categoryImage);
            TextView categoryLabel = itemView.findViewById(R.id.categoryLabel);

            String categoryName = entry.getKey();
            Product sampleProduct = entry.getValue();
            categoryLabel.setText(categoryName);
            bindProductImage(categoryImage, sampleProduct);

            itemView.setOnClickListener(v -> {
                searchInput.setText(categoryName);
                searchInput.setSelection(categoryName.length());
            });

            categoryContainer.addView(itemView);
        }
    }

    private void renderTopSearches(String query) {
        topSearchesGroup.removeAllViews();

        LinkedHashSet<String> suggestions = new LinkedHashSet<>();

        if (query == null || query.isEmpty()) {
            for (Product product : allProducts) {
                if (suggestions.size() < 6 && !product.getCategory().isEmpty()) {
                    suggestions.add(product.getCategory());
                }
                if (suggestions.size() < 6 && !product.getTag().isEmpty()) {
                    suggestions.add(product.getTag());
                }
                if (suggestions.size() >= 6) {
                    break;
                }
            }
        } else {
            String lowerQuery = query.toLowerCase();
            for (Product product : allProducts) {
                if (suggestions.size() < 8 && product.getName().toLowerCase().contains(lowerQuery)) {
                    suggestions.add(product.getName());
                }
                if (suggestions.size() < 8 && product.getCategory().toLowerCase().contains(lowerQuery)) {
                    suggestions.add(product.getCategory());
                }
                if (suggestions.size() < 8 && product.getTag().toLowerCase().contains(lowerQuery)) {
                    suggestions.add(product.getTag());
                }
                if (suggestions.size() >= 8) {
                    break;
                }
            }
        }

        for (String suggestion : suggestions) {
            Chip chip = new Chip(this);
            chip.setText(suggestion);
            chip.setCheckable(false);
            chip.setClickable(true);
            chip.setChipBackgroundColorResource(R.color.badge_bg);
            chip.setChipStrokeColorResource(R.color.badge_border);
            chip.setChipStrokeWidth(dpToPx(1));
            chip.setTextColor(getColor(R.color.text_gray_dark));
            chip.setOnClickListener(v -> {
                searchInput.setText(suggestion);
                searchInput.setSelection(suggestion.length());
                submitSearch(suggestion);
                hideKeyboard();
                searchInput.clearFocus();
            });
            topSearchesGroup.addView(chip);
        }
    }

    private void handleTyping(String query) {
        if (query.isEmpty()) {
            productAdapter.submitProducts(allProducts);
            productAdapter.setCategory("All");
            productAdapter.setTag("All");
            productAdapter.setSearchQuery("");
            renderTopSearches("");
            showDiscoveryMode();
            updateResultVisibility();
            return;
        }

        renderTopSearches(query);
        showSuggestionMode();
    }

    private void submitSearch(String query) {
        String normalizedQuery = getNormalizedQuery(query);
        if (normalizedQuery.isEmpty()) {
            handleTyping("");
            return;
        }

        productAdapter.submitProducts(allProducts);
        productAdapter.setCategory("All");
        productAdapter.setTag("All");
        productAdapter.setSearchQuery(normalizedQuery);
        showResultsMode(normalizedQuery);
        updateResultVisibility();
    }

    private void showDiscoveryMode() {
        categorySection.setVisibility(View.VISIBLE);
        topSearchesSection.setVisibility(View.VISIBLE);
        topSearchesTitle.setText(R.string.top_searches);
        resultsHeader.setVisibility(View.VISIBLE);
        seeAllText.setVisibility(View.VISIBLE);
        resultsTitleText.setText(R.string.best_selling);
    }

    private void showSuggestionMode() {
        categorySection.setVisibility(View.GONE);
        topSearchesSection.setVisibility(View.VISIBLE);
        topSearchesTitle.setText(R.string.search_suggestions);
        resultsHeader.setVisibility(View.GONE);
        bestSellingRecycler.setVisibility(View.GONE);
        emptyResultsText.setVisibility(View.GONE);
    }

    private void showResultsMode(String query) {
        categorySection.setVisibility(View.GONE);
        topSearchesSection.setVisibility(View.GONE);
        resultsHeader.setVisibility(View.VISIBLE);
        seeAllText.setVisibility(View.GONE);
        resultsTitleText.setText(getString(R.string.search_results_for, query));
    }

    private void updateResultVisibility() {
        if (productAdapter != null && productAdapter.isEmpty()) {
            emptyResultsText.setVisibility(View.VISIBLE);
            bestSellingRecycler.setVisibility(View.GONE);
        } else {
            emptyResultsText.setVisibility(View.GONE);
            bestSellingRecycler.setVisibility(View.VISIBLE);
        }
    }

    private void bindProductImage(ImageView imageView, Product product) {
        if (product != null) {
            ProductImageLoader.loadCenterCrop(imageView, product.getImageUrl(), product.getPreferredLocalImageRes());
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(searchInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchInput.getWindowToken(), 0);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private String getNormalizedQuery(CharSequence value) {
        return value == null ? "" : value.toString().trim();
    }
}
