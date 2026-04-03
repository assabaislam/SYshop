package com.example.SYshop.managers;

import com.example.SYshop.models.CartItem;
import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final List<CartItem> cartItems = new ArrayList<>();

    public static void addToCart(Product product) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                item.increaseQuantity();
                return;
            }
        }

        cartItems.add(new CartItem(product, 1));
    }

    public static List<CartItem> getCartItems() {
        return cartItems;
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