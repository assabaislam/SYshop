package com.example.SYshop.adapters;

import android.content.res.ColorStateList;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.SYshop.R;
import com.example.SYshop.models.Product;
import com.example.SYshop.utils.ProductImageLoader;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OfferSliderAdapter extends RecyclerView.Adapter<OfferSliderAdapter.ViewHolder> {

    public interface OnOfferClickListener {
        void onOfferClick(Product product);
    }

    private final Context context;
    private final List<Product> offers;
    private final OnOfferClickListener listener;

    public OfferSliderAdapter(Context context, List<Product> offers, OnOfferClickListener listener) {
        this.context = context;
        this.offers = new ArrayList<>(offers);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_offer_banner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = offers.get(position);
        int fallbackRes = product.getPreferredLocalImageRes();
        int backgroundRes = resolveBannerBackground(product);
        int accentColor = resolveBannerAccentColor(product);
        int metaColor = ColorUtils.blendARGB(accentColor, ContextCompat.getColor(context, R.color.black_soft), 0.35f);
        int oldPriceColor = ColorUtils.setAlphaComponent(accentColor, 150);
        int badgeFillColor = ColorUtils.blendARGB(accentColor, ContextCompat.getColor(context, R.color.white), 0.82f);
        int badgeStrokeColor = ColorUtils.setAlphaComponent(accentColor, 110);

        holder.offerBadgeText.setText(buildBadgeText(product));
        holder.offerTitleText.setText(product.getName());
        holder.offerCategoryText.setText(buildCategoryText(product));
        holder.offerPriceText.setText(product.getPrice());
        holder.offerPriceText.setTextColor(accentColor);
        holder.offerCategoryText.setTextColor(metaColor);
        holder.offerOldPriceText.setTextColor(oldPriceColor);
        holder.shopNowButton.setBackgroundTintList(ColorStateList.valueOf(accentColor));
        holder.shopNowButton.setStrokeColor(ColorStateList.valueOf(ColorUtils.setAlphaComponent(accentColor, 70)));
        styleBadge(holder.offerBadgeText, accentColor, badgeFillColor, badgeStrokeColor);

        if (product.hasOffer() && !product.getOldPrice().trim().isEmpty()) {
            holder.offerOldPriceText.setVisibility(View.VISIBLE);
            holder.offerOldPriceText.setText(product.getOldPrice());
            holder.offerOldPriceText.setPaintFlags(
                    holder.offerOldPriceText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG
            );
        } else {
            holder.offerOldPriceText.setVisibility(View.GONE);
            holder.offerOldPriceText.setText("");
        }

        if (backgroundRes != 0) {
            Glide.with(context)
                    .load(backgroundRes)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .placeholder(backgroundRes)
                    .error(backgroundRes)
                    .into(holder.offerBackgroundImage);
        } else {
            ProductImageLoader.loadCenterCrop(holder.offerBackgroundImage, product.getImageUrl(), fallbackRes);
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onOfferClick(product);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        holder.shopNowButton.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    public void submitOffers(List<Product> products) {
        offers.clear();
        if (products != null) {
            offers.addAll(products);
        }
        notifyDataSetChanged();
    }

    private String buildBadgeText(Product product) {
        if (product.hasOffer() && product.getDiscountPercent() > 0) {
            return product.getDiscountPercent() + "% off";
        }

        String tag = product.getTag().trim();
        if (!tag.isEmpty()) {
            return tag;
        }

        return context.getString(R.string.special_offer);
    }

    private String buildCategoryText(Product product) {
        String category = product.getCategory().trim();
        if (category.isEmpty()) {
            return context.getString(R.string.special_offer);
        }
        return category + " collection";
    }

    private int resolveBannerBackground(Product product) {
        String safeName = product.getName().trim().toLowerCase();
        String safeCategory = product.getCategory().trim().toLowerCase();

        if (safeName.contains("leather bag") || safeCategory.equals("bags")) {
            return R.drawable.offer_banner_luxury;
        }

        if (safeName.contains("watch") || safeCategory.equals("watches")) {
            return R.drawable.offer_banner_watch;
        }

        if (safeName.contains("shoe") || safeName.contains("sneaker") || safeCategory.equals("shoes")) {
            return R.drawable.offer_banner_shoes;
        }

        return 0;
    }

    private int resolveBannerAccentColor(Product product) {
        String safeName = product.getName().trim().toLowerCase();
        String safeCategory = product.getCategory().trim().toLowerCase();

        if (safeName.contains("leather bag") || safeCategory.equals("bags")) {
            return ContextCompat.getColor(context, R.color.offer_bag_accent);
        }

        if (safeName.contains("watch") || safeCategory.equals("watches")) {
            return ContextCompat.getColor(context, R.color.offer_watch_accent);
        }

        if (safeName.contains("shoe") || safeName.contains("sneaker") || safeCategory.equals("shoes")) {
            return ContextCompat.getColor(context, R.color.offer_shoes_accent);
        }

        return ContextCompat.getColor(context, R.color.offer_default_accent);
    }

    private void styleBadge(TextView badgeView, int textColor, int fillColor, int strokeColor) {
        badgeView.setTextColor(textColor);

        Drawable background = badgeView.getBackground();
        if (!(background instanceof GradientDrawable)) {
            return;
        }

        GradientDrawable badgeDrawable = (GradientDrawable) background.mutate();
        badgeDrawable.setColor(fillColor);
        badgeDrawable.setStroke(dpToPx(1), strokeColor);
    }

    private int dpToPx(int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView offerBadgeText;
        final TextView offerTitleText;
        final TextView offerCategoryText;
        final TextView offerPriceText;
        final TextView offerOldPriceText;
        final ImageView offerBackgroundImage;
        final MaterialButton shopNowButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerBadgeText = itemView.findViewById(R.id.offerBadgeText);
            offerTitleText = itemView.findViewById(R.id.offerTitleText);
            offerCategoryText = itemView.findViewById(R.id.offerCategoryText);
            offerPriceText = itemView.findViewById(R.id.offerPriceText);
            offerOldPriceText = itemView.findViewById(R.id.offerOldPriceText);
            offerBackgroundImage = itemView.findViewById(R.id.offerBackgroundImage);
            shopNowButton = itemView.findViewById(R.id.shopNowButton);
        }
    }
}
