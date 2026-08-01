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
    void insertar(Medicamento medicamento);

    @Update
    void actualizar(Medicamento medicamento);

    @Delete
    void eliminarDefinitivo(Medicamento medicamento); // 👈 Método para borrar de SQLite

    @Query("SELECT * FROM medicamentos WHERE activo = 1 ORDER BY id DESC")
    List<Medicamento> obtenerActivos();

    @Query("SELECT * FROM medicamentos ORDER BY id DESC")
    List<Medicamento> obtenerTodosHistorial();
}