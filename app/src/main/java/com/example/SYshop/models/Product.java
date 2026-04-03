package com.example.SYshop.models;

import java.util.ArrayList;
import java.util.List;

public class Product {

    private int id;
    private String category;
    private String tag;
    private String name;
    private String price;
    private String description;

    private int imageRes;
    private List<Integer> imagesList;

    private boolean hasOffer;
    private int discountPercent;
    private String oldPrice;

    private double rating;
    private int reviewCount;

    // ===== CONSTRUCTOR 1 (basic with single image) =====
    public Product(int id, String category, String tag, String name,
                   String price, String description, int imageRes) {

        this.id = id;
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageRes = imageRes;

        this.imagesList = new ArrayList<>();
        this.imagesList.add(imageRes);

        this.hasOffer = false;
        this.discountPercent = 0;
        this.oldPrice = "";

        this.rating = 0.0;
        this.reviewCount = 0;
    }

    // ===== CONSTRUCTOR 2 (with offer) =====
    public Product(int id, String category, String tag, String name,
                   String price, String description, int imageRes,
                   boolean hasOffer, int discountPercent, String oldPrice) {

        this(id, category, tag, name, price, description, imageRes);

        this.hasOffer = hasOffer;
        this.discountPercent = discountPercent;
        this.oldPrice = oldPrice;
    }

    // ===== CONSTRUCTOR 3 (multiple images) =====
    public Product(int id, String category, String tag, String name,
                   String price, String description, List<Integer> imagesList) {

        this.id = id;
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;

        if (imagesList != null && !imagesList.isEmpty()) {
            this.imagesList = imagesList;
            this.imageRes = imagesList.get(0);
        } else {
            this.imagesList = new ArrayList<>();
            this.imageRes = 0;
        }

        this.hasOffer = false;
        this.discountPercent = 0;
        this.oldPrice = "";

        this.rating = 0.0;
        this.reviewCount = 0;
    }

    // ===== CONSTRUCTOR 4 (multiple images + rating) =====
    public Product(int id, String category, String tag, String name,
                   String price, String description, List<Integer> imagesList,
                   double rating, int reviewCount) {

        this(id, category, tag, name, price, description, imagesList);

        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    // ===== CONSTRUCTOR 5 (offer + rating) ===== ← ADD THIS
    public Product(int id, String category, String tag, String name,
                   String price, String description, int imageRes,
                   boolean hasOffer, int discountPercent, String oldPrice,
                   double rating, int reviewCount) {

        this(id, category, tag, name, price, description, imageRes,
                hasOffer, discountPercent, oldPrice);

        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    // ===== GETTERS =====
    public int getId() { return id; }
    public String getCategory() { return category; }
    public String getTag() { return tag; }
    public String getName() { return name; }
    public String getPrice() { return price; }
    public String getDescription() { return description; }
    public int getImageRes() { return imageRes; }
    public List<Integer> getImagesList() { return imagesList; }
    public boolean hasOffer() { return hasOffer; }
    public int getDiscountPercent() { return discountPercent; }
    public String getOldPrice() { return oldPrice; }
    public double getRating() { return rating; }
    public int getReviewCount() { return reviewCount; }

    // ===== SETTERS =====
    public void setId(int id) { this.id = id; }
    public void setCategory(String category) { this.category = category; }
    public void setTag(String tag) { this.tag = tag; }
    public void setName(String name) { this.name = name; }
    public void setPrice(String price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setImagesList(List<Integer> imagesList) { this.imagesList = imagesList; }
    public void setRating(double rating) { this.rating = rating; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
}