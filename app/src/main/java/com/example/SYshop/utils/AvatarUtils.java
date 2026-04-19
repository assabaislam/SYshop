package com.example.SYshop.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.SYshop.R;

public final class AvatarUtils {

    public static final String PRESET_USER = "user";
    public static final String PRESET_MAN = "man";
    public static final String PRESET_WOMAN = "woman";
    public static final String PRESET_CUSTOM = "custom";

    private AvatarUtils() {
    }

    public static void bindAvatar(ImageView imageView, TextView initialsView, String fullName, String avatarUrl, String avatarPreset) {
        if (initialsView != null) {
            initialsView.setText(getInitials(fullName));
            initialsView.setVisibility(View.VISIBLE);
        }

        if (imageView == null) {
            return;
        }

        String safePreset = normalizePreset(avatarPreset, avatarUrl);
        int presetResId = resolvePresetAvatarRes(imageView.getContext(), safePreset);

        if (presetResId != 0) {
            if (initialsView != null) {
                initialsView.setVisibility(View.INVISIBLE);
            }
            imageView.setVisibility(View.VISIBLE);
            Glide.with(imageView.getContext())
                    .load(presetResId)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
            return;
        }

        if (hasRemoteUrl(avatarUrl)) {
            imageView.setVisibility(View.VISIBLE);
            Glide.with(imageView.getContext())
                    .load(avatarUrl)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            imageView.setVisibility(View.INVISIBLE);
                            if (initialsView != null) {
                                initialsView.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            if (initialsView != null) {
                                initialsView.setVisibility(View.INVISIBLE);
                            }
                            imageView.setVisibility(View.VISIBLE);
                            return false;
                        }
                    })
                    .into(imageView);
            return;
        }

        Glide.with(imageView.getContext()).clear(imageView);
        imageView.setImageDrawable(null);
        imageView.setVisibility(View.INVISIBLE);
    }

    public static boolean hasRemoteUrl(String avatarUrl) {
        if (avatarUrl == null) {
            return false;
        }

        String safeUrl = avatarUrl.trim().toLowerCase();
        return safeUrl.startsWith("http://") || safeUrl.startsWith("https://");
    }

    public static String normalizePreset(String avatarPreset, String avatarUrl) {
        if (avatarPreset != null && !avatarPreset.trim().isEmpty()) {
            return avatarPreset.trim().toLowerCase();
        }
        return hasRemoteUrl(avatarUrl) ? PRESET_CUSTOM : PRESET_USER;
    }

    public static int resolvePresetAvatarRes(Context context, String avatarPreset) {
        if (context == null) {
            return 0;
        }

        String drawableName;
        switch (normalizePreset(avatarPreset, "")) {
            case PRESET_MAN:
                drawableName = "avatar_man";
                break;
            case PRESET_WOMAN:
                drawableName = "avatar_woman";
                break;
            case PRESET_USER:
                drawableName = "avatar_user";
                break;
            default:
                return 0;
        }

        return context.getResources().getIdentifier(drawableName, "drawable", context.getPackageName());
    }

    public static String getInitials(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }

        String[] parts = fullName.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
            if (builder.length() == 2) {
                break;
            }
        }

        return builder.length() == 0 ? "U" : builder.toString();
    }
}
