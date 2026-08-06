package com.example.medicontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            Log.d("MEDICONTROL_BOOT", "Reagendando alarmas tras reinicio del sistema...");

            Executors.newSingleThreadExecutor().execute(() -> {
                List<Medicamento> activos = AppDatabase.getInstance(context)
                        .medicamentoDao()
                        .obtenerActivos();

                if (activos != null) {
                    for (Medicamento med : activos) {
                        // 🟢 Programa la alarma real en el AlarmManager
                        AlarmScheduler.programarAlarma(context, med);
                    }
                }
            });
        }
    }
}