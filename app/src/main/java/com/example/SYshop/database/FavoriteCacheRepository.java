package com.example.SYshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class FavoriteCacheRepository {

    private final LocalDbHelper dbHelper;

    public FavoriteCacheRepository(Context context) {
        dbHelper = new LocalDbHelper(context);
    }

    public void saveFavorite(Product product) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", product.getId());
        values.put("category", product.getCategory());
        values.put("tag", product.getTag());
        values.put("name", product.getName());
        values.put("price", product.getPrice());
        values.put("description", product.getDescription());
        values.put("image_res", product.getImageRes());
        values.put("image_name", product.getPreferredLocalImageName());
        values.put("image_url", product.getImageUrl());
        values.put("rating", product.getRating());
        values.put("review_count", product.getReviewCount());

        db.insertWithOnConflict(LocalDbHelper.TABLE_FAVORITES_CACHE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public void removeFavorite(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(LocalDbHelper.TABLE_FAVORITES_CACHE, "id=?", new String[]{String.valueOf(productId)});
        db.close();
    }

    public void clearAllFavorites() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(LocalDbHelper.TABLE_FAVORITES_CACHE, null, null);
        db.close();
    }

    public boolean isFavoriteCached(int productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM " + LocalDbHelper.TABLE_FAVORITES_CACHE + " WHERE id=?",
                new String[]{String.valueOf(productId)}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
            db.close();
        }
    }

    public List<Product> getAllFavorites() {
        List<Product> favorites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocalDbHelper.TABLE_FAVORITES_CACHE, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    Product product = new Product(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getString(cursor.getColumnIndexOrThrow("tag")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("price")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("image_res")),
                            false,
                            0,
                            "",
                            cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("review_count"))
                    );
                    product.setImageName(cursor.getString(cursor.getColumnIndexOrThrow("image_name")));
                    product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                    favorites.add(product);
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }

        return favorites;
    }
}
