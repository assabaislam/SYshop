package com.example.SYshop.managers;

import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {

    private static final List<Product> favoriteItems = new ArrayList<>();

    public static boolean toggleFavorite(Product product) {
        for (int i = 0; i < favoriteItems.size(); i++) {
            if (favoriteItems.get(i).getId() == product.getId()) {
                favoriteItems.remove(i);
                return false;
            }
        }

        favoriteItems.add(product);
        return true;
    }

    public static boolean isFavorite(Product product) {
        for (Product item : favoriteItems) {
            if (item.getId() == product.getId()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFavoriteById(int productId) {
        for (Product item : favoriteItems) {
            if (item.getId() == productId) {
                return true;
            }
        }
        return false;
    }

    public static List<Product> getFavoriteItems() {
        return favoriteItems;
    }

    public static int getFavoriteCount() {
        return favoriteItems.size();
    }

    public static void removeFavorite(int position) {
        if (position >= 0 && position < favoriteItems.size()) {
            favoriteItems.remove(position);
        }
    }

    public static void removeFavoriteById(int productId) {
        for (int i = 0; i < favoriteItems.size(); i++) {
            if (favoriteItems.get(i).getId() == productId) {
                favoriteItems.remove(i);
                return;
            }
        }
    }

    public static void replaceFavorites(List<Product> products) {
        favoriteItems.clear();
        if (products != null) {
            favoriteItems.addAll(products);
        }
    }
}
