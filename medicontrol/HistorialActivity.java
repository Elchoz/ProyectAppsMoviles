package com.example.medicontrol;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

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
        // 1. Obtenemos TODOS los medicamentos (tanto activos como inactivos)
        List<Medicamento> lista = AppDatabase.getInstance(this)
                .medicamentoDao()
                .obtenerTodosHistorial();

        // 2. Pasamos la variable 'lista' al adaptador
        adapter = new HistorialAdapter(lista, new HistorialAdapter.OnEliminarClickListener() {
            @Override
            public void onEliminarClick(Medicamento medicamento) {
                confirmarEliminacionDefinitiva(medicamento);
            }
        });

        rvHistorial.setAdapter(adapter);
    }

    private void confirmarEliminacionDefinitiva(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar del Historial")
                .setMessage("¿Deseas borrar permanentemente el registro de \"" + medicamento.getNombre() + "\"? No se podrá recuperar.")
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Llamada al método de eliminación definitiva del DAO
                        AppDatabase.getInstance(HistorialActivity.this)
                                .medicamentoDao()
                                .eliminarDefinitivo(medicamento);

                        Toast.makeText(HistorialActivity.this, "Registro eliminado permanentemente", Toast.LENGTH_SHORT).show();
                        cargarHistorial(); // Refrescar lista
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
