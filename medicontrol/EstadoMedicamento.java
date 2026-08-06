package com.example.medicontrol;

public enum EstadoMedicamento {
    ACTIVO("Activo"),
    FINALIZADO("Finalizado"),
    SUSPENDIDO("Suspendido");

    private final String nombre;

    EstadoMedicamento(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}