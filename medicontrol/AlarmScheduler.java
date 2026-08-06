package com.example.medicontrol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AlarmScheduler {

    public static void programarAlarma(Context context, Medicamento medicamento) {
        if (medicamento == null || medicamento.getFecha() == null || medicamento.getHora() == null) {
            return;
        }

        try {
            // Unir la fecha (dd/MM/yyyy) y la hora (HH:mm)
            String fechaHoraStr = medicamento.getFecha() + " " + medicamento.getHora();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date fechaHoraDate = sdf.parse(fechaHoraStr);

            if (fechaHoraDate == null) return;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fechaHoraDate);

            // Si la fecha/hora ya transcurrió, no programar
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                Log.d("AlarmScheduler", "La hora ya pasó, no se agenda: " + fechaHoraStr);
                return;
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) return;

            Intent intent = new Intent(context, NotificationReceiver.class);
            intent.putExtra("NOMBRE_MEDICAMENTO", medicamento.getNombre());
            intent.putExtra("DOSIS", medicamento.getDosis());
            intent.putExtra("MEDICAMENTO_ID", medicamento.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medicamento.getId(), // ID único por medicamento
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // Verificar permisos de alarma exacta en Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }

            Log.d("AlarmScheduler", "Alarma programada exitosamente para: " + fechaHoraStr);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cancelarAlarma(Context context, int medicamentoId) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    medicamentoId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null && pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}