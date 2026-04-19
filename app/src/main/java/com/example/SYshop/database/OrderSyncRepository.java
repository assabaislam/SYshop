package com.example.SYshop.database;

import androidx.annotation.NonNull;

import com.example.SYshop.models.CartItem;
import com.example.SYshop.models.Product;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderSyncRepository {

    public interface CheckoutCallback {
        void onSuccess(String orderId);
        void onError(String message);
    }

    public interface LoadOrdersCallback {
        void onLoaded(java.util.List<Map<String, Object>> orders);
        void onError(String message);
    }

    // 🔥 IMPORTANT FIX
    public interface LoadOrderItemsCallback {
        void onLoaded(java.util.List<java.util.Map<String, Object>> items);
        void onError(String message);
    }

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;

    public OrderSyncRepository() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
    }

    private FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    public void checkout(@NonNull List<CartItem> cartItems, @NonNull String totalPrice, CheckoutCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        if (cartItems.isEmpty()) {
            if (callback != null) callback.onError("Cart is empty");
            return;
        }

        CollectionReference ordersRef = firestore.collection("users")
                .document(user.getUid())
                .collection("orders");

        String orderId = ordersRef.document().getId();

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("totalPrice", totalPrice);
        orderData.put("status", "Placed");
        orderData.put("createdAt", System.currentTimeMillis());
        orderData.put("itemsCount", cartItems.size());

        WriteBatch batch = firestore.batch();
        batch.set(ordersRef.document(orderId), orderData);

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            Map<String, Object> itemData = new HashMap<>(product.toStorageMap());
            itemData.put("productId", product.getId());
            itemData.put("quantity", cartItem.getQuantity());

            batch.set(
                    ordersRef.document(orderId)
                            .collection("items")
                            .document(String.valueOf(product.getId())),
                    itemData
            );
        }

        CollectionReference cartRef = firestore.collection("users")
                .document(user.getUid())
                .collection("cart");

        cartRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        batch.delete(cartRef.document(doc.getId()));
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {
                                if (callback != null) callback.onSuccess(orderId);
                            })
                            .addOnFailureListener(e -> {
                                if (callback != null) callback.onError(e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    // 🔥 FIXED METHOD
    public void loadOrderItems(String orderId, LoadOrderItemsCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("orders")
                .document(orderId)
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<java.util.Map<String, Object>> items = new java.util.ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        items.add(doc.getData());
                    }

                    if (callback != null) callback.onLoaded(items);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }

    public void loadOrders(LoadOrdersCallback callback) {
        FirebaseUser user = getUser();
        if (user == null) {
            if (callback != null) callback.onError("User not logged in");
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .collection("orders")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<Map<String, Object>> orders = new java.util.ArrayList<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        orders.add(doc.getData());
                    }

                    if (callback != null) callback.onLoaded(orders);
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
}
