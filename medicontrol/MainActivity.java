package com.example.medicontrol;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView tvTomasPendientes, tvTomasCompletadas, tvProximaToma;
    private TextView tvTotalProgramadas, tvTotalTomadas, tvTotalPendientes, tvTotalOmitidas;
    private RecyclerView rvMedicamentos;
    private FloatingActionButton fabAgregarMedicamento;
    private ImageButton btnAjustes;

    private MedicamentoAdapter adapter;
    private List<Medicamento> listaMedicamentosActual = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeHelper.aplicarTemaGuardado(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTomasPendientes = findViewById(R.id.tvTomasPendientes);
        tvTomasCompletadas = findViewById(R.id.tvTomasCompletadas);
        tvProximaToma = findViewById(R.id.tvProximaToma);

        tvTotalProgramadas = findViewById(R.id.tvTotalProgramadas);
        tvTotalTomadas = findViewById(R.id.tvTotalTomadas);
        tvTotalPendientes = findViewById(R.id.tvTotalPendientes);
        tvTotalOmitidas = findViewById(R.id.tvTotalOmitidas);

        rvMedicamentos = findViewById(R.id.rvMedicamentos);
        fabAgregarMedicamento = findViewById(R.id.fabAgregarMedicamento);
        btnAjustes = findViewById(R.id.btnAjustes);

        if (rvMedicamentos != null) {
            rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
            adapter = new MedicamentoAdapter(listaMedicamentosActual, new MedicamentoAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Medicamento medicamento) {
                    mostrarDialogoMarcarToma(medicamento);
                }

                @Override
                public void onItemDeleteClick(Medicamento medicamento) {
                    mostrarDialogoOcultar(medicamento);
                }
            });
            rvMedicamentos.setAdapter(adapter);
        }

        if (btnAjustes != null) {
            btnAjustes.setOnClickListener(v -> mostrarDialogoAjustes());
        }

        if (fabAgregarMedicamento != null) {
            fabAgregarMedicamento.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
                startActivity(intent);
            });
        }

        if (tvTomasPendientes != null) {
            tvTomasPendientes.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
                startActivity(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDesdeSQLite();
    }

    private void cargarDatosDesdeSQLite() {
        Executors.newSingleThreadExecutor().execute(() -> {
            SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date ahora = new Date();

            String fechaActual = sdfFecha.format(ahora);
            String horaActual = sdfHora.format(ahora);

            // 1. Marcar automáticamente como OMITIDO los pendientes cuya hora ya pasó
            AppDatabase.getInstance(MainActivity.this)
                    .medicamentoDao()
                    .autoMarcarOmitidos(fechaActual, horaActual);

            // 2. Obtener lista de los que se muestran activamente en pantalla
            List<Medicamento> listaVisibles = AppDatabase.getInstance(MainActivity.this)
                    .medicamentoDao()
                    .obtenerActivos();

            // 3. Obtener el universo total para el conteo preciso de hoy
            List<Medicamento> listaTodos = AppDatabase.getInstance(MainActivity.this)
                    .medicamentoDao()
                    .obtenerTodos();

            runOnUiThread(() -> {
                listaMedicamentosActual.clear();
                if (listaVisibles != null) {
                    listaMedicamentosActual.addAll(listaVisibles);
                }

                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                actualizarResumenDelDia(listaTodos);
            });
        });
    }

    private void mostrarDialogoMarcarToma(Medicamento medicamento) {
        String[] opciones = {
                "Marcar como Tomada 💊",
                "Marcar como Omitida ❌",
                "Marcar como Pendiente ⏳",
                "Editar Información ✏️"
        };

        new AlertDialog.Builder(this)
                .setTitle("Registro de toma: " + medicamento.getNombre())
                .setItems(opciones, (dialog, which) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
                    String fechaHoraActual = sdf.format(new Date());

                    switch (which) {
                        case 0:
                            actualizarEstadoMedicamento(medicamento, "TOMADO", fechaHoraActual);
                            break;
                        case 1:
                            actualizarEstadoMedicamento(medicamento, "OMITIDO", fechaHoraActual);
                            break;
                        case 2:
                            actualizarEstadoMedicamento(medicamento, "ACTIVO", "");
                            break;
                        case 3:
                            Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
                            intent.putExtra("MEDICAMENTO_ID", medicamento.getId());
                            intent.putExtra("NOMBRE", medicamento.getNombre());
                            intent.putExtra("DOSIS", medicamento.getDosis());
                            intent.putExtra("PRESENTACION", medicamento.getPresentacion());
                            intent.putExtra("FECHA", medicamento.getFecha());
                            intent.putExtra("HORA", medicamento.getHora());
                            intent.putExtra("ESTADO", medicamento.getEstado());
                            startActivity(intent);
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void actualizarEstadoMedicamento(Medicamento medicamento, String nuevoEstado, String fechaHoraAccion) {
        medicamento.setEstado(nuevoEstado);

        try {
            medicamento.setFechaHoraAccion(fechaHoraAccion);
        } catch (NoSuchMethodError | Exception ignored) {
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(MainActivity.this)
                    .medicamentoDao()
                    .actualizar(medicamento);

            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Toma registrada como: " + nuevoEstado, Toast.LENGTH_SHORT).show();
                cargarDatosDesdeSQLite();
            });
        });
    }

    private void actualizarResumenDelDia(List<Medicamento> listaMedicamentos) {
        int programadas = 0;
        int tomadas = 0;
        int pendientes = 0;
        int omitidas = 0;

        Medicamento proximoMedicamento = null;
        long menorDiferencia = Long.MAX_VALUE;

        SimpleDateFormat sdfFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String fechaHoy = sdfFecha.format(new Date());

        Calendar horaActual = Calendar.getInstance();

        if (listaMedicamentos != null) {
            for (Medicamento med : listaMedicamentos) {
                if (fechaHoy.equals(med.getFecha())) {
                    programadas++;

                    String estado = med.getEstado() != null ? med.getEstado().toUpperCase() : "ACTIVO";

                    switch (estado) {
                        case "TOMADO":
                        case "TOMADA":
                            tomadas++;
                            break;

                        case "OMITIDO":
                        case "OMITIDA":
                            omitidas++;
                            break;

                        case "PENDIENTE":
                        case "ACTIVO":
                        default:
                            pendientes++;

                            if (med.getHora() != null && !med.getHora().trim().isEmpty() && med.getHora().contains(":")) {
                                try {
                                    String horaLimpia = med.getHora().replaceAll("[^0-9:]", "").trim();
                                    String[] partesHora = horaLimpia.split(":");

                                    if (partesHora.length >= 2) {
                                        Calendar horaMed = Calendar.getInstance();
                                        horaMed.set(Calendar.HOUR_OF_DAY, Integer.parseInt(partesHora[0].trim()));
                                        horaMed.set(Calendar.MINUTE, Integer.parseInt(partesHora[1].trim()));
                                        horaMed.set(Calendar.SECOND, 0);

                                        long diferencia = horaMed.getTimeInMillis() - horaActual.getTimeInMillis();

                                        if (diferencia > 0 && diferencia < menorDiferencia) {
                                            menorDiferencia = diferencia;
                                            proximoMedicamento = med;
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                    }
                }
            }
        }

        if (tvTotalProgramadas != null) tvTotalProgramadas.setText(String.valueOf(programadas));
        if (tvTotalTomadas != null) tvTotalTomadas.setText(String.valueOf(tomadas));
        if (tvTotalPendientes != null) tvTotalPendientes.setText(String.valueOf(pendientes));
        if (tvTotalOmitidas != null) tvTotalOmitidas.setText(String.valueOf(omitidas));

        if (tvTomasPendientes != null) tvTomasPendientes.setText("Registrados: " + programadas);
        if (tvTomasCompletadas != null) tvTomasCompletadas.setText("Tomadas: " + tomadas);

        if (tvProximaToma != null) {
            if (proximoMedicamento != null) {
                tvProximaToma.setText("Próxima:\n" + proximoMedicamento.getNombre() + " (" + proximoMedicamento.getHora() + ")");
            } else {
                tvProximaToma.setText("Próxima:\n--:--");
            }
        }
    }

    private void cancelarAlarma(int id) {
        try {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    id,
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

    private void mostrarDialogoOcultar(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Desactivar Medicamento")
                .setMessage("¿Deseas quitar \"" + medicamento.getNombre() + "\" de los activos? Seguirá guardado en tu historial.")
                .setPositiveButton("Quitar", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        medicamento.setEstado("FINALIZADO");
                        cancelarAlarma(medicamento.getId());

                        AppDatabase.getInstance(MainActivity.this)
                                .medicamentoDao()
                                .actualizar(medicamento);

                        runOnUiThread(() -> {
                            Toast.makeText(MainActivity.this, "Guardado en Historial", Toast.LENGTH_SHORT).show();
                            cargarDatosDesdeSQLite();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarDialogoAjustes() {
        boolean isDarkModeActual = ThemeHelper.obtenerPreferenciaTema(this);

        String[] opciones = {"Modo Claro ☀️", "Modo Oscuro 🌙"};
        int seleccionInicial = isDarkModeActual ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Configuración de Tema")
                .setSingleChoiceItems(opciones, seleccionInicial, (dialog, which) -> {
                    boolean seleccionarModoOscuro = (which == 1);

                    if (seleccionarModoOscuro != isDarkModeActual) {
                        dialog.dismiss();
                        ThemeHelper.guardarTema(MainActivity.this, seleccionarModoOscuro);
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}