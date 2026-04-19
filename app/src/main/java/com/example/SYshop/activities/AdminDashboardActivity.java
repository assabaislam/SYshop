package com.example.SYshop.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.adapters.AdminProductAdapter;
import com.example.SYshop.database.ProductCacheRepository;
import com.example.SYshop.database.ProductRepository;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.ProductImageLoader;
import com.example.SYshop.utils.AuthManager;
import com.example.SYshop.utils.UserRoleManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminDashboardActivity extends BaseActivity implements AdminProductAdapter.OnAdminProductActionListener {

    private ImageView backBtn;
    private MaterialButton addProductButton;
    private TextView emptyText;
    private RecyclerView productsRecycler;
    private LinearLayout loadingLayout;

    private final List<Product> adminProducts = new ArrayList<>();
    private AdminProductAdapter adapter;
    private ProductRepository productRepository;
    private ProductCacheRepository productCacheRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        setupBackToHome();

        if (!AuthManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        productRepository = new ProductRepository();
        productCacheRepository = new ProductCacheRepository(this);

        initViews();
        setupRecycler();
        setupClicks();
        verifyAdminAccess();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        addProductButton = findViewById(R.id.addProductButton);
        emptyText = findViewById(R.id.emptyText);
        productsRecycler = findViewById(R.id.productsRecycler);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void setupRecycler() {
        adapter = new AdminProductAdapter(this, adminProducts, this);
        productsRecycler.setLayoutManager(new LinearLayoutManager(this));
        productsRecycler.setAdapter(adapter);
    }

    private void setupClicks() {
        backBtn.setOnClickListener(v -> finish());
        addProductButton.setOnClickListener(v -> showProductDialog(null));
    }

    private void verifyAdminAccess() {
        setLoading(true);
        UserRoleManager.loadCurrentUserRole(new UserRoleManager.UserRoleCallback() {
            @Override
            public void onLoaded(String role) {
                if (!UserRoleManager.isAdminRole(role)) {
                    setLoading(false);
                    Toast.makeText(AdminDashboardActivity.this, getString(R.string.admin_access_denied), Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                loadProducts();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Toast.makeText(
                        AdminDashboardActivity.this,
                        message == null || message.trim().isEmpty() ? getString(R.string.admin_access_denied) : message,
                        Toast.LENGTH_LONG
                ).show();
                finish();
            }
        });
    }

    private void loadProducts() {
        setLoading(true);
        productRepository.loadProducts(new ProductRepository.LoadProductsCallback() {
            @Override
            public void onLoaded(List<Product> products) {
                setLoading(false);
                adminProducts.clear();
                adminProducts.addAll(products);
                sortProducts();
                productCacheRepository.replaceAllProducts(adminProducts);
                adapter.notifyDataSetChanged();
                updateEmptyState();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                if (adminProducts.isEmpty()) {
                    adminProducts.clear();
                    adminProducts.addAll(productCacheRepository.getCachedProducts());
                    sortProducts();
                    adapter.notifyDataSetChanged();
                }
                updateEmptyState();
                Toast.makeText(
                        AdminDashboardActivity.this,
                        message == null || message.trim().isEmpty() ? getString(R.string.admin_load_failed) : message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void sortProducts() {
        Collections.sort(adminProducts, Comparator.comparingInt(Product::getId).reversed());
    }

    private void updateEmptyState() {
        boolean isEmpty = adminProducts.isEmpty();
        emptyText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        productsRecycler.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void setLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        addProductButton.setEnabled(!isLoading);
    }

    private void showProductDialog(Product existingProduct) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_admin_product, null, false);

        TextView dialogTitle = dialogView.findViewById(R.id.dialogTitle);
        TextView dialogSubtitle = dialogView.findViewById(R.id.dialogSubtitle);
        AutoCompleteTextView categoryInput = dialogView.findViewById(R.id.categoryInput);
        AutoCompleteTextView tagInput = dialogView.findViewById(R.id.tagInput);
        TextInputEditText nameInput = dialogView.findViewById(R.id.nameInput);
        TextInputEditText priceInput = dialogView.findViewById(R.id.priceInput);
        TextInputEditText oldPriceInput = dialogView.findViewById(R.id.oldPriceInput);
        TextInputEditText imageUrlInput = dialogView.findViewById(R.id.imageUrlInput);
        TextInputEditText descriptionInput = dialogView.findViewById(R.id.descriptionInput);
        TextInputEditText ratingInput = dialogView.findViewById(R.id.ratingInput);
        TextInputEditText reviewsInput = dialogView.findViewById(R.id.reviewsInput);
        ImageView previewImage = dialogView.findViewById(R.id.previewImage);
        TextView previewMeta = dialogView.findViewById(R.id.previewMeta);
        TextView previewName = dialogView.findViewById(R.id.previewName);
        TextView previewPrice = dialogView.findViewById(R.id.previewPrice);
        ScrollView formScrollView = dialogView.findViewById(R.id.formScrollView);
        MaterialButton cancelButton = dialogView.findViewById(R.id.cancelButton);
        MaterialButton saveButton = dialogView.findViewById(R.id.saveButton);

        String[] categories = {
                getString(R.string.category_shoes),
                getString(R.string.category_watches),
                getString(R.string.category_bags),
                getString(R.string.category_audio)
        };
        String[] tags = {
                getString(R.string.tag_none),
                getString(R.string.product_tag_promo),
                getString(R.string.product_tag_new),
                getString(R.string.product_tag_best_seller),
                getString(R.string.product_tag_top_rated)
        };

        categoryInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories));
        tagInput.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tags));

        boolean isEditMode = existingProduct != null;
        dialogTitle.setText(isEditMode ? R.string.admin_edit_product : R.string.admin_add_product);
        dialogSubtitle.setText(isEditMode ? R.string.admin_edit_dialog_subtitle : R.string.admin_dialog_subtitle);
        saveButton.setText(isEditMode ? R.string.admin_save_changes : R.string.admin_save_product);

        if (isEditMode) {
            fillProductForm(
                    existingProduct,
                    categoryInput,
                    tagInput,
                    nameInput,
                    priceInput,
                    oldPriceInput,
                    imageUrlInput,
                    descriptionInput,
                    ratingInput,
                    reviewsInput
            );
        } else {
            tagInput.setText(getString(R.string.tag_none), false);
        }

        bindPreview(
                existingProduct,
                categoryInput,
                tagInput,
                nameInput,
                priceInput,
                imageUrlInput,
                previewImage,
                previewMeta,
                previewName,
                previewPrice
        );

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        saveButton.setOnClickListener(v -> submitProduct(
                dialog,
                existingProduct,
                categoryInput,
                tagInput,
                nameInput,
                priceInput,
                oldPriceInput,
                imageUrlInput,
                descriptionInput,
                ratingInput,
                reviewsInput,
                saveButton
        ));

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            dialog.getWindow().setLayout((int) (screenWidth * 0.94f), ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        adjustDialogScrollArea(formScrollView);
    }

    private void adjustDialogScrollArea(ScrollView formScrollView) {
        if (formScrollView == null) {
            return;
        }

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int maxScrollHeight = Math.min(dpToPx(420), (int) (screenHeight * 0.5f));
        ViewGroup.LayoutParams params = formScrollView.getLayoutParams();
        params.height = maxScrollHeight;
        formScrollView.setLayoutParams(params);
    }

    private void bindPreview(Product existingProduct,
                             AutoCompleteTextView categoryInput,
                             AutoCompleteTextView tagInput,
                             TextInputEditText nameInput,
                             TextInputEditText priceInput,
                             TextInputEditText imageUrlInput,
                             ImageView previewImage,
                             TextView previewMeta,
                             TextView previewName,
                             TextView previewPrice) {
        TextWatcher previewWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePreviewCard(existingProduct, categoryInput, tagInput, nameInput, priceInput, imageUrlInput,
                        previewImage, previewMeta, previewName, previewPrice);
            }
        };

        nameInput.addTextChangedListener(previewWatcher);
        priceInput.addTextChangedListener(previewWatcher);
        imageUrlInput.addTextChangedListener(previewWatcher);
        categoryInput.addTextChangedListener(previewWatcher);
        tagInput.addTextChangedListener(previewWatcher);

        categoryInput.setOnItemClickListener((parent, view, position, id) ->
                updatePreviewCard(existingProduct, categoryInput, tagInput, nameInput, priceInput, imageUrlInput,
                        previewImage, previewMeta, previewName, previewPrice));
        tagInput.setOnItemClickListener((parent, view, position, id) ->
                updatePreviewCard(existingProduct, categoryInput, tagInput, nameInput, priceInput, imageUrlInput,
                        previewImage, previewMeta, previewName, previewPrice));

        updatePreviewCard(existingProduct, categoryInput, tagInput, nameInput, priceInput, imageUrlInput,
                previewImage, previewMeta, previewName, previewPrice);
    }

    private void updatePreviewCard(Product existingProduct,
                                   AutoCompleteTextView categoryInput,
                                   AutoCompleteTextView tagInput,
                                   TextInputEditText nameInput,
                                   TextInputEditText priceInput,
                                   TextInputEditText imageUrlInput,
                                   ImageView previewImage,
                                   TextView previewMeta,
                                   TextView previewName,
                                   TextView previewPrice) {
        String displayName = safeText(nameInput);
        String displayCategory = safeText(categoryInput);
        String displayTag = safeText(tagInput);
        String displayPrice = normalizePrice(safeText(priceInput));
        String displayImageUrl = safeText(imageUrlInput);

        previewName.setText(TextUtils.isEmpty(displayName)
                ? ""
                : displayName);
        previewName.setVisibility(TextUtils.isEmpty(displayName) ? View.INVISIBLE : View.VISIBLE);

        if (TextUtils.isEmpty(displayPrice)) {
            previewPrice.setText("");
            previewPrice.setVisibility(View.GONE);
        } else {
            previewPrice.setText(displayPrice);
            previewPrice.setVisibility(View.VISIBLE);
        }

        boolean hasCategory = !TextUtils.isEmpty(displayCategory);
        boolean hasTag = !TextUtils.isEmpty(displayTag) && !getString(R.string.tag_none).equalsIgnoreCase(displayTag);
        if (hasCategory && hasTag) {
            previewMeta.setText(displayCategory + " • " + displayTag);
        } else if (hasCategory) {
            previewMeta.setText(displayCategory);
        } else if (hasTag) {
            previewMeta.setText(displayTag);
        } else {
            previewMeta.setText(getString(R.string.admin_preview_meta));
        }

        String existingImageUrl = existingProduct != null ? existingProduct.getImageUrl() : "";
        String previewImageUrl = !TextUtils.isEmpty(displayImageUrl) ? displayImageUrl : existingImageUrl;
        int fallbackRes = resolvePreviewFallbackRes(existingProduct, displayCategory, displayName);

        if (TextUtils.isEmpty(previewImageUrl)) {
            previewImage.setImageDrawable(null);
            previewImage.setBackgroundResource(R.drawable.bg_admin_preview_placeholder);
            return;
        }

        previewImage.setBackground(null);
        ProductImageLoader.loadCenterCrop(previewImage, previewImageUrl, fallbackRes);
    }

    private int resolvePreviewFallbackRes(Product existingProduct, String displayCategory, String displayName) {
        if (existingProduct != null && existingProduct.getPreferredLocalImageRes() != 0) {
            return existingProduct.getPreferredLocalImageRes();
        }

        String normalizedCategory = mapCategoryValue(displayCategory);
        Product draftPreview = new Product(
                0,
                normalizedCategory,
                "",
                displayName,
                "",
                "",
                Product.getImageFromName("classic_watch1")
        );
        return draftPreview.getPreferredLocalImageRes();
    }

    private void fillProductForm(Product product,
                                 AutoCompleteTextView categoryInput,
                                 AutoCompleteTextView tagInput,
                                 TextInputEditText nameInput,
                                 TextInputEditText priceInput,
                                 TextInputEditText oldPriceInput,
                                 TextInputEditText imageUrlInput,
                                 TextInputEditText descriptionInput,
                                 TextInputEditText ratingInput,
                                 TextInputEditText reviewsInput) {
        categoryInput.setText(getDisplayCategory(product.getCategory()), false);
        tagInput.setText(getDisplayTag(product.getTag()), false);
        nameInput.setText(product.getName());
        priceInput.setText(stripPriceSymbol(product.getPrice()));
        oldPriceInput.setText(stripPriceSymbol(product.getOldPrice()));
        imageUrlInput.setText(product.getImageUrl());
        descriptionInput.setText(product.getDescription());
        ratingInput.setText(String.valueOf(product.getRating()));
        reviewsInput.setText(String.valueOf(product.getReviewCount()));
    }

    private void submitProduct(AlertDialog dialog,
                               Product existingProduct,
                               AutoCompleteTextView categoryInput,
                               AutoCompleteTextView tagInput,
                               TextInputEditText nameInput,
                               TextInputEditText priceInput,
                               TextInputEditText oldPriceInput,
                               TextInputEditText imageUrlInput,
                               TextInputEditText descriptionInput,
                               TextInputEditText ratingInput,
                               TextInputEditText reviewsInput,
                               MaterialButton saveButton) {
        String categoryDisplay = safeText(categoryInput);
        String tagDisplay = safeText(tagInput);
        String name = safeText(nameInput);
        String price = normalizePrice(safeText(priceInput));
        String oldPrice = normalizePrice(safeText(oldPriceInput));
        String imageUrl = safeText(imageUrlInput);
        String description = safeText(descriptionInput);

        if (TextUtils.isEmpty(name)) {
            nameInput.setError(getString(R.string.admin_required_field));
            nameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(categoryDisplay)) {
            categoryInput.setError(getString(R.string.admin_required_field));
            categoryInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(price)) {
            priceInput.setError(getString(R.string.admin_required_field));
            priceInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(imageUrl)) {
            imageUrlInput.setError(getString(R.string.admin_required_field));
            imageUrlInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            descriptionInput.setError(getString(R.string.admin_required_field));
            descriptionInput.requestFocus();
            return;
        }

        String category = mapCategoryValue(categoryDisplay);
        String tag = mapTagValue(tagDisplay);
        double rating = parseDoubleOrDefault(safeText(ratingInput), existingProduct != null ? existingProduct.getRating() : 4.5);
        int reviews = parseIntOrDefault(safeText(reviewsInput), existingProduct != null ? existingProduct.getReviewCount() : 0);
        boolean hasOffer = !TextUtils.isEmpty(oldPrice) || "Promo".equalsIgnoreCase(tag);
        int discountPercent = calculateDiscountPercent(price, oldPrice);

        int productId = existingProduct != null ? existingProduct.getId() : 0;
        Product product = new Product(
                productId,
                category,
                tag,
                name,
                price,
                description,
                Product.getImageFromName("classic_watch1"),
                hasOffer,
                discountPercent,
                hasOffer ? oldPrice : "",
                rating,
                reviews
        );
        product.setImageUrl(imageUrl);
        if (existingProduct != null) {
            product.setDocumentId(existingProduct.getDocumentId());
        }
        product.setImageName(product.getPreferredLocalImageName());
        product.setImageRes(resolveEditableImageRes(existingProduct, product));

        saveButton.setEnabled(false);
        boolean isEditMode = existingProduct != null;

        productRepository.addProduct(product, new ProductRepository.ProductActionCallback() {
            @Override
            public void onSuccess(Product savedProduct) {
                productCacheRepository.saveProduct(savedProduct);
                dialog.dismiss();
                Toast.makeText(
                        AdminDashboardActivity.this,
                        getString(isEditMode ? R.string.admin_product_updated : R.string.admin_product_added),
                        Toast.LENGTH_SHORT
                ).show();
                loadProducts();
            }

            @Override
            public void onError(String message) {
                saveButton.setEnabled(true);
                Toast.makeText(
                        AdminDashboardActivity.this,
                        message == null || message.trim().isEmpty()
                                ? getString(isEditMode ? R.string.admin_update_failed : R.string.admin_save_failed)
                                : message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onEditProduct(Product product) {
        if (product != null) {
            showProductDialog(product);
        }
    }

    @Override
    public void onDeleteProduct(Product product) {
        if (product == null) {
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.admin_delete_title))
                .setMessage(getString(R.string.admin_delete_message, product.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.admin_delete_action, (dialog, which) -> deleteProduct(product))
                .show();
    }

    private void deleteProduct(Product product) {
        productRepository.deleteProduct(product, new ProductRepository.DeleteProductCallback() {
            @Override
            public void onSuccess() {
                productCacheRepository.deleteProduct(product.getId());
                Toast.makeText(AdminDashboardActivity.this, getString(R.string.admin_product_deleted), Toast.LENGTH_SHORT).show();
                loadProducts();
            }

            @Override
            public void onError(String message) {
                Toast.makeText(
                        AdminDashboardActivity.this,
                        message == null || message.trim().isEmpty() ? getString(R.string.admin_delete_failed) : message,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private String safeText(TextView textView) {
        return textView.getText() == null ? "" : textView.getText().toString().trim();
    }

    private String normalizePrice(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }

        String safeValue = value.trim();
        return safeValue.startsWith("$") ? safeValue : "$" + safeValue;
    }

    private String stripPriceSymbol(String value) {
        return TextUtils.isEmpty(value) ? "" : value.replace("$", "").trim();
    }

    private String mapCategoryValue(String displayValue) {
        if (displayValue.equals(getString(R.string.category_shoes))) {
            return "Shoes";
        }
        if (displayValue.equals(getString(R.string.category_watches))) {
            return "Watches";
        }
        if (displayValue.equals(getString(R.string.category_bags))) {
            return "Bags";
        }
        if (displayValue.equals(getString(R.string.category_audio))) {
            return "Audio";
        }
        return displayValue;
    }

    private String getDisplayCategory(String storedValue) {
        if ("Shoes".equalsIgnoreCase(storedValue)) {
            return getString(R.string.category_shoes);
        }
        if ("Watches".equalsIgnoreCase(storedValue)) {
            return getString(R.string.category_watches);
        }
        if ("Bags".equalsIgnoreCase(storedValue)) {
            return getString(R.string.category_bags);
        }
        if ("Audio".equalsIgnoreCase(storedValue)) {
            return getString(R.string.category_audio);
        }
        return storedValue;
    }

    private String mapTagValue(String displayValue) {
        if (TextUtils.isEmpty(displayValue) || displayValue.equals(getString(R.string.tag_none))) {
            return "";
        }
        if (displayValue.equals(getString(R.string.product_tag_promo))) {
            return "Promo";
        }
        if (displayValue.equals(getString(R.string.product_tag_new))) {
            return "New";
        }
        if (displayValue.equals(getString(R.string.product_tag_best_seller))) {
            return "Best seller";
        }
        if (displayValue.equals(getString(R.string.product_tag_top_rated))) {
            return "Top rated";
        }
        return displayValue;
    }

    private String getDisplayTag(String storedValue) {
        if (TextUtils.isEmpty(storedValue)) {
            return getString(R.string.tag_none);
        }
        if ("Promo".equalsIgnoreCase(storedValue)) {
            return getString(R.string.product_tag_promo);
        }
        if ("New".equalsIgnoreCase(storedValue)) {
            return getString(R.string.product_tag_new);
        }
        if ("Best seller".equalsIgnoreCase(storedValue)) {
            return getString(R.string.product_tag_best_seller);
        }
        if ("Top rated".equalsIgnoreCase(storedValue)) {
            return getString(R.string.product_tag_top_rated);
        }
        return storedValue;
    }

    private double parseDoubleOrDefault(String value, double fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }

        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int parseIntOrDefault(String value, int fallback) {
        if (TextUtils.isEmpty(value)) {
            return fallback;
        }

        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private int calculateDiscountPercent(String price, String oldPrice) {
        double current = parsePriceValue(price);
        double old = parsePriceValue(oldPrice);

        if (current <= 0 || old <= current) {
            return 0;
        }

        return (int) Math.round(((old - current) / old) * 100.0);
    }

    private double parsePriceValue(String price) {
        if (TextUtils.isEmpty(price)) {
            return 0;
        }

        try {
            return Double.parseDouble(price.replace("$", "").replace(",", "").trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private int resolveEditableImageRes(Product existingProduct, Product draftProduct) {
        if (draftProduct.hasRemoteImageUrl()) {
            String category = draftProduct.getCategory();
            if ("Shoes".equalsIgnoreCase(category)) {
                return Product.getImageFromName("running_shoes");
            }
            if ("Watches".equalsIgnoreCase(category)) {
                return Product.getImageFromName("classic_watch1");
            }
            if ("Bags".equalsIgnoreCase(category)) {
                return Product.getImageFromName("leather_bag");
            }
            if ("Audio".equalsIgnoreCase(category)) {
                return Product.getImageFromName("wireless_headset");
            }
        }

        if (existingProduct != null && existingProduct.getImageRes() != 0) {
            return existingProduct.getImageRes();
        }

        String category = draftProduct.getCategory();
        if ("Shoes".equalsIgnoreCase(category)) {
            return Product.getImageFromName("running_shoes");
        }
        if ("Watches".equalsIgnoreCase(category)) {
            return Product.getImageFromName("classic_watch1");
        }
        if ("Bags".equalsIgnoreCase(category)) {
            return Product.getImageFromName("leather_bag");
        }
        if ("Audio".equalsIgnoreCase(category)) {
            return Product.getImageFromName("wireless_headset");
        }

        return draftProduct.getPreferredLocalImageRes();
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
