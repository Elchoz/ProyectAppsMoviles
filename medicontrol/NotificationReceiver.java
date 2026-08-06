package com.example.medicontrol;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String nombreMedicamento = intent.getStringExtra("NOMBRE_MEDICAMENTO");
        String dosis = intent.getStringExtra("DOSIS");
        int idMedicamento = intent.getIntExtra("MEDICAMENTO_ID", (int) System.currentTimeMillis());

        // 🟢 Intent para abrir la MainActivity
        Intent intentAbrirApp = new Intent(context, MainActivity.class);

        // Banderas para limpiar la pila de tareas y traer la app al frente limpia
        intentAbrirApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 🟢 PendingIntent asociando el clic en la notificación
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                idMedicamento, // ID único para evitar sobreescribir otros intents
                intentAbrirApp,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MediControlApp.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⏰ Hora de tomar tu medicamento")
                .setContentText((nombreMedicamento != null ? nombreMedicamento : "Medicamento") +
                        (dosis != null ? " - Dosis: " + dosis : ""))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true) // 🟢 Elimina la notificación automáticamente al hacerle clic
                .setContentIntent(pendingIntent); // 🟢 Asigna la acción de clic

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(idMedicamento, builder.build());
        }
    }
}