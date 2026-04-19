package com.example.SYshop.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.SYshop.models.CachedProduct;
import com.example.SYshop.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductCacheRepository {

    private final LocalDbHelper dbHelper;

    public ProductCacheRepository(Context context) {
        dbHelper = new LocalDbHelper(context);
    }

    public void replaceAllProducts(List<Product> products) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();

        try {
            db.delete(LocalDbHelper.TABLE_PRODUCTS_CACHE, null, null);

            for (Product product : products) {
                ContentValues values = new ContentValues();
                values.put("id", product.getId());
                values.put("document_id", product.getDocumentId());
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

                db.insert(LocalDbHelper.TABLE_PRODUCTS_CACHE, null, values);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void saveProduct(Product product) {
        if (product == null) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("id", product.getId());
            values.put("document_id", product.getDocumentId());
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

            db.insertWithOnConflict(
                    LocalDbHelper.TABLE_PRODUCTS_CACHE,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
            );
        } finally {
            db.close();
        }
    }

    public void deleteProduct(int productId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.delete(
                    LocalDbHelper.TABLE_PRODUCTS_CACHE,
                    "id=?",
                    new String[]{String.valueOf(productId)}
            );
        } finally {
            db.close();
        }
    }

    public List<CachedProduct> getAllCachedProducts() {
        List<CachedProduct> products = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + LocalDbHelper.TABLE_PRODUCTS_CACHE, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    products.add(new CachedProduct(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("document_id")),
                            cursor.getString(cursor.getColumnIndexOrThrow("category")),
                            cursor.getString(cursor.getColumnIndexOrThrow("tag")),
                            cursor.getString(cursor.getColumnIndexOrThrow("name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("price")),
                            cursor.getString(cursor.getColumnIndexOrThrow("description")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("image_res")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image_name")),
                            cursor.getString(cursor.getColumnIndexOrThrow("image_url")),
                            cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("review_count"))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
            db.close();
        }

        return products;
    }

    public List<Product> getCachedProducts() {
        List<Product> products = new ArrayList<>();

        for (CachedProduct cachedProduct : getAllCachedProducts()) {
            Product product = new Product(
                    cachedProduct.getId(),
                    cachedProduct.getCategory(),
                    cachedProduct.getTag(),
                    cachedProduct.getName(),
                    cachedProduct.getPrice(),
                    cachedProduct.getDescription(),
                    cachedProduct.getImageRes(),
                    cachedProduct.getRating(),
                    cachedProduct.getReviewCount()
            );
            product.setDocumentId(cachedProduct.getDocumentId());
            product.setImageName(cachedProduct.getImageName());
            product.setImageUrl(cachedProduct.getImageUrl());
            products.add(product);
        }

        return products;
    }

    public Product getCachedProductById(int productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + LocalDbHelper.TABLE_PRODUCTS_CACHE + " WHERE id=? LIMIT 1",
                new String[]{String.valueOf(productId)}
        );

        try {
            if (cursor.moveToFirst()) {
                Product product = new Product(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getString(cursor.getColumnIndexOrThrow("tag")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("description")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("image_res")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("review_count"))
                );
                product.setDocumentId(cursor.getString(cursor.getColumnIndexOrThrow("document_id")));
                product.setImageName(cursor.getString(cursor.getColumnIndexOrThrow("image_name")));
                product.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow("image_url")));
                return product;
            }
        } finally {
            cursor.close();
            db.close();
        }

        return null;
    }

    public boolean hasCachedProducts() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + LocalDbHelper.TABLE_PRODUCTS_CACHE, null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
            return false;
        } finally {
            cursor.close();
            db.close();
        }
    }
}
