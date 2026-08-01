package com.example.medicontrol;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.HistorialViewHolder> {

    private List<Medicamento> listaMedicamentos;
    private OnEliminarClickListener listener;

    public interface OnEliminarClickListener {
        void onEliminarClick(Medicamento medicamento);
    }

    public HistorialAdapter(List<Medicamento> listaMedicamentos, OnEliminarClickListener listener) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicamento_historial, parent, false);
        return new HistorialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistorialViewHolder holder, int position) {
        Medicamento med = listaMedicamentos.get(position);
        holder.tvNombre.setText(med.getNombre());
        holder.tvDetalle.setText(med.getDosis() + " • " + med.getPresentacion());
        holder.tvHorario.setText("Hora: " + med.getHora() + " | Fecha: " + med.getFecha());

        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEliminarClick(med);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos != null ? listaMedicamentos.size() : 0;
    }

    public static class HistorialViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDetalle, tvHorario;
        ImageButton btnEliminar;

        public HistorialViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvHistorialNombre);
            tvDetalle = itemView.findViewById(R.id.tvHistorialDetalle);
            tvHorario = itemView.findViewById(R.id.tvHistorialHorario);
            btnEliminar = itemView.findViewById(R.id.btnEliminarMedicamento);
        }
    }
}