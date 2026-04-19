package com.example.SYshop.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.example.SYshop.R;
import com.example.SYshop.adapters.CartAdapter;
import com.example.SYshop.database.CartSyncRepository;
import com.example.SYshop.database.OrderSyncRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.CartItem;
import com.example.SYshop.models.Product;
import com.example.SYshop.models.UserProfile;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends BaseActivity implements CartAdapter.OnCartChangedListener {

    private ImageView backBtn;
    private TextView cartCountText, cartEmptyText, totalPriceText;
    private RecyclerView cartRecycler;
    private MaterialButton checkoutButton;

    private CartAdapter cartAdapter;
    private List<CartItem> cartList;
    private CartSyncRepository cartSyncRepository;
    private OrderSyncRepository orderSyncRepository;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        setupBackToHome();

        cartSyncRepository = new CartSyncRepository(this);
        orderSyncRepository = new OrderSyncRepository();
        firestore = FirebaseFirestore.getInstance();

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
        checkoutButton = findViewById(R.id.checkoutButton);
    }

    private void loadCartItems() {
        cartList = CartManager.getCartItems();

        cartAdapter = new CartAdapter(this, cartList, this);
        cartRecycler.setLayoutManager(new LinearLayoutManager(this));
        cartRecycler.setAdapter(cartAdapter);

        updateCartUI();

        cartSyncRepository.loadCartFromCloud(new CartSyncRepository.LoadCartCallback() {
            @Override
            public void onLoaded(List<Map<String, Object>> items) {
                CartManager.clearCart();

                for (Map<String, Object> map : items) {
                    Product product = Product.fromMap(map);
                    int quantity = getInt(map.get("quantity"));

                    if (product == null) {
                        continue;
                    }

                    for (int i = 0; i < quantity; i++) {
                        CartManager.addToCart(product);
                    }
                }

                cartAdapter.notifyDataSetChanged();
                updateCartUI();
            }

            @Override
            public void onError(String message) {
                // keep local state if cloud load fails
            }
        });
    }

    private void updateCartUI() {
        cartCountText.setText(getString(R.string.items_count, CartManager.getCartCount()));

        if (cartList.isEmpty()) {
            cartEmptyText.setVisibility(View.VISIBLE);
            cartRecycler.setVisibility(View.GONE);
            checkoutButton.setVisibility(View.GONE);
            totalPriceText.setText(getString(R.string.price_zero));
        } else {
            cartEmptyText.setVisibility(View.GONE);
            cartRecycler.setVisibility(View.VISIBLE);
            checkoutButton.setVisibility(View.VISIBLE);
            calculateTotal();
        }
    }

    private void calculateTotal() {
        double total = 0;

        for (CartItem item : cartList) {
            String priceStr = item.getProduct().getPrice().replace("$", "").trim();
            try {
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (NumberFormatException ignored) {
            }
        }

        totalPriceText.setText(String.format(Locale.US, "$%.2f", total));
    }

    private String getTotalAsString() {
        double total = 0;

        for (CartItem item : cartList) {
            String priceStr = item.getProduct().getPrice().replace("$", "").trim();
            try {
                double price = Double.parseDouble(priceStr);
                total += price * item.getQuantity();
            } catch (NumberFormatException ignored) {
            }
        }

        return String.format(Locale.US, "$%.2f", total);
    }

    private void setupClicks() {
        backBtn.setOnClickListener(v -> finish());

        checkoutButton.setOnClickListener(v -> {
            if (cartList == null || cartList.isEmpty()) {
                Toast.makeText(this, getString(R.string.cart_empty), Toast.LENGTH_SHORT).show();
                return;
            }

            requestCheckoutConfirmation();
        });
    }

    private void requestCheckoutConfirmation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, getString(R.string.auth_generic_error), Toast.LENGTH_SHORT).show();
            return;
        }

        checkoutButton.setEnabled(false);
        Toast.makeText(this, getString(R.string.loading_checkout_info), Toast.LENGTH_SHORT).show();

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    checkoutButton.setEnabled(true);
                    UserProfile profile = documentSnapshot.toObject(UserProfile.class);
                    showCheckoutDialog(profile);
                })
                .addOnFailureListener(e -> {
                    checkoutButton.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showCheckoutDialog(UserProfile profile) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_checkout_confirmation, null, false);

        TextView dialogBody = dialogView.findViewById(R.id.dialogCheckoutBody);
        TextView dialogNameValue = dialogView.findViewById(R.id.dialogNameValue);
        TextView dialogPhoneValue = dialogView.findViewById(R.id.dialogPhoneValue);
        TextView dialogAddressValue = dialogView.findViewById(R.id.dialogAddressValue);
        TextView dialogTotalValue = dialogView.findViewById(R.id.dialogTotalValue);
        TextView dialogHint = dialogView.findViewById(R.id.dialogCheckoutHint);
        MaterialButton dialogSecondaryButton = dialogView.findViewById(R.id.dialogSecondaryButton);
        MaterialButton dialogPrimaryButton = dialogView.findViewById(R.id.dialogPrimaryButton);

        String fullName = profile != null && !TextUtils.isEmpty(profile.getFullName())
                ? profile.getFullName().trim()
                : "User";
        String phone = profile != null ? safeProfileValue(profile.getPhone()) : getString(R.string.not_added_yet);
        String address = profile != null ? safeProfileValue(profile.getAddress()) : getString(R.string.not_added_yet);
        boolean profileReady = isProfileReady(profile);

        dialogNameValue.setText(fullName);
        dialogPhoneValue.setText(phone);
        dialogAddressValue.setText(address);
        dialogTotalValue.setText(getTotalAsString());

        if (profileReady) {
            dialogBody.setText(getString(R.string.checkout_confirm_message));
            dialogHint.setText(getString(R.string.checkout_confirm_footer));
        } else {
            dialogBody.setText(getString(R.string.checkout_missing_info));
            dialogHint.setText(getString(R.string.checkout_missing_info_footer));
        }

        if (profileReady) {
            dialogSecondaryButton.setText(R.string.edit_profile_action);
            dialogPrimaryButton.setText(R.string.checkout_place_order);
        } else {
            dialogSecondaryButton.setText(R.string.cancel);
            dialogPrimaryButton.setText(R.string.edit_profile_action);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create();

        dialogSecondaryButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (profileReady) {
                navigateTo(EditProfileActivity.class, false);
            }
        });

        dialogPrimaryButton.setOnClickListener(v -> {
            dialog.dismiss();
            if (profileReady) {
                performCheckout();
            } else {
                navigateTo(EditProfileActivity.class, false);
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private boolean isProfileReady(UserProfile profile) {
        return profile != null
                && !TextUtils.isEmpty(safeTrim(profile.getPhone()))
                && !TextUtils.isEmpty(safeTrim(profile.getAddress()));
    }

    private String safeProfileValue(String value) {
        String trimmedValue = safeTrim(value);
        return TextUtils.isEmpty(trimmedValue) ? getString(R.string.not_added_yet) : trimmedValue;
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private void performCheckout() {
        checkoutButton.setEnabled(false);

        orderSyncRepository.checkout(cartList, getTotalAsString(), new OrderSyncRepository.CheckoutCallback() {
            @Override
            public void onSuccess(String orderId) {
                checkoutButton.setEnabled(true);
                int itemsCount = CartManager.getCartCount();
                String total = getTotalAsString();
                CartManager.clearCart();
                cartAdapter.notifyDataSetChanged();
                updateCartUI();
                showOrderSuccessDialog(orderId, total, itemsCount);
            }

            @Override
            public void onError(String message) {
                checkoutButton.setEnabled(true);
                Toast.makeText(CartActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOrderSuccessDialog(String orderId, String totalPrice, int itemsCount) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.order_share_title)
                .setMessage(buildOrderConfirmationMessage(orderId, totalPrice, itemsCount))
                .setNegativeButton(R.string.order_share_later, (dialog, which) -> {
                    dialog.dismiss();
                    navigateTo(OrdersActivity.class, true);
                })
                .setNeutralButton(R.string.order_share_now, (dialog, which) -> {
                    dialog.dismiss();
                    shareOrderConfirmation(orderId, totalPrice, itemsCount);
                    navigateTo(OrdersActivity.class, true);
                })
                .setPositiveButton(R.string.view_order_details, (dialog, which) -> {
                    dialog.dismiss();
                    navigateTo(OrdersActivity.class, true);
                })
                .show();
    }

    private void shareOrderConfirmation(String orderId, String totalPrice, int itemsCount) {
        String shareMessage = buildOrderShareMessage(orderId, totalPrice, itemsCount);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.order_share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

        try {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.order_share_chooser)));
        } catch (ActivityNotFoundException exception) {
            Toast.makeText(this, getString(R.string.order_share_unavailable), Toast.LENGTH_SHORT).show();
        }
    }

    private String buildOrderConfirmationMessage(String orderId, String totalPrice, int itemsCount) {
        return getString(
                R.string.order_share_success_message,
                getShortOrderId(orderId),
                totalPrice,
                itemsCount
        );
    }

    private String buildOrderShareMessage(String orderId, String totalPrice, int itemsCount) {
        return getString(
                R.string.order_share_message,
                getShortOrderId(orderId),
                itemsCount,
                totalPrice
        );
    }

    private String getShortOrderId(String orderId) {
        String safeOrderId = orderId == null ? "" : orderId.trim();
        return safeOrderId.length() > 8 ? safeOrderId.substring(0, 8) : safeOrderId;
    }

    private int getInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    @Override
    public void onCartChanged() {
        updateCartUI();
    }
}
