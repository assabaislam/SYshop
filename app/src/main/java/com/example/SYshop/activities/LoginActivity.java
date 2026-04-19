package com.example.SYshop.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.SYshop.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends BaseActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private MaterialButton loginButton;
    private TextView registerNowText;
    private TextView forgotPasswordText;
    private LinearLayout loadingLayout;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            openMainAndFinish();
            return;
        }

        initViews();
        setupClicks();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerNowText = findViewById(R.id.registerNowText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void setupClicks() {
        loginButton.setOnClickListener(v -> attemptLogin());

        registerNowText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        forgotPasswordText.setOnClickListener(v -> sendPasswordReset());
    }

    private void attemptLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!isValidInput(email, password)) {
            return;
        }

        setLoading(true);

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    setLoading(false);

                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        openMainAndFinish();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_generic_error);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isValidInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.email_required));
            emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.email_invalid));
            emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.password_required));
            passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError(getString(R.string.password_min_length));
            passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void sendPasswordReset() {
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.enter_email_for_reset));
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError(getString(R.string.email_invalid));
            emailEditText.requestFocus();
            return;
        }

        setLoading(true);

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show();
                    } else {
                        String message = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_generic_error);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        registerNowText.setEnabled(!isLoading);
        forgotPasswordText.setEnabled(!isLoading);
    }

    private void openMainAndFinish() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
