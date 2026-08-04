package com.example.medicontrol;

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
        holder.txtItemNombre.setText(med.getNombre());
        holder.txtItemDetalle.setText(med.getPresentacion() + " • " + med.getDosis() + " • Hora: " + med.getHora());

        // Tocar la tarjeta -> Editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(med);
            }
        });

        // Tocar el botón de basurero -> Eliminar
        holder.btnEliminarItem.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemDeleteClick(med);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMedicamentos != null ? listaMedicamentos.size() : 0;
    }

    public static class MedicamentoViewHolder extends RecyclerView.ViewHolder {
        TextView txtItemNombre, txtItemDetalle;
        ImageButton btnEliminarItem;

        public MedicamentoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtItemNombre = itemView.findViewById(R.id.txtItemNombre);
            txtItemDetalle = itemView.findViewById(R.id.txtItemDetalle);
            btnEliminarItem = itemView.findViewById(R.id.btnEliminarItem);
        }
    }
}
