package com.example.medicontrol;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "medicamentos")
public class Medicamento {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String nombre;
    private String dosis;
    private String presentacion;
    private String fecha;
    private String hora;

    // Campo para distinguir si está activo en la lista principal o solo en historial
    private boolean activo = true;

    public Medicamento(String nombre, String dosis, String presentacion, String fecha, String hora) {
        this.nombre = nombre;
        this.dosis = dosis;
        this.presentacion = presentacion;
        this.fecha = fecha;
        this.hora = hora;
        this.activo = true;
    }

    // Getters y Setters
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

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}