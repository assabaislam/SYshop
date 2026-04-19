package com.example.SYshop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public final class LanguageManager {

    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_AR = "ar";

    private static final String PREFS_NAME = "sy_shop_preferences";
    private static final String KEY_APP_LANGUAGE = "app_language";

    private LanguageManager() {
    }

    public static void setSavedLanguage(Context context, String languageCode) {
        getPreferences(context)
                .edit()
                .putString(KEY_APP_LANGUAGE, normalizeLanguage(languageCode))
                .apply();
    }

    public static String getSavedLanguage(Context context) {
        String savedLanguage = getPreferences(context).getString(KEY_APP_LANGUAGE, LANGUAGE_EN);
        return normalizeLanguage(savedLanguage);
    }

    public static Context wrap(Context context) {
        return updateResources(context, getSavedLanguage(context));
    }

    public static boolean isCurrentLanguage(Context context, String languageCode) {
        return normalizeLanguage(languageCode).equals(getSavedLanguage(context));
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String normalizeLanguage(String languageCode) {
        return LANGUAGE_AR.equalsIgnoreCase(languageCode) ? LANGUAGE_AR : LANGUAGE_EN;
    }

    private static Context updateResources(Context context, String languageCode) {
        Locale locale = new Locale(normalizeLanguage(languageCode));
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = new Configuration(resources.getConfiguration());
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(configuration);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
        return context;
    }
}
