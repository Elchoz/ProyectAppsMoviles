package com.example.medicontrol;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class ThemeHelper {

    private static final String PREF_NAME = "theme_pref";
    private static final String KEY_DARK_MODE = "is_dark_mode";

    // Método que está llamando MediControlApp
    public static void aplicarTema(Context context) {
        boolean isDarkMode = obtenerPreferenciaTema(context);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    // Alias por si lo llamas desde MainActivity u otros sitios
    public static void aplicarTemaGuardado(Context context) {
        aplicarTema(context);
    }

    public static boolean obtenerPreferenciaTema(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    public static void guardarTema(Context context, boolean isDarkMode) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_DARK_MODE, isDarkMode).apply();
        aplicarTema(context);
    }
}