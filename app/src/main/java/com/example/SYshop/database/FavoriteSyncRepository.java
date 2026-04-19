package com.example.SYshop.database;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.SYshop.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FavoriteSyncRepository {

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LoadFavoritesCallback {
        void onLoaded(List<Product> favorites);
        void onError(String message);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final FavoriteCacheRepository favoriteCacheRepository;

    public FavoriteSyncRepository(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        this.favoriteCacheRepository = new FavoriteCacheRepository(context);
    }

    private CollectionReference getFavoritesCollection() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return firestore.collection("users")
                .document(user.getUid())
                .collection("favorites");
    }

    public void addFavorite(Product product, SyncCallback callback) {
        CollectionReference collection = getFavoritesCollection();
        if (collection == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        collection.document(String.valueOf(product.getId()))
                .set(product.toStorageMap())
                .addOnSuccessListener(unused -> {
                    favoriteCacheRepository.saveFavorite(product);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    favoriteCacheRepository.saveFavorite(product);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void removeFavorite(Product product, SyncCallback callback) {
        CollectionReference collection = getFavoritesCollection();
        if (collection == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        collection.document(String.valueOf(product.getId()))
                .delete()
                .addOnSuccessListener(unused -> {
                    favoriteCacheRepository.removeFavorite(product.getId());
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    favoriteCacheRepository.removeFavorite(product.getId());
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void loadFavoritesFromCloud(@NonNull LoadFavoritesCallback callback) {
        CollectionReference collection = getFavoritesCollection();
        if (collection == null) {
            callback.onError("User not logged in");
            return;
        }

        collection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Product> favorites = new ArrayList<>();
                    favoriteCacheRepository.clearAllFavorites();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = Product.fromMap(doc.getData());
                        if (product == null) {
                            continue;
                        }

                        favorites.add(product);
                        favoriteCacheRepository.saveFavorite(product);
                    }

                    callback.onLoaded(favorites);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
