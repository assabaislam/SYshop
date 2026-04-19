package com.example.SYshop.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.SYshop.R;
import com.example.SYshop.utils.ProductImageLoader;
import com.example.SYshop.views.ZoomableImageView;

import java.util.List;

public class ZoomableImagePagerAdapter extends RecyclerView.Adapter<ZoomableImagePagerAdapter.ViewHolder> {

    private final Context context;
    private final List<Integer> imagesList;
    private final String imageUrl;
    private final int fallbackImageRes;

    public ZoomableImagePagerAdapter(Context context, List<Integer> imagesList, String imageUrl) {
        this.context = context;
        this.imagesList = imagesList;
        this.imageUrl = imageUrl == null ? "" : imageUrl.trim();
        this.fallbackImageRes = (imagesList != null && !imagesList.isEmpty())
                ? imagesList.get(0)
                : R.drawable.classic_watch1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_zoomable_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Object source = resolveImageSource(position);
        if (source instanceof String) {
            ProductImageLoader.loadFitCenter(holder.imageView, (String) source, fallbackImageRes);
        } else {
            Glide.with(context)
                    .load(source)
                    .fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .placeholder(fallbackImageRes)
                    .error(fallbackImageRes)
                    .into(holder.imageView);
        }
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ZoomableImageView imageView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.zoomableImageView);
        }
    }
}
