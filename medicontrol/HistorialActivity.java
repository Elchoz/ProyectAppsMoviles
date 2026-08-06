package com.example.medicontrol;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executors;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView rvHistorial;
    private HistorialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        rvHistorial = findViewById(R.id.rvHistorialMedicamentos);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));

        cargarHistorial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    private void cargarHistorial() {
        // Ejecución en hilo secundario para evitar bloqueos e inconsistencias en la UI
        Executors.newSingleThreadExecutor().execute(() -> {
            // 1. Obtener medicamentos desde Room Database
            List<Medicamento> lista = AppDatabase.getInstance(HistorialActivity.this)
                    .medicamentoDao()
                    .obtenerTodosHistorial();

            // 2. Renderizar lista en el hilo principal
            runOnUiThread(() -> {
                adapter = new HistorialAdapter(lista, new HistorialAdapter.OnEliminarClickListener() {
                    @Override
                    public void onEliminarClick(Medicamento medicamento) {
                        confirmarEliminacionDefinitiva(medicamento);
                    }
                });
                rvHistorial.setAdapter(adapter);
            });
        });
    }

    private void confirmarEliminacionDefinitiva(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar del Historial")
                .setMessage("¿Deseas borrar permanentemente el registro de \"" + medicamento.getNombre() + "\"? No se podrá recuperar.")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Eliminación física en hilo secundario
                        Executors.newSingleThreadExecutor().execute(() -> {
                            AppDatabase.getInstance(HistorialActivity.this)
                                    .medicamentoDao()
                                    .eliminar(medicamento); // ✅ Nombre correcto

                            // Actualizar UI
                            runOnUiThread(() -> {
                                Toast.makeText(HistorialActivity.this, "Registro eliminado permanentemente", Toast.LENGTH_SHORT).show();
                                cargarHistorial(); // Refrescar vista
                            });
                        });
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}