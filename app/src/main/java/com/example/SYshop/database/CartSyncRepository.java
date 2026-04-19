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
import java.util.Map;

public class CartSyncRepository {

    public interface SyncCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface LoadCartCallback {
        void onLoaded(List<Map<String, Object>> items);
        void onError(String message);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public CartSyncRepository(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    private CollectionReference getCartCollection() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return null;
        }
        return firestore.collection("users")
                .document(user.getUid())
                .collection("cart");
    }

    public void addOrIncreaseCartItem(Product product, SyncCallback callback) {
        addOrIncreaseCartItem(product, 1, callback);
    }

    public void addOrIncreaseCartItem(Product product, int amount, SyncCallback callback) {
        CollectionReference collection = getCartCollection();
        if (collection == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        int safeAmount = Math.max(amount, 1);

        String docId = String.valueOf(product.getId());

        collection.document(docId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    long quantity = safeAmount;
                    if (documentSnapshot.exists()) {
                        Number q = (Number) documentSnapshot.get("quantity");
                        if (q != null) {
                            quantity = q.longValue() + safeAmount;
                        }
                    }

                    Map<String, Object> data = product.toStorageMap();
                    data.put("quantity", quantity);

                    collection.document(docId)
                            .set(data)
                            .addOnSuccessListener(unused -> {
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void updateQuantity(int productId, int quantity, SyncCallback callback) {
        CollectionReference collection = getCartCollection();
        if (collection == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        if (quantity <= 0) {
            removeItem(productId, callback);
            return;
        }

        collection.document(String.valueOf(productId))
                .update("quantity", quantity)
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void removeItem(int productId, SyncCallback callback) {
        CollectionReference collection = getCartCollection();
        if (collection == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        collection.document(String.valueOf(productId))
                .delete()
                .addOnSuccessListener(unused -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void loadCartFromCloud(@NonNull LoadCartCallback callback) {
        CollectionReference collection = getCartCollection();
        if (collection == null) {
            callback.onError("User not logged in");
            return;
        }

        collection.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> items = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        items.add(doc.getData());
                    }

                    callback.onLoaded(items);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}
