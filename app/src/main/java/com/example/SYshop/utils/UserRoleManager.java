package com.example.SYshop.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public final class UserRoleManager {

    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    public interface UserRoleCallback {
        void onLoaded(String role);
        void onError(String message);
    }

    private UserRoleManager() {
    }

    public static void loadCurrentUserRole(UserRoleCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            if (callback != null) {
                callback.onError("User not logged in");
            }
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = normalizeRole(documentSnapshot.getString("role"));
                    if (callback != null) {
                        callback.onLoaded(role);
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) {
                        callback.onError(e.getMessage());
                    }
                });
    }

    public static boolean isAdminRole(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(normalizeRole(role));
    }

    public static String normalizeRole(String role) {
        return ROLE_ADMIN.equalsIgnoreCase(role) ? ROLE_ADMIN : ROLE_USER;
    }
}
