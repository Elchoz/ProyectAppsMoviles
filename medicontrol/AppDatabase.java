package com.example.medicontrol;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Subimos la versión a 2 si cambió la estructura
@Database(entities = {Medicamento.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract MedicamentoDao medicamentoDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "medicontrol_db")
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration() // 👈 Destruye la BD vieja y la crea nueva sin fallar
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
