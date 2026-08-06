package com.example.medicontrol;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MedicamentoDao {

    @Insert
    long insertar(Medicamento medicamento);

    @Update
    void actualizar(Medicamento medicamento);

    @Delete
    void eliminar(Medicamento medicamento);

    @Query("SELECT * FROM medicamento WHERE id = :id LIMIT 1")
    Medicamento obtenerPorId(int id);

    @Query("SELECT * FROM medicamento WHERE estado = 'ACTIVO' ORDER BY id DESC")
    List<Medicamento> obtenerActivos();

    @Query("SELECT * FROM medicamento")
    List<Medicamento> obtenerTodos();

    // 🟢 Método restaurado para el HistorialActivity
    @Query("SELECT * FROM medicamento ORDER BY id DESC")
    List<Medicamento> obtenerTodosHistorial();

    @Query("UPDATE medicamento SET estado = :nuevoEstado WHERE id = :id")
    void actualizarEstado(int id, String nuevoEstado);

    @Query("UPDATE medicamento SET estado = 'OMITIDO' WHERE estado = 'ACTIVO' AND (fecha < :fechaActual OR (fecha = :fechaActual AND hora < :horaActual))")
    void autoMarcarOmitidos(String fechaActual, String horaActual);

    @Query("SELECT * FROM medicamento WHERE estado = 'TOMADO' ORDER BY id DESC")
    List<Medicamento> obtenerTomados();

    @Query("SELECT * FROM medicamento WHERE estado = 'OMITIDO' ORDER BY id DESC")
    List<Medicamento> obtenerOmitidos();
}
