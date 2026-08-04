package com.example.medicontrol;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
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
    private ImageButton btnAjustes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 1. Aplicar la preferencia de tema guardada antes de crear la vista
        ThemeHelper.aplicarTema(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 2. Vincular componentes de la interfaz
        tvTomasPendientes = findViewById(R.id.tvTomasPendientes);
        tvTomasCompletadas = findViewById(R.id.tvTomasCompletadas);
        tvProximaToma = findViewById(R.id.tvProximaToma);
        rvMedicamentos = findViewById(R.id.rvMedicamentos);
        fabAgregarMedicamento = findViewById(R.id.fabAgregarMedicamento);
        btnAjustes = findViewById(R.id.btnAjustes);

        if (rvMedicamentos != null) {
            rvMedicamentos.setLayoutManager(new LinearLayoutManager(this));
        }

        // 3. Asignar listener al botón de Ajustes (⚙️)
        if (btnAjustes != null) {
            btnAjustes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mostrarDialogoAjustes();
                }
            });
        }

        // 4. Evento para ir al formulario de registro
        if (fabAgregarMedicamento != null) {
            fabAgregarMedicamento.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, RegistroMedicamentoActivity.class);
                startActivity(intent);
            });
        }

        // 5. Evento para ir a la pantalla de Historial
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

        if (rvMedicamentos != null) {
            rvMedicamentos.setAdapter(adapter);
        }

        if (tvTomasPendientes != null) {
            tvTomasPendientes.setText("Registrados: " + listaMedicamentos.size());
        }

        if (tvTomasCompletadas != null) {
            tvTomasCompletadas.setText("Tomadas: 0");
        }

        if (tvProximaToma != null) {
            if (!listaMedicamentos.isEmpty()) {
                tvProximaToma.setText("Próxima:\n" + listaMedicamentos.get(0).getHora());
            } else {
                tvProximaToma.setText("Próxima:\n--:--");
            }
        }
    }

    private void mostrarDialogoOcultar(Medicamento medicamento) {
        new AlertDialog.Builder(this)
                .setTitle("Desactivar Medicamento")
                .setMessage("¿Deseas quitar \"" + medicamento.getNombre() + "\" de los activos? Seguirá guardado en tu historial.")
                .setPositiveButton("Quitar", (dialog, which) -> {
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

    // 🎨 Diálogo de selección de Tema (Claro / Oscuro) - CORREGIDO
    private void mostrarDialogoAjustes() {
        boolean isDarkModeActual = ThemeHelper.obtenerPreferenciaTema(this);

        String[] opciones = {"Modo Claro ☀️", "Modo Oscuro 🌙"};
        int seleccionInicial = isDarkModeActual ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle("Configuración de Tema")
                .setSingleChoiceItems(opciones, seleccionInicial, (dialog, which) -> {
                    boolean seleccionarModoOscuro = (which == 1);

                    if (seleccionarModoOscuro != isDarkModeActual) {
                        // 1. Ocultar el diálogo primero para evitar conflictos de vista
                        dialog.dismiss();

                        // 2. Guardar y cambiar el modo (Android recreará la vista automáticamente)
                        ThemeHelper.guardarTema(MainActivity.this, seleccionarModoOscuro);
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
