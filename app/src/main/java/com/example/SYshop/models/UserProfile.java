package com.example.SYshop.models;

public class UserProfile {

    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String avatarUrl;
    private String avatarPreset;
    private String role;

    public UserProfile() {
        // Required empty constructor for Firestore
    }

    public UserProfile(String fullName, String email, String phone, String address) {
        this(fullName, email, phone, address, "", "user", "user");
    }

    public UserProfile(String fullName, String email, String phone, String address, String avatarUrl) {
        this(fullName, email, phone, address, avatarUrl, "", "user");
    }

    public UserProfile(String fullName, String email, String phone, String address, String avatarUrl, String avatarPreset) {
        this(fullName, email, phone, address, avatarUrl, avatarPreset, "user");
    }

    public UserProfile(String fullName, String email, String phone, String address, String avatarUrl, String avatarPreset, String role) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.avatarUrl = avatarUrl;
        this.avatarPreset = avatarPreset;
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarPreset() {
        return avatarPreset;
    }

    public void setAvatarPreset(String avatarPreset) {
        this.avatarPreset = avatarPreset;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
