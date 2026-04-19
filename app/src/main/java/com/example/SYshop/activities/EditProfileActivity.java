package com.example.SYshop.activities;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.SYshop.R;
import com.example.SYshop.models.UserProfile;
import com.example.SYshop.utils.AuthManager;
import com.example.SYshop.utils.AvatarUtils;
import com.example.SYshop.utils.UserRoleManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class EditProfileActivity extends BaseActivity {

    private ImageView backBtn;
    private ImageView avatarPreviewImage;
    private EditText fullNameEditText, phoneEditText, addressEditText;
    private TextView avatarInitialsText;
    private MaterialButton saveButton;
    private MaterialButton presetUserButton, presetManButton, presetWomanButton;
    private LinearLayout avatarChoicesLayout;
    private LinearLayout loadingLayout;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private String selectedAvatarPreset = AvatarUtils.PRESET_USER;
    private String currentUserRole = UserRoleManager.ROLE_USER;
    private boolean avatarChoicesVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        if (!AuthManager.isLoggedIn()) {
            finish();
            return;
        }

        initViews();
        updatePresetButtons();
        updateAvatarPreview();
        setupClicks();
        loadCurrentProfile();
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtn);
        avatarPreviewImage = findViewById(R.id.avatarPreviewImage);
        fullNameEditText = findViewById(R.id.fullNameEditText);
        phoneEditText = findViewById(R.id.phoneEditText);
        addressEditText = findViewById(R.id.addressEditText);
        avatarInitialsText = findViewById(R.id.avatarInitialsText);
        saveButton = findViewById(R.id.saveButton);
        presetUserButton = findViewById(R.id.presetUserButton);
        presetManButton = findViewById(R.id.presetManButton);
        presetWomanButton = findViewById(R.id.presetWomanButton);
        avatarChoicesLayout = findViewById(R.id.avatarChoicesLayout);
        loadingLayout = findViewById(R.id.loadingLayout);
    }

    private void setupClicks() {
        backBtn.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveProfile());
        avatarPreviewImage.setOnClickListener(v -> toggleAvatarChoices());
        avatarInitialsText.setOnClickListener(v -> toggleAvatarChoices());
        presetUserButton.setOnClickListener(v -> selectPreset(AvatarUtils.PRESET_USER));
        presetManButton.setOnClickListener(v -> selectPreset(AvatarUtils.PRESET_MAN));
        presetWomanButton.setOnClickListener(v -> selectPreset(AvatarUtils.PRESET_WOMAN));

        TextWatcher avatarWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateAvatarPreview();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        fullNameEditText.addTextChangedListener(avatarWatcher);
    }

    private void loadCurrentProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        setLoading(true);

        firestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    setLoading(false);
                    if (documentSnapshot.exists()) {
                        UserProfile userProfile = documentSnapshot.toObject(UserProfile.class);
                        if (userProfile != null) {
                            fullNameEditText.setText(emptyOrValue(userProfile.getFullName()));
                            phoneEditText.setText(emptyOrValue(userProfile.getPhone()));
                            addressEditText.setText(emptyOrValue(userProfile.getAddress()));
                            currentUserRole = UserRoleManager.normalizeRole(userProfile.getRole());
                            selectedAvatarPreset = sanitizeSelectablePreset(userProfile.getAvatarPreset(), userProfile.getAvatarUrl());
                            updatePresetButtons();
                            updateAvatarPreview();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveProfile() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String fullName = fullNameEditText.getText().toString().trim();
        String phone = phoneEditText.getText().toString().trim();
        String address = addressEditText.getText().toString().trim();

        if (fullName.isEmpty()) {
            fullNameEditText.setError(getString(R.string.full_name_required));
            fullNameEditText.requestFocus();
            return;
        }

        setLoading(true);

        UserProfile updatedProfile = new UserProfile(
                fullName,
                currentUser.getEmail(),
                phone,
                address,
                "",
                selectedAvatarPreset,
                currentUserRole
        );

        firestore.collection("users")
                .document(currentUser.getUid())
                .set(updatedProfile, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    setLoading(false);
                    Toast.makeText(this, getString(R.string.profile_updated_successfully), Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void setLoading(boolean isLoading) {
        loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!isLoading);
        presetUserButton.setEnabled(!isLoading);
        presetManButton.setEnabled(!isLoading);
        presetWomanButton.setEnabled(!isLoading);
        avatarPreviewImage.setEnabled(!isLoading);
        avatarInitialsText.setEnabled(!isLoading);
    }

    private String emptyOrValue(String value) {
        return value == null ? "" : value;
    }

    private void updateAvatarPreview() {
        AvatarUtils.bindAvatar(
                avatarPreviewImage,
                avatarInitialsText,
                fullNameEditText.getText().toString().trim(),
                "",
                selectedAvatarPreset
        );
    }

    private void selectPreset(String preset) {
        selectedAvatarPreset = preset;
        updatePresetButtons();
        updateAvatarPreview();
        setAvatarChoicesVisible(false);
    }

    private void updatePresetButtons() {
        stylePresetButton(presetUserButton, AvatarUtils.PRESET_USER.equals(selectedAvatarPreset));
        stylePresetButton(presetManButton, AvatarUtils.PRESET_MAN.equals(selectedAvatarPreset));
        stylePresetButton(presetWomanButton, AvatarUtils.PRESET_WOMAN.equals(selectedAvatarPreset));
    }

    private void stylePresetButton(MaterialButton button, boolean selected) {
        int backgroundColor = ContextCompat.getColor(this, selected ? R.color.navy_button : R.color.white);
        int textColor = ContextCompat.getColor(this, selected ? R.color.white : R.color.navy_button);

        button.setBackgroundTintList(ColorStateList.valueOf(backgroundColor));
        button.setTextColor(textColor);
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.navy_button)));
    }

    private void toggleAvatarChoices() {
        setAvatarChoicesVisible(!avatarChoicesVisible);
    }

    private void setAvatarChoicesVisible(boolean visible) {
        avatarChoicesVisible = visible;
        avatarChoicesLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private String sanitizeSelectablePreset(String avatarPreset, String avatarUrl) {
        String normalizedPreset = AvatarUtils.normalizePreset(avatarPreset, avatarUrl);
        if (AvatarUtils.PRESET_MAN.equals(normalizedPreset) || AvatarUtils.PRESET_WOMAN.equals(normalizedPreset)) {
            return normalizedPreset;
        }
        return AvatarUtils.PRESET_USER;
    }
}
