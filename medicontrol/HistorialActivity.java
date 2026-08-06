package com.example.medicontrol;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView rvHistorial;
    private HistorialAdapter adapter;
    private List<Medicamento> listaHistorial = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        rvHistorial = findViewById(R.id.rvHistorialMedicamentos);
        rvHistorial.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar adaptador una sola vez en el onCreate
        adapter = new HistorialAdapter(listaHistorial, this::confirmarEliminacionDefinitiva);
        rvHistorial.setAdapter(adapter);

        cargarHistorial();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarHistorial();
    }

    private void cargarHistorial() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Obtener todos los registros ordenados desde Room
            List<Medicamento> lista = AppDatabase.getInstance(HistorialActivity.this)
                    .medicamentoDao()
                    .obtenerTodosHistorial();

            runOnUiThread(() -> {
                listaHistorial.clear();
                if (lista != null) {
                    listaHistorial.addAll(lista);
                }
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void confirmarEliminacionDefinitiva(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar del Historial")
                .setMessage("¿Deseas borrar permanentemente el registro de \"" + medicamento.getNombre() + "\"? No se podrá recuperar.")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase.getInstance(HistorialActivity.this)
                                .medicamentoDao()
                                .eliminar(medicamento);

                        runOnUiThread(() -> {
                            Toast.makeText(HistorialActivity.this, "Registro eliminado permanentemente", Toast.LENGTH_SHORT).show();
                            cargarHistorial();
                        });
                    });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}