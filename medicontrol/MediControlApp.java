package com.example.medicontrol;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MediControlApp extends Application {

    public static final String CHANNEL_ID = "medicontrol_notifications";

    @Override
    public void onCreate() {
        super.onCreate();
        crearCanalDeNotificaciones();
    }

    private void crearCanalDeNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Recordatorios de Medicamentos";
            String descripcion = "Canal para las notificaciones de toma de medicamentos";
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            channel.setDescription(descripcion);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}