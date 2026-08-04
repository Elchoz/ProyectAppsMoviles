package com.example.medicontrol;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;
import java.util.Locale;

public class RegistroMedicamentoActivity extends AppCompatActivity {

    private EditText etNombre, etDosis, etFechaInicio, etHoraToma;
    private Spinner spPresentacion;
    private Button btnGuardar;

    // Variables para almacenar la fecha y hora seleccionadas
    private String fechaSeleccionada = "";
    private String horaSeleccionada = "";
    private int medicamentoId = -1; // -1 indica que es un nuevo registro

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_medicamento);

        // 🔔 Solicitar permiso de notificaciones dinámico para Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 1. Vincular vistas con el XML
        etNombre = findViewById(R.id.etNombre);
        etDosis = findViewById(R.id.etDosis);
        etFechaInicio = findViewById(R.id.etFechaInicio);
        etHoraToma = findViewById(R.id.etHoraToma);
        spPresentacion = findViewById(R.id.spPresentacion);
        btnGuardar = findViewById(R.id.btnGuardar);

        // Bloquear teclado directo para obligar el uso de los dialogs
        etFechaInicio.setFocusable(false);
        etFechaInicio.setClickable(true);
        etHoraToma.setFocusable(false);
        etHoraToma.setClickable(true);

        // 2. Verificar si viene en modo edición desde MainActivity
        if (getIntent().hasExtra("MEDICAMENTO_ID")) {
            medicamentoId = getIntent().getIntExtra("MEDICAMENTO_ID", -1);
            etNombre.setText(getIntent().getStringExtra("NOMBRE"));
            etDosis.setText(getIntent().getStringExtra("DOSIS"));
            fechaSeleccionada = getIntent().getStringExtra("FECHA");
            horaSeleccionada = getIntent().getStringExtra("HORA");
            etFechaInicio.setText(fechaSeleccionada);
            etHoraToma.setText(horaSeleccionada);
            btnGuardar.setText("Actualizar Medicamento");
        }

        // 3. Eventos de los selectores
        etFechaInicio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFecha();
            }
        });

        etHoraToma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorHora();
            }
        });

        // 4. Guardar / Actualizar
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarMedicamento();
            }
        });
    }

    private void mostrarSelectorFecha() {
        Calendar calendario = Calendar.getInstance();
        int anio = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistroMedicamentoActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fechaSeleccionada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (month + 1), year);
                        etFechaInicio.setText(fechaSeleccionada);
                    }
                },
                anio, mes, dia
        );
        datePickerDialog.show();
    }

    private void mostrarSelectorHora() {
        Calendar calendario = Calendar.getInstance();
        int hora = calendario.get(Calendar.HOUR_OF_DAY);
        int minuto = calendario.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                RegistroMedicamentoActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        etHoraToma.setText(horaSeleccionada);
                    }
                },
                hora, minuto, true
        );
        timePickerDialog.show();
    }

    private void guardarMedicamento() {
        String nombre = etNombre.getText().toString().trim();
        String dosis = etDosis.getText().toString().trim();
        String presentacion = spPresentacion.getSelectedItem() != null ?
                spPresentacion.getSelectedItem().toString() : "Tableta";

        // Validaciones de formulario
        if (nombre.isEmpty()) {
            etNombre.setError("Ingresa el nombre del medicamento");
            return;
        }
        if (dosis.isEmpty()) {
            etDosis.setError("Ingresa la dosis");
            return;
        }
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha de inicio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (horaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una hora de toma", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear objeto
        Medicamento medicamento = new Medicamento(nombre, dosis, presentacion, fechaSeleccionada, horaSeleccionada);

        int idGenerado = medicamentoId;

        if (medicamentoId == -1) {
            // Insertar nuevo registro en Room SQLite
            AppDatabase.getInstance(this).medicamentoDao().insertar(medicamento);
            idGenerado = (int) System.currentTimeMillis();
        } else {
            // Actualizar registro existente en Room SQLite
            medicamento.setId(medicamentoId);
            AppDatabase.getInstance(this).medicamentoDao().actualizar(medicamento);
        }

        // Programar notificación combinando Fecha + Hora seleccionadas
        programarNotificacion(nombre, fechaSeleccionada, horaSeleccionada, idGenerado);

        Toast.makeText(this, "¡Medicamento guardado con éxito!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void programarNotificacion(String nombreMedicamento, String fecha, String hora, int idNotificacion) {
        try {
            // Parsear Fecha (dd/MM/yyyy) y Hora (HH:mm)
            String[] partesFecha = fecha.split("/");
            int dia = Integer.parseInt(partesFecha[0]);
            int mes = Integer.parseInt(partesFecha[1]) - 1; // En Calendar Enero es 0
            int anio = Integer.parseInt(partesFecha[2]);

            String[] partesHora = hora.split(":");
            int horaInt = Integer.parseInt(partesHora[0]);
            int minutoInt = Integer.parseInt(partesHora[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.YEAR, anio);
            calendar.set(Calendar.MONTH, mes);
            calendar.set(Calendar.DAY_OF_MONTH, dia);
            calendar.set(Calendar.HOUR_OF_DAY, horaInt);
            calendar.set(Calendar.MINUTE, minutoInt);
            calendar.set(Calendar.SECOND, 0);

            // Si la fecha/hora elegida ya transcurrió en el pasado exacto, sumar 1 día
            if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            // Intent hacia el BroadcastReceiver
            Intent alarmIntent = new Intent(this, NotificationReceiver.class);
            alarmIntent.putExtra("NOMBRE_MEDICAMENTO", nombreMedicamento);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    idNotificacion,
                    alarmIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                    } else {
                        Intent intentPermiso = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        startActivity(intentPermiso);
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
