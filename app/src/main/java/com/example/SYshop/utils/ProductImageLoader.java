package com.example.SYshop.utils;

import android.net.Uri;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.SYshop.R;
import com.google.firebase.storage.FirebaseStorage;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ProductImageLoader {

    private static final Map<String, String> STORAGE_URL_CACHE = new ConcurrentHashMap<>();

    private ProductImageLoader() {
    }

    public static boolean isRemoteUrl(String rawUrl) {
        String safeUrl = normalize(rawUrl).toLowerCase(Locale.ROOT);
        return safeUrl.startsWith("http://")
                || safeUrl.startsWith("https://")
                || safeUrl.startsWith("gs://");
    }

    public static void loadCenterCrop(ImageView imageView, String imageUrl, @DrawableRes int fallbackRes) {
        load(imageView, imageUrl, fallbackRes, true);
    }

    public static void loadFitCenter(ImageView imageView, String imageUrl, @DrawableRes int fallbackRes) {
        load(imageView, imageUrl, fallbackRes, false);
    }

    private static void load(ImageView imageView, String imageUrl, @DrawableRes int fallbackRes, boolean centerCrop) {
        String normalizedUrl = normalize(imageUrl);
        int safeFallbackRes = fallbackRes != 0 ? fallbackRes : R.drawable.classic_watch1;

        imageView.setTag(R.id.tag_remote_image_request, normalizedUrl);

        if (!isRemoteUrl(normalizedUrl)) {
            loadLocal(imageView, safeFallbackRes, centerCrop);
            return;
        }

        if (normalizedUrl.startsWith("gs://")) {
            String cachedDownloadUrl = STORAGE_URL_CACHE.get(normalizedUrl);
            if (cachedDownloadUrl != null && !cachedDownloadUrl.trim().isEmpty()) {
                loadResolvedRemote(imageView, normalizedUrl, Uri.parse(cachedDownloadUrl), safeFallbackRes, centerCrop);
                return;
            }

            loadLocal(imageView, safeFallbackRes, centerCrop);
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(normalizedUrl)
                    .getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        STORAGE_URL_CACHE.put(normalizedUrl, uri.toString());
                        loadResolvedRemote(imageView, normalizedUrl, uri, safeFallbackRes, centerCrop);
                    })
                    .addOnFailureListener(ignored -> {
                        if (isSameRequest(imageView, normalizedUrl)) {
                            loadLocal(imageView, safeFallbackRes, centerCrop);
                        }
                    });
            return;
        }

        loadResolvedRemote(imageView, normalizedUrl, normalizedUrl, safeFallbackRes, centerCrop);
    }

    private static void loadResolvedRemote(ImageView imageView,
                                           String originalRequest,
                                           Object source,
                                           @DrawableRes int fallbackRes,
                                           boolean centerCrop) {
        if (!isSameRequest(imageView, originalRequest)) {
            return;
        }

        RequestBuilder<?> request = Glide.with(imageView)
                .load(source)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade())
                .placeholder(fallbackRes)
                .error(fallbackRes);

        if (centerCrop) {
            request.centerCrop();
        } else {
            request.fitCenter();
        }

        request.into(imageView);
    }

    private static void loadLocal(ImageView imageView, @DrawableRes int fallbackRes, boolean centerCrop) {
        RequestBuilder<?> request = Glide.with(imageView)
                .load(fallbackRes)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontAnimate()
                .placeholder(fallbackRes)
                .error(fallbackRes);

        if (centerCrop) {
            request.centerCrop();
        } else {
            request.fitCenter();
        }

        request.into(imageView);
    }

    private static boolean isSameRequest(ImageView imageView, String requestUrl) {
        Object currentTag = imageView.getTag(R.id.tag_remote_image_request);
        return currentTag instanceof String && requestUrl.equals(currentTag);
    }

    private static String normalize(String rawUrl) {
        return rawUrl == null ? "" : rawUrl.trim();
    }
}
