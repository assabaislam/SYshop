package com.example.SYshop.models;

import com.example.SYshop.R;
import com.example.SYshop.utils.ProductImageLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {

    private int id;
    private String documentId;
    private String imageName;
    private String category;
    private String tag;
    private String name;
    private String price;
    private String description;

    private int imageRes;
    private List<Integer> imagesList;

    private String imageUrl;

    private boolean hasOffer;
    private int discountPercent;
    private String oldPrice;

    private double rating;
    private int reviewCount;

    public Product(int id,
                   String category,
                   String tag,
                   String name,
                   String price,
                   String description,
                   int imageRes) {
        this(id, category, tag, name, price, description, imageRes, false, 0, "", 4.5, 0);
    }

    public Product(int id,
                   String category,
                   String tag,
                   String name,
                   String price,
                   String description,
                   int imageRes,
                   double rating,
                   int reviewCount) {
        this(id, category, tag, name, price, description, imageRes, false, 0, "", rating, reviewCount);
    }

    public Product(int id,
                   String category,
                   String tag,
                   String name,
                   String price,
                   String description,
                   int imageRes,
                   boolean hasOffer,
                   int discountPercent,
                   String oldPrice,
                   double rating,
                   int reviewCount) {
        this.id = id;
        this.documentId = "";
        this.imageName = getImageNameFromRes(imageRes);
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageRes = imageRes;
        this.imagesList = new ArrayList<>();
        this.imagesList.add(imageRes);
        this.imageUrl = "";
        this.hasOffer = hasOffer;
        this.discountPercent = discountPercent;
        this.oldPrice = oldPrice;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public Product(int id,
                   String category,
                   String tag,
                   String name,
                   String price,
                   String description,
                   List<Integer> imagesList) {
        this(id, category, tag, name, price, description, imagesList, 4.5, 0);
    }

    public Product(int id,
                   String category,
                   String tag,
                   String name,
                   String price,
                   String description,
                   List<Integer> imagesList,
                   double rating,
                   int reviewCount) {
        this.id = id;
        this.documentId = "";
        this.imageName = "";
        this.category = category;
        this.tag = tag;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imagesList = imagesList != null ? new ArrayList<>(imagesList) : new ArrayList<>();
        this.imageRes = this.imagesList.isEmpty() ? 0 : this.imagesList.get(0);
        this.imageName = getImageNameFromRes(this.imageRes);
        this.imageUrl = "";
        this.hasOffer = false;
        this.discountPercent = 0;
        this.oldPrice = "";
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category == null ? "" : category;
    }

    public String getDocumentId() {
        return documentId == null ? "" : documentId;
    }

    public String getImageName() {
        return imageName == null ? "" : imageName;
    }

    public String getTag() {
        return tag == null ? "" : tag;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public String getPrice() {
        return price == null ? "" : price;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public int getImageRes() {
        return imageRes;
    }

    public List<Integer> getImagesList() {
        return imagesList == null ? new ArrayList<>() : imagesList;
    }

    public String getImageUrl() {
        return imageUrl == null ? "" : imageUrl;
    }

    public int getPreferredLocalImageRes() {
        String preferredImageName = getPreferredLocalImageName();
        if (!preferredImageName.isEmpty()) {
            return getImageFromName(preferredImageName);
        }

        String derivedImageName = deriveImageNameFromContent();
        if (!derivedImageName.isEmpty()) {
            return getImageFromName(derivedImageName);
        }

        return imageRes != 0 ? imageRes : R.drawable.classic_watch1;
    }

    public String getPreferredLocalImageName() {
        String derivedImageName = deriveImageNameFromContent();
        if (hasRemoteImageUrl() && !derivedImageName.isEmpty()) {
            return derivedImageName;
        }

        String safeImageName = getImageName().trim();
        if (!safeImageName.isEmpty()) {
            return safeImageName;
        }

        if (!derivedImageName.isEmpty()) {
            return derivedImageName;
        }

        return getImageNameFromRes(imageRes);
    }

    public boolean hasRemoteImageUrl() {
        return ProductImageLoader.isRemoteUrl(getImageUrl());
    }

    public boolean hasOffer() {
        return hasOffer;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getOldPrice() {
        return oldPrice == null ? "" : oldPrice;
    }

    public double getRating() {
        return rating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
        if (getImageName().trim().isEmpty()) {
            this.imageName = getImageNameFromRes(imageRes);
        }
        if (this.imagesList == null) {
            this.imagesList = new ArrayList<>();
        }
        if (this.imagesList.isEmpty()) {
            this.imagesList.add(imageRes);
        } else {
            this.imagesList.set(0, imageRes);
        }
    }

    public void setImagesList(List<Integer> imagesList) {
        this.imagesList = imagesList != null ? new ArrayList<>(imagesList) : new ArrayList<>();
        this.imageRes = this.imagesList.isEmpty() ? 0 : this.imagesList.get(0);
        if (getImageName().trim().isEmpty()) {
            this.imageName = getImageNameFromRes(this.imageRes);
        }
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
    }

    public void setHasOffer(boolean hasOffer) {
        this.hasOffer = hasOffer;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public void setOldPrice(String oldPrice) {
        this.oldPrice = oldPrice;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Map<String, Object> toStorageMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("category", getCategory());
        data.put("tag", getTag());
        data.put("name", getName());
        data.put("price", getPrice());
        data.put("description", getDescription());
        data.put("image_res", getImageRes());
        data.put("image", getPreferredLocalImageName());
        data.put("imageUrl", getImageUrl());
        data.put("hasOffer", hasOffer());
        data.put("discountPercent", getDiscountPercent());
        data.put("oldPrice", getOldPrice());
        data.put("rating", getRating());
        data.put("review_count", getReviewCount());
        return data;
    }

    public static Product fromMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }

        int id = getIntValue(map.get("id"));
        String category = getStringValue(map.get("category"));
        String tag = getStringValue(map.get("tag"));
        String name = getStringValue(map.get("name"));
        String price = getStringValue(map.get("price"));
        String description = getStringValue(map.get("description"));
        double rating = getDoubleValue(map.get("rating"));
        int reviewCount = getIntValue(firstNonNull(map.get("reviewCount"), map.get("review_count")));
        boolean hasOffer = getBooleanValue(map.get("hasOffer"));
        int discountPercent = getIntValue(map.get("discountPercent"));
        String oldPrice = getStringValue(map.get("oldPrice"));
        String imageUrl = getStringValue(firstNonNull(map.get("imageUrl"), map.get("image_url")));
        String imageName = getStringValue(firstNonNull(map.get("image"), map.get("image_name")));

        List<Integer> images = parseImages(map.get("images"));
        Product product;

        if (!images.isEmpty()) {
            product = new Product(
                    id,
                    category,
                    tag,
                    name,
                    price,
                    description,
                    images,
                    rating,
                    reviewCount
            );
            product.setHasOffer(hasOffer);
            product.setDiscountPercent(discountPercent);
            product.setOldPrice(oldPrice);
        } else {
            int imageRes = parseImageRes(map);
            product = new Product(
                    id,
                    category,
                    tag,
                    name,
                    price,
                    description,
                    imageRes,
                    hasOffer,
                    discountPercent,
                    oldPrice,
                    rating,
                    reviewCount
            );
        }

        product.setImageUrl(imageUrl);
        product.setDocumentId(getStringValue(firstNonNull(map.get("documentId"), map.get("document_id"))));
        product.setImageName(imageName);
        return product;
    }

    public static int getImageFromName(String imageName) {
        if (imageName == null) {
            return R.drawable.classic_watch1;
        }

        switch (imageName) {
            case "sneakers_urban":
                return R.drawable.sneakers_urban;
            case "running_shoes":
                return R.drawable.running_shoes;
            case "smart_watch_pro":
                return R.drawable.smart_watch_pro;
            case "leather_bag":
                return R.drawable.leather_bag;
            case "wireless_headset":
                return R.drawable.wireless_headset;
            case "mini_speaker":
                return R.drawable.mini_speaker;
            case "classic_watch1":
                return R.drawable.classic_watch1;
            case "classic_watch2":
                return R.drawable.classic_watch2;
            case "classic_watch3":
                return R.drawable.classic_watch3;
            case "hayat_sac":
                return R.drawable.hayat_sac;
            default:
                return R.drawable.classic_watch1;
        }
    }

    public static List<Integer> getImagesListFromNames(List<String> imageNames) {
        List<Integer> result = new ArrayList<>();
        if (imageNames == null || imageNames.isEmpty()) {
            result.add(R.drawable.classic_watch1);
            return result;
        }

        for (String imageName : imageNames) {
            result.add(getImageFromName(imageName));
        }

        return result;
    }

    public static List<Integer> getImagesListFromName(String imageName) {
        return new ArrayList<>(Arrays.asList(getImageFromName(imageName)));
    }

    private static List<Integer> parseImages(Object imagesValue) {
        List<Integer> result = new ArrayList<>();

        if (!(imagesValue instanceof List)) {
            return result;
        }

        for (Object imageItem : (List<?>) imagesValue) {
            if (imageItem instanceof Number) {
                result.add(((Number) imageItem).intValue());
            } else {
                result.add(getImageFromName(String.valueOf(imageItem)));
            }
        }

        return result;
    }

    private static int parseImageRes(Map<String, Object> map) {
        String imageName = getStringValue(firstNonNull(map.get("image"), map.get("image_name")));
        if (!imageName.isEmpty()) {
            return getImageFromName(imageName);
        }

        Object imageResValue = map.get("image_res");
        if (imageResValue instanceof Number) {
            return ((Number) imageResValue).intValue();
        }
        return R.drawable.classic_watch1;
    }

    private static Object firstNonNull(Object first, Object second) {
        return first != null ? first : second;
    }

    private static String getStringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int getIntValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            return 0;
        }
    }

    public static String getImageNameFromRes(int imageRes) {
        if (imageRes == R.drawable.sneakers_urban) {
            return "sneakers_urban";
        }
        if (imageRes == R.drawable.running_shoes) {
            return "running_shoes";
        }
        if (imageRes == R.drawable.smart_watch_pro) {
            return "smart_watch_pro";
        }
        if (imageRes == R.drawable.leather_bag) {
            return "leather_bag";
        }
        if (imageRes == R.drawable.wireless_headset) {
            return "wireless_headset";
        }
        if (imageRes == R.drawable.mini_speaker) {
            return "mini_speaker";
        }
        if (imageRes == R.drawable.classic_watch1) {
            return "classic_watch1";
        }
        if (imageRes == R.drawable.classic_watch2) {
            return "classic_watch2";
        }
        if (imageRes == R.drawable.classic_watch3) {
            return "classic_watch3";
        }
        if (imageRes == R.drawable.hayat_sac) {
            return "hayat_sac";
        }
        return "";
    }

    private static double getDoubleValue(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static boolean getBooleanValue(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        return "true".equalsIgnoreCase(String.valueOf(value));
    }

    private String deriveImageNameFromContent() {
        String safeName = getName().trim().toLowerCase();
        String safeCategory = getCategory().trim().toLowerCase();

        if (safeName.contains("running")) {
            return "running_shoes";
        }
        if (safeName.contains("sneaker") || safeName.contains("urban")) {
            return "sneakers_urban";
        }
        if (safeName.contains("smart watch")) {
            return "smart_watch_pro";
        }
        if (safeName.contains("classic watch")) {
            return "classic_watch1";
        }
        if (safeName.contains("leather bag")) {
            return "leather_bag";
        }
        if (safeName.contains("hand made") || safeName.contains("handmade") || safeName.contains("hayat")) {
            return "hayat_sac";
        }
        if (safeName.contains("speaker")) {
            return "mini_speaker";
        }
        if (safeName.contains("headset")) {
            return "wireless_headset";
        }

        if ("shoes".equals(safeCategory)) {
            return "running_shoes";
        }
        if ("watches".equals(safeCategory)) {
            return "classic_watch1";
        }
        if ("bags".equals(safeCategory)) {
            return "leather_bag";
        }
        if ("audio".equals(safeCategory)) {
            return "wireless_headset";
        }

        return "";
    }
}
