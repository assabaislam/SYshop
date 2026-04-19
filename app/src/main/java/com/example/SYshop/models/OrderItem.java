package com.example.SYshop.models;

import com.example.SYshop.R;
import com.example.SYshop.utils.ProductImageLoader;

public class OrderItem {

    private int productId;
    private String category;
    private String tag;
    private String name;
    private String price;
    private String description;
    private int imageRes;
    private String imageUrl;
    private double rating;
    private int reviewCount;
    private int quantity;

    public OrderItem(int productId, String category, String tag, String name,
                     String price, String description, int imageRes,
                     double rating, int reviewCount, int quantity) {
        this(productId, category, tag, name, price, description, imageRes, "", rating, reviewCount, quantity);
    }

    public OrderItem(int productId, String category, String tag, String name,
                     String price, String description, int imageRes, String imageUrl,
                     double rating, int reviewCount, int quantity) {
        this.productId = productId;
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageRes = imageRes;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public String getCategory() {
        return category;
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

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public int getPreferredLocalImageRes() {
        String safeName = getName() == null ? "" : getName().trim().toLowerCase();
        String safeCategory = getCategory() == null ? "" : getCategory().trim().toLowerCase();

        if (safeName.contains("running")) {
            return R.drawable.running_shoes;
        }
        if (safeName.contains("sneaker") || safeName.contains("urban")) {
            return R.drawable.sneakers_urban;
        }
        if (safeName.contains("smart watch")) {
            return R.drawable.smart_watch_pro;
        }
        if (safeName.contains("classic watch")) {
            return R.drawable.classic_watch1;
        }
        if (safeName.contains("leather bag")) {
            return R.drawable.leather_bag;
        }
        if (safeName.contains("hand made") || safeName.contains("handmade") || safeName.contains("hayat")) {
            return R.drawable.hayat_sac;
        }
        if (safeName.contains("speaker")) {
            return R.drawable.mini_speaker;
        }
        if (safeName.contains("headset")) {
            return R.drawable.wireless_headset;
        }

        if ("shoes".equals(safeCategory)) {
            return R.drawable.running_shoes;
        }
        if ("watches".equals(safeCategory)) {
            return R.drawable.classic_watch1;
        }
        if ("bags".equals(safeCategory)) {
            return R.drawable.leather_bag;
        }
        if ("audio".equals(safeCategory)) {
            return R.drawable.wireless_headset;
        }

        return imageRes != 0 ? imageRes : R.drawable.classic_watch1;
    }

    public void setImageData(int imageRes, String imageUrl) {
        this.imageRes = imageRes;
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean hasRemoteImageUrl() {
        return ProductImageLoader.isRemoteUrl(getImageUrl());
    }
}
