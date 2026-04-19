package com.example.SYshop.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.SYshop.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class AuthManager {

    public static boolean isLoggedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    public static boolean requireLogin(Context context) {
        if (isLoggedIn()) {
            return true;
        }

        Toast.makeText(context, "Please login or register first", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(context, LoginActivity.class);
        context.startActivity(intent);

        return false;
    }
}