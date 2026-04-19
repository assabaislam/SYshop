package com.example.SYshop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.SYshop.R;
import com.example.SYshop.activities.ProductDetailsActivity;
import com.example.SYshop.models.Product;

public class Navigator {

    private static final String EXTRA_PRODUCT_ID = "product_id";
    private static final String EXTRA_PRODUCT_CATEGORY = "product_category";
    private static final String EXTRA_PRODUCT_TAG = "product_tag";
    private static final String EXTRA_PRODUCT_NAME = "product_name";
    private static final String EXTRA_PRODUCT_PRICE = "product_price";
    private static final String EXTRA_PRODUCT_DESCRIPTION = "product_description";
    private static final String EXTRA_PRODUCT_IMAGE_RES = "product_image_res";
    private static final String EXTRA_PRODUCT_IMAGE_NAME = "product_image_name";
    private static final String EXTRA_PRODUCT_IMAGE_URL = "product_image_url";
    private static final String EXTRA_PRODUCT_RATING = "product_rating";
    private static final String EXTRA_PRODUCT_REVIEW_COUNT = "product_review_count";

    public static void openProductDetails(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailsActivity.class);
        intent.putExtra(EXTRA_PRODUCT_ID, product.getId());
        intent.putExtra(EXTRA_PRODUCT_CATEGORY, product.getCategory());
        intent.putExtra(EXTRA_PRODUCT_TAG, product.getTag());
        intent.putExtra(EXTRA_PRODUCT_NAME, product.getName());
        intent.putExtra(EXTRA_PRODUCT_PRICE, product.getPrice());
        intent.putExtra(EXTRA_PRODUCT_DESCRIPTION, product.getDescription());
        intent.putExtra(EXTRA_PRODUCT_IMAGE_RES, product.getPreferredLocalImageRes());
        intent.putExtra(EXTRA_PRODUCT_IMAGE_NAME, product.getPreferredLocalImageName());
        intent.putExtra(EXTRA_PRODUCT_IMAGE_URL, product.getImageUrl());
        intent.putExtra(EXTRA_PRODUCT_RATING, product.getRating());
        intent.putExtra(EXTRA_PRODUCT_REVIEW_COUNT, product.getReviewCount());
        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
