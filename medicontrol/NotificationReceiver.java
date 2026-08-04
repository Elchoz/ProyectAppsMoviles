package com.example.medicontrol;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "MEDICONTROL_CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String nombre = intent.getStringExtra("NOMBRE_MEDICAMENTO");
        if (nombre == null || nombre.isEmpty()) {
            nombre = "tu medicamento";
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // 1. Crear canal de notificación para Android 8.0+ (API 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Recordatorios de Medicamentos",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para alarmas y avisos de MediControl");
            channel.enableVibration(true);
            channel.enableLights(true);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // 2. Usar un icono genérico seguro por si R.mipmap.ic_launcher falla en la notificación
        int iconoNotificacion = android.R.drawable.ic_lock_idle_alarm;

        // 3. Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(iconoNotificacion)
                .setContentTitle("⏰ Hora de tomar tu medicamento")
                .setContentText("Es momento de tomar: " + nombre)
                .setPriority(NotificationCompat.PRIORITY_MAX) // Prioridad Máxima para salir sobre otras apps
                .setCategory(NotificationCompat.CATEGORY_ALARM) // Categoría Alarma
                .setDefaults(NotificationCompat.DEFAULT_ALL)   // Sonido y vibración por defecto
                .setVibrate(new long[]{0, 500, 200, 500})       // Patrón de vibración explícito
                .setAutoCancel(true);

        // 4. Disparar la notificación con un ID único
        if (manager != null) {
            int notificationId = (int) System.currentTimeMillis();
            manager.notify(notificationId, builder.build());
        }
    }
}
