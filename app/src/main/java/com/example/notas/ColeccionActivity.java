package com.example.notas;

import android.content.Context;
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
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ColeccionActivity extends AppCompatActivity {
    private ArrayList<String> notas = new ArrayList<String>();
    private String usuario = null;
    private String coleccion = null;
    private int imagen = R.drawable.postit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coleccion);

        //recuperar los datos del activity anterior
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
            coleccion= extras.getString("tituloCole");
            imagen = extras.getInt("imagenCole");
        }

        //visualizar la coleccion en el textview
        TextView textTitulo = (TextView)findViewById(R.id.tituloColeccion);
        if(coleccion != null) {
            textTitulo.setText(coleccion);
        }

        //comprobar si ya hay notas guardadas en BD, y si las hay, meterlas en las variables para visualizarlas
        miBD gestorBD = new miBD(this, "NotasBD", null, 1);
        SQLiteDatabase bd = gestorBD.getReadableDatabase();
        Cursor c = bd.rawQuery("SELECT Nombre FROM Notas WHERE Coleccion = '" + coleccion + "' AND Usuario = '" + usuario + "'", null);
        while(c.moveToNext()) {
            notas.add(c.getString(0));
        }
        c.close();
        bd.close();

        //lista que contiene las notas. Si clicamos en una nota, se elimina
        ArrayAdapter eladaptador=new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, notas);
        ListView listaNotas = (ListView) findViewById(R.id.listaCole);
        listaNotas.setAdapter(eladaptador);
        listaNotas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nota = (String) listaNotas.getItemAtPosition(position);
                eliminarNota(nota);
            }
        });

        //definir variable para lanzar el intent de cuando se cree una nota
        ActivityResultLauncher<Intent> startCrearNotaIntent =registerForActivityResult(new
                        ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            String nota = result.getData().getStringExtra("nota");
                            ListView listaNotas = (ListView) findViewById(R.id.listaCole);
                            listaNotas.setAdapter(eladaptador);

                            //guardar la nueva coleccion en el fichero
                            miBD gestorBD = new miBD(ColeccionActivity.this, "NotasBD", null, 1);
                            SQLiteDatabase bd = gestorBD.getWritableDatabase();
                            try {
                                bd.execSQL("INSERT INTO Notas VALUES ('" + usuario + "', '" + coleccion + "', '" + nota + "', " + imagen + ")");
                                notas.add(nota);
                            }
                            catch (SQLiteConstraintException e) {
                                e.printStackTrace();
                                Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.note_exists), Toast.LENGTH_SHORT);
                                aviso.show();
                            }
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });

        //boton añadir nota
        Button botonAñadirNota = (Button) findViewById(R.id.botonAñadirNota);
        botonAñadirNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ColeccionActivity.this, CrearNotaActivity.class);
                startCrearNotaIntent.launch(intent);
            }
        });

        //boton eliminar coleccion
        Button botonEliminarCole = (Button) findViewById(R.id.botonCrearCole);
        botonEliminarCole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarColeccion();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("coleccion", coleccion);
        outState.putStringArrayList("notas", notas);
        outState.putString("usuario", usuario);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        coleccion = savedInstanceState.getString("coleccion");
        notas = savedInstanceState.getStringArrayList("notas");
        usuario = savedInstanceState.getString("usuario");
        ArrayAdapter eladaptador=new ArrayAdapter<String> (this, android.R.layout.simple_list_item_1, notas);
        ListView listaNotas = (ListView) findViewById(R.id.listaCole);
        listaNotas.setAdapter(eladaptador);
    }

    public void eliminarColeccion() {
        miBD gestorBD = new miBD(ColeccionActivity.this, "NotasBD", null, 1);
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.execSQL("DELETE FROM Notas WHERE Coleccion = '" + coleccion + "' AND Usuario = '" + usuario + "'");
        bd.execSQL("DELETE FROM Colecciones WHERE Nombre = '" + coleccion + "' AND Usuario = '" + usuario + "'");
        bd.close();
        //cuando se elimina la coleccion volvemos a la actividad principal dónde están las colecciones
        Intent intent = new Intent(ColeccionActivity.this, MainActivity.class);
        intent.putExtra("usuario", usuario);
        startActivity(intent);
    }

    public void eliminarNota(String nota) {
        miBD gestorBD = new miBD(ColeccionActivity.this, "NotasBD", null, 1);
        SQLiteDatabase bd = gestorBD.getWritableDatabase();
        bd.execSQL("DELETE FROM Notas WHERE Nombre = '" + nota + "' AND Coleccion = '" + coleccion + "' AND Usuario = '" + usuario + "'");
        bd.close();

        //reiniciar actividad para que vuelva a cargar las notas de BD y no apaezca la recién eliminada
        finish();
        startActivity(getIntent());
    }

}
