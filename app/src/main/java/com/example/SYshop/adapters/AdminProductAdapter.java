package com.example.SYshop.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.SYshop.R;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.ProductImageLoader;

import java.util.List;
import java.util.Locale;

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.ViewHolder> {

    public interface OnAdminProductActionListener {
        void onEditProduct(Product product);
        void onDeleteProduct(Product product);
    }

    private final Context context;
    private final List<Product> products;
    private final OnAdminProductActionListener listener;

    public AdminProductAdapter(Context context, List<Product> products, OnAdminProductActionListener listener) {
        this.context = context;
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);

        holder.productName.setText(product.getName());
        holder.productMeta.setText(buildMeta(product));
        holder.productPrice.setText(product.getPrice());

        if (product.hasOffer() && !product.getOldPrice().trim().isEmpty()) {
            holder.productOldPrice.setVisibility(View.VISIBLE);
            holder.productOldPrice.setText(product.getOldPrice());
            holder.productOldPrice.setPaintFlags(holder.productOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.productOldPrice.setVisibility(View.GONE);
        }

        ProductImageLoader.loadCenterCrop(
                holder.productImage,
                product.getImageUrl(),
                product.getPreferredLocalImageRes()
        );

        holder.editText.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditProduct(product);
            }
        });

        holder.deleteText.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteProduct(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String buildMeta(Product product) {
        String category = getLocalizedCategory(product.getCategory());
        String tag = getLocalizedTag(product.getTag());

        if (tag.isEmpty()) {
            return category;
        }

        return category + " / " + tag;
    }

    private String getLocalizedCategory(String category) {
        if (category == null) {
            return "";
        }

        switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "shoes":
                return context.getString(R.string.category_shoes);
            case "watches":
                return context.getString(R.string.category_watches);
            case "bags":
                return context.getString(R.string.category_bags);
            case "audio":
                return context.getString(R.string.category_audio);
            default:
                return category;
        }
    }

    private String getLocalizedTag(String tag) {
        if (tag == null || tag.trim().isEmpty()) {
            return "";
        }

        switch (tag.trim().toLowerCase(Locale.ROOT)) {
            case "promo":
                return context.getString(R.string.product_tag_promo);
            case "new":
                return context.getString(R.string.product_tag_new);
            case "best seller":
                return context.getString(R.string.product_tag_best_seller);
            case "top rated":
                return context.getString(R.string.product_tag_top_rated);
            case "sale":
                return context.getString(R.string.product_tag_sale);
            default:
                return tag;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName;
        TextView productMeta;
        TextView productPrice;
        TextView productOldPrice;
        TextView editText;
        TextView deleteText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productMeta = itemView.findViewById(R.id.productMeta);
            productPrice = itemView.findViewById(R.id.productPrice);
            productOldPrice = itemView.findViewById(R.id.productOldPrice);
            editText = itemView.findViewById(R.id.editText);
            deleteText = itemView.findViewById(R.id.deleteText);
        }
    }
}
