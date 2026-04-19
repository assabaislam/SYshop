package com.example.SYshop.activities;

import android.content.res.ColorStateList;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.SYshop.R;
import com.example.SYshop.managers.CartManager;
import com.example.SYshop.models.UserProfile;
import com.example.SYshop.utils.AuthManager;
import com.example.SYshop.utils.AvatarUtils;
import com.example.SYshop.utils.LanguageManager;
import com.example.SYshop.utils.UserRoleManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends BaseActivity {

    private BottomNavigationView bottomNavigation;
    private TextView fullNameText, emailText, phoneText, addressText;
    private TextView profileSubtitleText, avatarInitialsText;
    private ImageView profileAvatarImage;
    private MaterialButton logoutButton, editProfileButton, adminDashboardButton, englishLanguageButton, arabicLanguageButton;
    private LinearLayout loadingLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBackToHome();

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (!AuthManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        updateLanguageButtons();
        setupBottomNavigation();
        setupClicks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void initViews() {
        bottomNavigation = findViewById(R.id.bottomNavigation);
        fullNameText = findViewById(R.id.fullNameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        addressText = findViewById(R.id.addressText);
        profileSubtitleText = findViewById(R.id.profileSubtitleText);
        avatarInitialsText = findViewById(R.id.avatarInitialsText);
        profileAvatarImage = findViewById(R.id.profileAvatarImage);
        logoutButton = findViewById(R.id.logoutButton);
        editProfileButton = findViewById(R.id.editProfileButton);
        adminDashboardButton = findViewById(R.id.adminDashboardButton);
        englishLanguageButton = findViewById(R.id.englishLanguageButton);
        arabicLanguageButton = findViewById(R.id.arabicLanguageButton);
        loadingLayout = findViewById(R.id.loadingLayout);
        adminDashboardButton.setVisibility(View.GONE);
    }

    private void setupClicks() {
        logoutButton.setOnClickListener(v -> {
            firebaseAuth.signOut();
            CartManager.clearCart();
            Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        adminDashboardButton.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        englishLanguageButton.setOnClickListener(v -> applyLanguageSelection(LanguageManager.LANGUAGE_EN));
        arabicLanguageButton.setOnClickListener(v -> applyLanguageSelection(LanguageManager.LANGUAGE_AR));
    }

    private void loadProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        setLoading(true);

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);

                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);

                        if (userProfile != null) {
                            String fullName = emptyOrFallback(userProfile.getFullName(), getString(R.string.profile_avatar_user));
                            String email = emptyOrFallback(userProfile.getEmail(), currentUser.getEmail());

                            fullNameText.setText(fullName);
                            emailText.setText(email);
                            profileSubtitleText.setText(buildProfileSubtitle(fullName, email));
                            phoneText.setText(emptyOrFallback(userProfile.getPhone(), getString(R.string.not_added_yet)));
                            addressText.setText(emptyOrFallback(userProfile.getAddress(), getString(R.string.not_added_yet)));
                            AvatarUtils.bindAvatar(
                                    profileAvatarImage,
                                    avatarInitialsText,
                                    fullName,
                                    userProfile.getAvatarUrl(),
                                    userProfile.getAvatarPreset()
                            );
                            updateAdminButtonVisibility(userProfile.getRole());
                            return;
                        }
                    }

                    String email = emptyOrFallback(currentUser.getEmail(), getString(R.string.no_email));
                    fullNameText.setText(R.string.profile_avatar_user);
                    emailText.setText(email);
                    profileSubtitleText.setText(buildProfileSubtitle(getString(R.string.profile_avatar_user), email));
                    phoneText.setText(R.string.not_added_yet);
                    addressText.setText(R.string.not_added_yet);
                    AvatarUtils.bindAvatar(
                            profileAvatarImage,
                            avatarInitialsText,
                            getString(R.string.profile_avatar_user),
                            "",
                            AvatarUtils.PRESET_USER
                    );
                    updateAdminButtonVisibility(UserRoleManager.ROLE_USER);
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    updateAdminButtonVisibility(UserRoleManager.ROLE_USER);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String emptyOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private String buildProfileSubtitle(String fullName, String email) {
        return fullName + " | " + email;
    }

    private void applyLanguageSelection(String languageCode) {
        if (LanguageManager.isCurrentLanguage(this, languageCode)) {
            return;
        }

        LanguageManager.setSavedLanguage(this, languageCode);
        updateLanguageButtons();

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void updateLanguageButtons() {
        styleLanguageButton(englishLanguageButton, LanguageManager.isCurrentLanguage(this, LanguageManager.LANGUAGE_EN));
        styleLanguageButton(arabicLanguageButton, LanguageManager.isCurrentLanguage(this, LanguageManager.LANGUAGE_AR));
    }

    private void styleLanguageButton(MaterialButton button, boolean selected) {
        if (button == null) {
            return;
        }

        int backgroundColor = ContextCompat.getColor(this, selected ? R.color.navy_button : R.color.white);
        int textColor = ContextCompat.getColor(this, selected ? R.color.white : R.color.black_soft);
        int strokeColor = ContextCompat.getColor(this, selected ? R.color.navy_button : R.color.card_stroke);

        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(strokeColor));
    }

    private void updateAdminButtonVisibility(String role) {
        adminDashboardButton.setVisibility(UserRoleManager.isAdminRole(role) ? View.VISIBLE : View.GONE);
    }

    private void setLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        logoutButton.setEnabled(!isLoading);
        editProfileButton.setEnabled(!isLoading);
        adminDashboardButton.setEnabled(!isLoading);
        englishLanguageButton.setEnabled(!isLoading);
        arabicLanguageButton.setEnabled(!isLoading);
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_profile);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                navigateToHome(true);
                return true;
            } else if (id == R.id.nav_orders) {
                navigateTo(OrdersActivity.class, true);
                return true;
            } else if (id == R.id.nav_favorites) {
                navigateTo(FavoritesActivity.class, true);
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }

            return false;
        });
    }
}
