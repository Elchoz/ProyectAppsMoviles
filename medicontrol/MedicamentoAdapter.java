package com.example.medicontrol;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class MedicamentoAdapter extends RecyclerView.Adapter<MedicamentoAdapter.MedicamentoViewHolder> {

    private List<Medicamento> listaMedicamentos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Medicamento medicamento);
        void onItemDeleteClick(Medicamento medicamento);
    }

    public MedicamentoAdapter(List<Medicamento> listaMedicamentos, OnItemClickListener listener) {
        this.listaMedicamentos = listaMedicamentos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MedicamentoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicamento, parent, false);
        return new MedicamentoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicamentoViewHolder holder, int position) {
        Medicamento med = listaMedicamentos.get(position);

        if (holder.txtItemNombre != null) {
            holder.txtItemNombre.setText(med.getNombre());
        }

        if (holder.txtItemDetalle != null) {
            String detalle = med.getPresentacion() + " • " + med.getDosis() + " • Hora: " + med.getHora();
            holder.txtItemDetalle.setText(detalle);
        }

        // --- Control visual según el Estado ---
        String estado = med.getEstado();

        if (holder.txtItemEstado != null) {
            if ("FINALIZADO".equalsIgnoreCase(estado)) {
                holder.txtItemEstado.setText("Finalizado");
                holder.txtItemEstado.setTextColor(Color.parseColor("#757575")); // Gris
            } else if ("SUSPENDIDO".equalsIgnoreCase(estado)) {
                holder.txtItemEstado.setText("Suspendido");
                holder.txtItemEstado.setTextColor(Color.parseColor("#D32F2F")); // Rojo
            } else {
                holder.txtItemEstado.setText("Activo");
                holder.txtItemEstado.setTextColor(Color.parseColor("#388E3C")); // Verde
            }
        }

        // Evento: Tocar la tarjeta entera -> Editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(med);
            }
        });

        // Evento: Tocar botón de eliminar (con verificación para evitar Crash)
        if (holder.btnEliminarItem != null) {
            holder.btnEliminarItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemDeleteClick(med);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos != null ? listaMedicamentos.size() : 0;
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView txtItemNombre, txtItemDetalle, txtItemEstado;
        ImageButton btnEliminarItem;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemNombre = itemView.findViewById(R.id.txtItemNombre);
            txtItemDetalle = itemView.findViewById(R.id.txtItemDetalle);
            txtItemEstado = itemView.findViewById(R.id.txtItemEstado);
            btnEliminarItem = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}