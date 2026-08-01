package com.example.medicontrol;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvTomasPendientes, tvTomasCompletadas, tvProximaToma;
    private RecyclerView rvMedicamentos;
    private FloatingActionButton fabAgregarMedicamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTomasPendientes = findViewById(R.id.tvTomasPendientes);
        tvTomasCompletadas = findViewById(R.id.tvTomasCompletadas);
        tvProximaToma = findViewById(R.id.tvProximaToma);
        rvMedicamentos = findViewById(R.id.rvMedicamentos);
        fabAgregarMedicamento = findViewById(R.id.fabAgregarMedicamento);

        rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        fabAgregarMedicamento.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
            startActivity(intent);
        });

        tvTomasPendientes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistorialActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDesdeSQLite();
    }

    private void cargarDatosDesdeSQLite() {
        // Solo obtener medicamentos marcados como activos
        List<Medicamento> listaMedicamentos = AppDatabase.getInstance(this)
                .medicamentoDao()
                .obtenerActivos();

        MedicamentoAdapter adapter = new MedicamentoAdapter(listaMedicamentos, new MedicamentoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Medicamento medicamento) {
                Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
                intent.putExtra("MEDICAMENTO_ID", medicamento.getId());
                intent.putExtra("NOMBRE", medicamento.getNombre());
                intent.putExtra("DOSIS", medicamento.getDosis());
                intent.putExtra("PRESENTACION", medicamento.getPresentacion());
                intent.putExtra("FECHA", medicamento.getFecha());
                intent.putExtra("HORA", medicamento.getHora());
                startActivity(intent);
            }

            @Override
            public void onItemDeleteClick(Medicamento medicamento) {
                mostrarDialogoOcultar(medicamento);
            }
        });

        rvMedicamentos.setAdapter(adapter);

        tvTomasPendientes.setText("Registrados: " + listaMedicamentos.size());
        tvTomasCompletadas.setText("Tomadas: 0");

        if (!listaMedicamentos.isEmpty()) {
            tvProximaToma.setText("Próxima:\n" + listaMedicamentos.get(0).getHora());
        } else {
            tvProximaToma.setText("Próxima:\n--:--");
        }
    }

    private void mostrarDialogoOcultar(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Desactivar Medicamento")
                .setMessage("¿Deseas quitar \"" + medicamento.getNombre() + "\" de los activos? Seguirá guardado en tu historial.")
                .setPositiveButton("Quitar", (dialog, which) -> {
                    // Marcamos como inactivo en lugar de borrarlo físicamente
                    medicamento.setActivo(false);
                    AppDatabase.getInstance(MainActivity.this)
                            .medicamentoDao()
                            .actualizar(medicamento);

                    Toast.makeText(MainActivity.this, "Guardado en Historial", Toast.LENGTH_SHORT).show();
                    cargarDatosDesdeSQLite();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}