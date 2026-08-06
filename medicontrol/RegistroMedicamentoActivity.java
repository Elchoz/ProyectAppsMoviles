package com.example.medicontrol;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;

public class RegistroMedicamentoActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 101;
    public static final String CHANNEL_ID = "MEDICONTROL_CHANNEL";

    private EditText etNombre, etDosis, etFecha, etHora;
    private Spinner spPresentacion;
    private Button btnGuardar;

    private int medicamentoId = -1; // -1 = Nuevo registro
    private Calendar calendarioSeleccionado = Calendar.getInstance();

    private DatePickerDialog datePickerDialog;
    private TimePickerDialog timePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicamento);

        // 0. Crear el canal de notificaciones
        crearCanalNotificaciones();

        // 1. Vincular vistas
        etNombre = findViewById(R.id.etNombre);
        etDosis = findViewById(R.id.etDosis);
        spPresentacion = findViewById(R.id.spPresentacion);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);
        btnGuardar = findViewById(R.id.btnGuardar);

        // 2. Cargar adaptador del Spinner
        if (spPresentacion.getAdapter() == null) {
            String[] opciones = {"Tableta", "Cápsula", "Jarabe", "Inyección", "Gotas", "Otro"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, opciones);
            spPresentacion.setAdapter(adapter);
        }

        // 3. Modals para seleccionar fecha y hora
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());

        // 4. Cargar datos si venimos de "Editar"
        cargarDatosIntent();

        // 5. Permisos Android 13+
        solicitarPermisoNotificaciones();

        // 6. Botón Guardar
        btnGuardar.setOnClickListener(v -> guardarYProgramar());
    }

    private void crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Recordatorio de Medicamentos";
            String descripcion = "Canal para alarmas y notificaciones de MediControl";
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, nombre, importancia);
            channel.setDescription(descripcion);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void solicitarPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS
                );
            }
        }
    }

    private void cargarDatosIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        if (intent.hasExtra("MEDICAMENTO_ID") || intent.hasExtra("ID")) {
            if (intent.hasExtra("MEDICAMENTO_ID")) {
                medicamentoId = intent.getIntExtra("MEDICAMENTO_ID", -1);
            } else {
                medicamentoId = intent.getIntExtra("ID", -1);
            }

            if (intent.hasExtra("NOMBRE")) etNombre.setText(intent.getStringExtra("NOMBRE"));
            if (intent.hasExtra("DOSIS")) etDosis.setText(intent.getStringExtra("DOSIS"));
            if (intent.hasExtra("FECHA")) etFecha.setText(intent.getStringExtra("FECHA"));
            if (intent.hasExtra("HORA")) etHora.setText(intent.getStringExtra("HORA"));

            String presentacion = intent.getStringExtra("PRESENTACION");
            if (presentacion != null && spPresentacion != null && spPresentacion.getAdapter() != null) {
                for (int i = 0; i < spPresentacion.getCount(); i++) {
                    Object item = spPresentacion.getItemAtPosition(i);
                    if (item != null && item.toString().trim().equalsIgnoreCase(presentacion.trim())) {
                        spPresentacion.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    private void mostrarDatePicker() {
        int anio = calendarioSeleccionado.get(Calendar.YEAR);
        int mes = calendarioSeleccionado.get(Calendar.MONTH);
        int dia = calendarioSeleccionado.get(Calendar.DAY_OF_MONTH);

        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }

        datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendarioSeleccionado.set(Calendar.YEAR, year);
            calendarioSeleccionado.set(Calendar.MONTH, month);
            calendarioSeleccionado.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            etFecha.setText(sdf.format(calendarioSeleccionado.getTime()));
        }, anio, mes, dia);

        datePickerDialog.show();
    }

    private void mostrarTimePicker() {
        int hora = calendarioSeleccionado.get(Calendar.HOUR_OF_DAY);
        int minuto = calendarioSeleccionado.get(Calendar.MINUTE);

        if (timePickerDialog != null && timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }

        timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendarioSeleccionado.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarioSeleccionado.set(Calendar.MINUTE, minute);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            etHora.setText(sdf.format(calendarioSeleccionado.getTime()));
        }, hora, minuto, true);

        timePickerDialog.show();
    }

    private void guardarYProgramar() {
        String nombre = etNombre.getText().toString().trim();
        String dosis = etDosis.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();
        String presentacion = spPresentacion.getSelectedItem() != null ?
                spPresentacion.getSelectedItem().toString() : "Otro";

        if (nombre.isEmpty() || dosis.isEmpty() || fecha.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            int idFinal;
            if (medicamentoId != -1) {
                Medicamento med = AppDatabase.getInstance(this).medicamentoDao().obtenerPorId(medicamentoId);
                if (med != null) {
                    med.setNombre(nombre);
                    med.setDosis(dosis);
                    med.setPresentacion(presentacion);
                    med.setFecha(fecha);
                    med.setHora(hora);
                    AppDatabase.getInstance(this).medicamentoDao().actualizar(med);
                }
                idFinal = medicamentoId;
            } else {
                Medicamento med = new Medicamento(nombre, dosis, presentacion, fecha, hora, "ACTIVO");
                long nuevoId = AppDatabase.getInstance(this).medicamentoDao().insertar(med);
                idFinal = (int) nuevoId;
            }

            runOnUiThread(() -> {
                boolean exito = programarAlarma(idFinal, nombre, dosis, fecha, hora);
                if (exito) {
                    Toast.makeText(RegistroMedicamentoActivity.this, "Medicamento guardado y alarma programada", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }

    private boolean programarAlarma(int id, String nombre, String dosis, String fechaStr, String horaStr) {
        Log.d("MEDICONTROL", "Iniciando programarAlarma() para: " + nombre + " a las " + fechaStr + " " + horaStr);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Permiso de alarmas exactas para Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Log.e("MEDICONTROL", "La app NO tiene permiso para agendar alarmas exactas.");
                Intent intentPermiso = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentPermiso);
                Toast.makeText(this, "Concede el permiso para alarmas exactas y vuelve a guardar", Toast.LENGTH_LONG).show();
                return false;
            }
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = sdf.parse(fechaStr + " " + horaStr);

            if (date == null) {
                Log.e("MEDICONTROL", "Error al procesar la fecha/hora");
                return false;
            }

            Calendar calAlarma = Calendar.getInstance();
            calAlarma.setTime(date);
            calAlarma.set(Calendar.SECOND, 0);
            calAlarma.set(Calendar.MILLISECOND, 0);

            long tiempoProgramado = calAlarma.getTimeInMillis();
            long tiempoActual = System.currentTimeMillis();

            if (tiempoProgramado <= tiempoActual) {
                Log.w("MEDICONTROL", "La hora seleccionada ya pasó.");
                Toast.makeText(RegistroMedicamentoActivity.this, "La hora seleccionada ya ha pasado. Selecciona una fecha u hora futura.", Toast.LENGTH_LONG).show();
                return false;
            }

            //Intent dirigido al BroadcastReceiver
            Intent intent = new Intent(this, NotificationReceiver.class);
            intent.putExtra("NOMBRE_MEDICAMENTO", nombre);
            intent.putExtra("DOSIS_MEDICAMENTO", dosis);
            intent.putExtra("MEDICAMENTO_ID", id);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    id, // ID único para que no sobreescriba otras alarmas
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        tiempoProgramado,
                        pendingIntent
                );
                Log.d("MEDICONTROL", "✅ Alarma agendada exitosamente en AlarmManager para milisegundos: " + tiempoProgramado);
                return true; // 🟢 Retorna true si todo salió bien
            }

        } catch (Exception e) {
            Log.e("MEDICONTROL", "Error al programar la alarma: ", e);
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (datePickerDialog != null && datePickerDialog.isShowing()) {
            datePickerDialog.dismiss();
        }
        if (timePickerDialog != null && timePickerDialog.isShowing()) {
            timePickerDialog.dismiss();
        }
    }
}