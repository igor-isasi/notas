package com.example.notas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class miBD extends SQLiteOpenHelper {

    public miBD(@Nullable Context context, @Nullable String name,
                @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE Usuarios ('Nombre' VARCHAR(10) PRIMARY KEY NOT NULL, 'Contraseña' VARCHAR(20))");
        sqLiteDatabase.execSQL("CREATE TABLE Colecciones ('Usuario' VARCHAR(10), 'Nombre' VARCHAR(50), 'Imagen' INTEGER, PRIMARY KEY('Usuario', 'Nombre'), FOREIGN KEY('Usuario') REFERENCES Usuarios('Nombre'))");
        sqLiteDatabase.execSQL("CREATE TABLE Notas ('Usuario' VARCHAR(10), 'Coleccion' VARCHAR(50), 'Nombre' VARCHAR(100), 'Imagen' INTEGER, PRIMARY KEY('Usuario', 'Coleccion', 'Nombre'), FOREIGN KEY('Usuario') REFERENCES Usuarios('Nombre'), FOREIGN KEY('Coleccion') REFERENCES Colecciones('Nombre'))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
