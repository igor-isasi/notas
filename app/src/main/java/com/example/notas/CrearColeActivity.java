package com.example.notas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrearColeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_cole);

        Button miBoton  = (Button) findViewById(R.id.botonAñadir);
        miBoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText miTitulo = (EditText) findViewById(R.id.tituloCole);
                if(!miTitulo.getText().toString().isEmpty()) {
                    Intent intent = new Intent();
                    intent.putExtra("coleccion", miTitulo.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                }
                else {
                    Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_title_collection), Toast.LENGTH_SHORT);
                    aviso.show();
                }
            }
        });
    }
}
