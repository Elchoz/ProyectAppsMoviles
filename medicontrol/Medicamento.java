package com.example.medicontrol;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicamento")
public class Medicamento {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String nombre;
    private String dosis;
    private String presentacion;
    private String fecha;
    private String hora;
    private String estado;
    private String fechaHoraAccion; // Campo nuevo

    public Medicamento(String nombre, String dosis, String presentacion, String fecha, String hora, String estado) {
        this.nombre = nombre;
        this.dosis = dosis;
        this.presentacion = presentacion;
        this.fecha = fecha;
        this.hora = hora;
        this.estado = estado;
        this.fechaHoraAccion = "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDosis() { return dosis; }
    public void setDosis(String dosis) { this.dosis = dosis; }

    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFechaHoraAccion() { return fechaHoraAccion; }
    public void setFechaHoraAccion(String fechaHoraAccion) { this.fechaHoraAccion = fechaHoraAccion; }
}