package com.example.SYshop.utils;

import android.content.Context;

import com.example.SYshop.database.CartSyncRepository;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.CartItem;
import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CartStateSync {

    private CartStateSync() {
    }

    public static void refreshFromCloud(Context context, Runnable onComplete) {
        if (context == null) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (!AuthManager.isLoggedIn()) {
            CartManager.clearCart();
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        CartSyncRepository cartSyncRepository = new CartSyncRepository(context);
        cartSyncRepository.loadCartFromCloud(new CartSyncRepository.LoadCartCallback() {
            @Override
            public void onLoaded(List<Map<String, Object>> items) {
                List<CartItem> syncedItems = new ArrayList<>();

                for (Map<String, Object> map : items) {
                    Product product = Product.fromMap(map);
                    int quantity = getQuantity(map.get("quantity"));

                    if (product == null || quantity <= 0) {
                        continue;
                    }

                    syncedItems.add(new CartItem(product, quantity));
                }

                CartManager.replaceCartItems(syncedItems);

                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(String message) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
    }

    private static int getQuantity(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
}
