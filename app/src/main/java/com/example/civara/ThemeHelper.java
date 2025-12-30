package com.example.civara;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {
    private static final String PREF_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    public static void applyTheme(Context context, int mode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_THEME, mode).apply();
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void loadTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int mode = prefs.getInt(KEY_THEME, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}