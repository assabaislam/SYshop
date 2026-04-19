package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.R;
import com.example.SYshop.utils.ProductImageLoader;

import java.util.List;

public class ProductImagePagerAdapter extends RecyclerView.Adapter<ProductImagePagerAdapter.ViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(int position);
    }

    private final Context context;
    private final List<Integer> imagesList;
    private final String imageUrl;
    private final int fallbackImageRes;
    private final OnImageClickListener onImageClickListener;

    public ProductImagePagerAdapter(Context context, List<Integer> imagesList) {
        this(context, imagesList, "", null);
    }

    public ProductImagePagerAdapter(Context context, List<Integer> imagesList, String imageUrl) {
        this(context, imagesList, imageUrl, null);
    }

    public ProductImagePagerAdapter(Context context, List<Integer> imagesList, String imageUrl, OnImageClickListener onImageClickListener) {
        this.context = context;
        this.imagesList = imagesList;
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
        this.fallbackImageRes = (imagesList != null && !imagesList.isEmpty())
                ? imagesList.get(0)
                : R.drawable.classic_watch1;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_full_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object source = resolveImageSource(position);
        if (source instanceof String) {
            ProductImageLoader.loadCenterCrop(holder.imageView, (String) source, fallbackImageRes);
        } else {
            Glide.with(context)
                    .load(source)
                    .centerCrop()
                    .override(1080, 1080)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(fallbackImageRes)
                    .error(fallbackImageRes)
                    .into(holder.imageView);
        }

        holder.imageView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onImageClick(holder.getBindingAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        if (ProductImageLoader.isRemoteUrl(imageUrl)) {
            return 1;
        }
        return imagesList != null ? imagesList.size() : 0;
    }

    private Object resolveImageSource(int position) {
        if (ProductImageLoader.isRemoteUrl(imageUrl)) {
            return imageUrl;
        }
        return imagesList.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgFullProduct);
        }
    }
}
