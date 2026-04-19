package com.example.SYshop.managers;

import com.example.SYshop.models.CartItem;
import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final List<CartItem> cartItems = new ArrayList<>();

    public static void addToCart(Product product) {
        addToCart(product, 1);
    }

    public static void addToCart(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                for (int i = 0; i < quantity; i++) {
                    item.increaseQuantity();
                }
                return;
            }
        }

        cartItems.add(new CartItem(product, quantity));
    }

    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    public static void replaceCartItems(List<CartItem> items) {
        cartItems.clear();

        if (items != null && !items.isEmpty()) {
            cartItems.addAll(items);
        }
    }

    public static int getCartCount() {
        int count = 0;
        for (CartItem item : cartItems) {
            count += item.getQuantity();
        }
        return count;
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static void increaseQuantity(int position) {
        cartItems.get(position).increaseQuantity();
    }

    public static void decreaseQuantity(int position) {
        CartItem item = cartItems.get(position);

        if (item.getQuantity() > 1) {
            item.decreaseQuantity();
        } else {
            cartItems.remove(position);
        }
    }
}
