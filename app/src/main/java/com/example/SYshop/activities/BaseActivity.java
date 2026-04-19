package com.example.SYshop.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SYshop.R;
import com.example.SYshop.utils.CartStateSync;
import com.example.SYshop.utils.LanguageManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LanguageManager.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    protected void navigateTo(Class<?> targetActivity, boolean finishCurrent) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);

        if (finishCurrent) {
            finish();
        }
    }

    protected void navigateToHome(boolean finishCurrent) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);

        if (finishCurrent) {
            finish();
        }
    }

    protected void setupBackToHome() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackNavigation();
            }
        });
    }

    protected void handleBackNavigation() {
        if (isTaskRoot()) {
            navigateToHome(true);
        } else {
            finish();
        }
    }

    protected void refreshCartState(Runnable onUpdated) {
        CartStateSync.refreshFromCloud(this, onUpdated);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
