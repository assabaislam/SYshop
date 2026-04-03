package com.example.SYshop.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.example.SYshop.R;
import com.example.SYshop.activities.ProductDetailsActivity;
import com.example.SYshop.models.Product;

import java.util.ArrayList;

public class Navigator {

    public static void openProductDetails(Context context, Product product) {
        Intent intent = new Intent(context, ProductDetailsActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("category", product.getCategory());
        intent.putExtra("tag", product.getTag());
        intent.putExtra("name", product.getName());
        intent.putExtra("price", product.getPrice());
        intent.putExtra("description", product.getDescription());
        intent.putExtra("image", product.getImageRes());
        
        if (product.getImagesList() != null) {
            intent.putIntegerArrayListExtra("images_list", new ArrayList<>(product.getImagesList()));
        }
        intent.putExtra("rating", product.getRating());
        intent.putExtra("reviews_count", product.getReviewCount());

        context.startActivity(intent);

        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}