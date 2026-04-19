package com.example.SYshop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LocalDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "syshop_local.db";
    public static final int DATABASE_VERSION = 4;

    public static final String TABLE_PRODUCTS_CACHE = "products_cache";
    public static final String TABLE_FAVORITES_CACHE = "favorites_cache";

    public LocalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createProductsTable = "CREATE TABLE " + TABLE_PRODUCTS_CACHE + " (" +
                "id INTEGER PRIMARY KEY, " +
                "document_id TEXT, " +
                "category TEXT, " +
                "tag TEXT, " +
                "name TEXT, " +
                "price TEXT, " +
                "description TEXT, " +
                "image_res INTEGER, " +
                "image_name TEXT, " +
                "image_url TEXT, " +
                "rating REAL, " +
                "review_count INTEGER" +
                ")";

        String createFavoritesTable = "CREATE TABLE " + TABLE_FAVORITES_CACHE + " (" +
                "id INTEGER PRIMARY KEY, " +
                "document_id TEXT, " +
                "category TEXT, " +
                "tag TEXT, " +
                "name TEXT, " +
                "price TEXT, " +
                "description TEXT, " +
                "image_res INTEGER, " +
                "image_name TEXT, " +
                "image_url TEXT, " +
                "rating REAL, " +
                "review_count INTEGER" +
                ")";

        db.execSQL(createProductsTable);
        db.execSQL(createFavoritesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS_CACHE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES_CACHE);
        onCreate(db);
    }
}
