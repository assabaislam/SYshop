package com.example.SYshop.models;

public class CachedProduct {

    private int id;
    private String documentId;
    private String category;
    private String tag;
    private String name;
    private String price;
    private String description;
    private int imageRes;
    private String imageName;
    private String imageUrl;
    private double rating;
    private int reviewCount;

    public CachedProduct(int id, String documentId, String category, String tag, String name,
                         String price, String description, int imageRes, String imageName, String imageUrl,
                         double rating, int reviewCount) {
        this.id = id;
        this.documentId = documentId;
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageRes = imageRes;
        this.imageName = imageName;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public int getImageRes() {
        return imageRes;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }
}
