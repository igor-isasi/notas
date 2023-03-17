package com.example.notas;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity implements DialogoIdiomas.ListenerDialogoIdiomas {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        cargarPreferencias();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //boton idioma
        ImageButton botonIdioma = (ImageButton) findViewById(R.id.botonIdioma);
        botonIdioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickIdioma(view);
            }
        });

        //boton iniciar sesion
        Button botonIniciarSesion = (Button) findViewById(R.id.botonIniciarSesion);
        botonIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickIniciarSesion(view);
            }
        });

        //boton registro
        Button botonRegistro = (Button) findViewById(R.id.botonRegistro);
        botonRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRegistro(view);
            }
        });
    }

    @Override
    public void onBackPressed() {
        ;
    }

    public void onClickIdioma(View view) {
        DialogFragment df = new DialogoIdiomas();
        df.show(getSupportFragmentManager(), "DialogoIdiomas");
    }

    public void onClickIniciarSesion(View view) {
        String usuario = ((TextView)findViewById(R.id.textUsuario)).getText().toString();
        String contraseña = ((TextView)findViewById(R.id.textContraseña)).getText().toString();
        if (!usuario.isEmpty() && !contraseña.isEmpty()) {
            boolean found = false;
            miBD gestorBD = new miBD(this, "NotasBD", null, 1);
            SQLiteDatabase bd = gestorBD.getReadableDatabase();
            Cursor c = bd.rawQuery("SELECT * FROM Usuarios", null);
            while(c.moveToNext()) {
                if(usuario.equals(c.getString(0)) && contraseña.equals(c.getString(1))) {
                    found = true;
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("usuario", usuario);
                    setResult(RESULT_OK, intent);
                    startActivity(intent);
                }
            }
            bd.close();
            c.close();
            if(!found) {
                Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_not_found), Toast.LENGTH_SHORT);
                aviso.show();
            }
        }
        else {
            Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_login), Toast.LENGTH_SHORT);
            aviso.show();
        }
    }

    public void onClickRegistro(View view) {
        String usuario = ((TextView)findViewById(R.id.textUsuario)).getText().toString();
        String contraseña = ((TextView)findViewById(R.id.textContraseña)).getText().toString();
        if (!usuario.isEmpty() && !contraseña.isEmpty()) {
            //se intenta meter el usuario en BD y si ya existe, entonces salta el Toast
            miBD gestorBD = new miBD(this, "NotasBD", null, 1);
            SQLiteDatabase bd = gestorBD.getWritableDatabase();
            try {
                bd.execSQL("INSERT INTO Usuarios VALUES ('" + usuario + "', '" + contraseña + "')");
                bd.close();
                //meter usuario en el fichero para después visualizarlos todos
                try {
                    OutputStreamWriter fichero = new OutputStreamWriter(openFileOutput("usuarios.txt", Context.MODE_APPEND));
                    fichero.write(usuario + "\n");
                    fichero.close();
                } catch (IOException e) {e.printStackTrace(); }

                Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.succ_sign_up), Toast.LENGTH_SHORT);
                aviso.show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("usuario", usuario);
                setResult(RESULT_OK, intent);
                startActivity(intent);
            }
            catch (SQLiteConstraintException e) {
                e.printStackTrace();
                Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.user_exists), Toast.LENGTH_SHORT);
                aviso.show();
                bd.close();
            }

        }
        else {
            Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.empty_login), Toast.LENGTH_SHORT);
            aviso.show();
        }
    }

    @Override
    public void alElegirIdioma(int i, ArrayList<Integer> opciones) {
        Locale nuevaloc = new Locale("es");
        switch(i) {
            case 0:
                nuevaloc = new Locale("eu");
                guardarPreferencesIdioma("Euskara");
                break;
            case 1:
                nuevaloc = new Locale("es");
                guardarPreferencesIdioma("Castellano");
                break;
            case 2:
                nuevaloc = new Locale("eng");
                guardarPreferencesIdioma("English");
                break;
        }
        Locale.setDefault(nuevaloc);
        Configuration configuration =
                getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        //reiniciar actividad para que se cargue el idioma
        finish();
        startActivity(getIntent());
    }

    public void setIdioma(String idioma) {
        Locale nuevaloc = new Locale("es");
        switch(idioma) {
            case "Euskara":
                nuevaloc = new Locale("eu");
                break;
            case "Castellano":
                nuevaloc = new Locale("es");
                break;
            case "English":
                nuevaloc = new Locale("eng");
                break;
        }
        Locale.setDefault(nuevaloc);
        Configuration configuration =
                getBaseContext().getResources().getConfiguration();
        configuration.setLocale(nuevaloc);
        configuration.setLayoutDirection(nuevaloc);

        Context context = getBaseContext().createConfigurationContext(configuration);
        getBaseContext().getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());
    }

    public void cargarPreferencias() {
        SharedPreferences prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String idioma = prefs.getString("idioma", "");
        setIdioma(idioma);

        String modo = prefs.getString("modo", "");
        if(modo.equals("oscuro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else if(modo.equals("claro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void guardarPreferencesIdioma(String idioma) {
        SharedPreferences prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("idioma", idioma);
        editor.commit();
    }
}
