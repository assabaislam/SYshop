package com.example.SYshop.database;

import androidx.annotation.NonNull;

import com.example.SYshop.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductRepository {

    private static final Map<Integer, Product> MEMORY_CACHE = new HashMap<>();
    private static boolean productsDirty = false;
    private final FirebaseFirestore firestore;

    public interface LoadProductsCallback {
        void onLoaded(List<Product> products);
        void onError(String message);
    }

    public interface LoadProductCallback {
        void onLoaded(Product product);
        void onError(String message);
    }

    public interface ProductActionCallback {
        void onSuccess(Product product);
        void onError(String message);
    }

    public interface DeleteProductCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface NextProductIdCallback {
        void onLoaded(int nextProductId);
        void onError(String message);
    }

    public ProductRepository() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void addProduct(Product product, ProductActionCallback callback) {
        if (product == null) {
            if (callback != null) {
                callback.onError("Product data is missing");
            }
            return;
        }

        if (product.getId() > 0) {
            saveProduct(product, callback);
            return;
        }

        getNextProductId(new NextProductIdCallback() {
            @Override
            public void onLoaded(int nextProductId) {
                product.setId(nextProductId);
                saveProduct(product, callback);
            }

            @Override
            public void onError(String message) {
                if (callback != null) {
                    callback.onError(message);
                }
            }
        });
    }

    public void deleteProduct(int productId, DeleteProductCallback callback) {
        deleteProductByDocumentId(String.valueOf(productId), productId, callback);
    }

    public void deleteProduct(Product product, DeleteProductCallback callback) {
        if (product == null) {
            if (callback != null) {
                callback.onError("Product not found");
            }
            return;
        }

        String documentId = product.getDocumentId().trim().isEmpty()
                ? String.valueOf(product.getId())
                : product.getDocumentId();

        deleteProductByDocumentId(documentId, product.getId(), callback);
    }

    private void deleteProductByDocumentId(String documentId, int productId, DeleteProductCallback callback) {
        firestore.collection("products")
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    MEMORY_CACHE.remove(productId);
                    productsDirty = true;
                    if (callback != null) {
                        callback.onSuccess();
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void getNextProductId(NextProductIdCallback callback) {
        firestore.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int maxId = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = mapDocument(doc);
                        if (product != null) {
                            maxId = Math.max(maxId, product.getId());
                        }
                    }

                    if (callback != null) {
                        callback.onLoaded(maxId + 1);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void loadProducts(LoadProductsCallback callback) {
        firestore.collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<Integer, Product> bestProductsById = new HashMap<>();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Product product = mapDocument(doc);
                        if (product != null) {
                            Product currentBest = bestProductsById.get(product.getId());
                            bestProductsById.put(product.getId(), pickBetterProduct(currentBest, product));
                        }
                    }

                    List<Product> products = new ArrayList<>(bestProductsById.values());

                    cacheProducts(products);

                    if (callback != null) {
                        callback.onLoaded(products);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public void loadProductById(int productId, LoadProductCallback callback) {
        loadProductByField(productId, callback);
    }

    public Product getCachedProduct(int productId) {
        return MEMORY_CACHE.get(productId);
    }

    public List<Product> getCachedProducts() {
        return Collections.unmodifiableList(new ArrayList<>(MEMORY_CACHE.values()));
    }

    public static boolean consumeDirtyFlag() {
        boolean wasDirty = productsDirty;
        productsDirty = false;
        return wasDirty;
    }

    private void loadProductByField(int productId, LoadProductCallback callback) {
        firestore.collection("products")
                .whereEqualTo("id", productId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Product bestProduct = null;

                    for (com.google.firebase.firestore.DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        Product product = mapDocument(documentSnapshot);
                        bestProduct = pickBetterProduct(bestProduct, product);
                    }

                    Product cachedProduct = MEMORY_CACHE.get(productId);
                    bestProduct = pickBetterProduct(bestProduct, cachedProduct);

                    if (bestProduct != null) {
                        cacheProduct(bestProduct);
                        if (callback != null) {
                            callback.onLoaded(bestProduct);
                        }
                        return;
                    }

                    firestore.collection("products")
                            .document(String.valueOf(productId))
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Product fallbackProduct = documentSnapshot.exists() ? mapDocument(documentSnapshot) : null;
                                fallbackProduct = pickBetterProduct(fallbackProduct, cachedProduct);

                                if (fallbackProduct != null) {
                                    cacheProduct(fallbackProduct);
                                    if (callback != null) {
                                        callback.onLoaded(fallbackProduct);
                                    }
                                    return;
                                }

                                if (callback != null) {
                                    callback.onError("Product not found");
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (cachedProduct != null) {
                                    if (callback != null) {
                                        callback.onLoaded(cachedProduct);
                                    }
                                } else if (callback != null) {
                                    callback.onError(e.getMessage());
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Product cachedProduct = MEMORY_CACHE.get(productId);
                    if (cachedProduct != null) {
                        if (callback != null) {
                            callback.onLoaded(cachedProduct);
                        }
                        return;
                    }

                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    private Product pickBetterProduct(Product current, Product candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null) {
            return candidate;
        }
        return scoreProduct(candidate) > scoreProduct(current) ? candidate : current;
    }

    private int scoreProduct(Product product) {
        if (product == null) {
            return Integer.MIN_VALUE;
        }

        int score = 0;

        if (product.hasRemoteImageUrl()) {
            score += 100;
        }
        if (!product.getImageUrl().trim().isEmpty()) {
            score += 40;
        }
        if (!product.getDocumentId().trim().isEmpty()) {
            score += 10;
        }
        if (!product.getDescription().trim().isEmpty()) {
            score += 5;
        }
        if (!product.getName().trim().isEmpty()) {
            score += 5;
        }
        if (product.getReviewCount() > 0) {
            score += 2;
        }

        return score;
    }

    private void saveProduct(Product product, ProductActionCallback callback) {
        String documentId = product.getDocumentId().trim().isEmpty()
                ? String.valueOf(product.getId())
                : product.getDocumentId();
        product.setDocumentId(documentId);

        firestore.collection("products")
                .document(documentId)
                .set(product.toStorageMap())
                .addOnSuccessListener(unused -> {
                    cacheProduct(product);
                    productsDirty = true;
                    if (callback != null) {
                        callback.onSuccess(product);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    private Product mapDocument(@NonNull com.google.firebase.firestore.DocumentSnapshot documentSnapshot) {
        Map<String, Object> data = documentSnapshot.getData();
        if (data == null) {
            return null;
        }

        if (!data.containsKey("id")) {
            try {
                data.put("id", Integer.parseInt(documentSnapshot.getId()));
            } catch (Exception ignored) {
            }
        }

        Product product = Product.fromMap(data);
        if (product != null) {
            product.setDocumentId(documentSnapshot.getId());
        }
        return product;
    }

    private void cacheProducts(List<Product> products) {
        MEMORY_CACHE.clear();
        for (Product product : products) {
            cacheProduct(product);
        }
    }

    private void cacheProduct(Product product) {
        if (product != null) {
            MEMORY_CACHE.put(product.getId(), product);
        }
    }
}
