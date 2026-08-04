package com.example.medicontrol;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String PREFS_NAME = "medicontrol_theme_prefs";
    private static final String KEY_DARK_MODE = "is_dark_mode";

    public static void aplicarTema(Context context) {
        if (context == null) return;
        boolean isDarkMode = obtenerPreferenciaTema(context);
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static void guardarTema(Context context, boolean isDarkMode) {
        if (context == null) return;

        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();

        // Esto cambia el tema en toda la app inmediatamente
        AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    public static boolean obtenerPreferenciaTema(Context context) {
        if (context == null) return false;
        SharedPreferences prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }
}