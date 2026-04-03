package com.example.SYshop.activities;

import android.content.Intent;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.SYshop.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected void navigateTo(Class<?> targetActivity, boolean finishCurrent) {
        Intent intent = new Intent(this, targetActivity);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (finishCurrent) {
            finish();
        }
    }

    protected void navigateToHome(boolean finishCurrent) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        if (finishCurrent) {
            finish();
        }
    }

    protected void setupBackToHome() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome(true);
            }
        });
    }
}