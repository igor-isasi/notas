package com.example.notas;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class UsuariosActivity extends AppCompatActivity {
    private ArrayList<String> usuarios = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usuarios);

        //leer usuarios del fichero
        try {
            BufferedReader fichero = new BufferedReader(new InputStreamReader(openFileInput("usuarios.txt")));
            String linea = fichero.readLine();
            while(linea != null) {
                usuarios.add(linea);
                linea = fichero.readLine();
            }
            fichero.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //lista que contiene los usuarios
        ArrayAdapter eladaptador=new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, usuarios);
        ListView listaNotas = (ListView) findViewById(R.id.listaUsuarios);
        listaNotas.setAdapter(eladaptador);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("usuarios", usuarios);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        usuarios = savedInstanceState.getStringArrayList("usuarios");
        ArrayAdapter eladaptador=new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, usuarios);
        ListView listaUsuarios = (ListView) findViewById(R.id.listaUsuarios);
        listaUsuarios.setAdapter(eladaptador);
    }
}
