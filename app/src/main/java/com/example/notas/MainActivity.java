package com.example.notas;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import android.Manifest;


public class MainActivity extends AppCompatActivity implements DialogoIdiomas.ListenerDialogoIdiomas, DialogoCerrarSesion.ListenerDialogoCerrarSesion {

    private ArrayList<String> titulosCole = new ArrayList<String>();
    private ArrayList<Integer> imagenesCole = new ArrayList<Integer>();
    private String usuario = null;
    private NotificationManager elManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //recuperar el usuario del login
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            usuario = extras.getString("usuario");
        }

        cargarPreferencias();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView textUsuario = (TextView)findViewById(R.id.textUser);
        textUsuario.setText(usuario);

        //comprobar si ya hay colecciones guardadas en BD, y si las hay, meterlas en las variables para visualizarlas
        miBD gestorBD = new miBD(this, "NotasBD", null, 1);
        SQLiteDatabase bd = gestorBD.getReadableDatabase();
        Cursor c = bd.rawQuery("SELECT Nombre, Imagen FROM Colecciones WHERE Usuario = '" + usuario + "'", null);
        while(c.moveToNext()) {
            titulosCole.add(c.getString(0));
            if(c.getInt(1) != -1) {
                imagenesCole.add(c.getInt(1));
            }
        }
        bd.close();
        c.close();

        //crear el manager y el builder de las notificaciones locales
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        NotificationCompat.Builder elBuilder = new NotificationCompat.Builder(this, "Primera_cole")
                .setSmallIcon(R.drawable.postit)
                .setContentTitle(getString(R.string.no_collections))
                .setContentText(getString(R.string.first_collection))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        elManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel elCanal = new NotificationChannel("Primera_cole", "Canal_principal", NotificationManager.IMPORTANCE_DEFAULT);
            elCanal.setDescription("Canal principal");
            elCanal.enableLights(true);
            elCanal.setLightColor(Color.YELLOW);
            elCanal.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            elCanal.enableVibration(true);
            elManager.createNotificationChannel(elCanal);
        }


        //si no hay colecciones lanzar notificacion para crear primera coleccion
        if(titulosCole.size()==0) {
            elManager.notify(1, elBuilder.build());
        }

        //switch para elegir modo
        Switch swModoOscuro = (Switch) findViewById(R.id.switchModoOscuro);
        SharedPreferences prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        String modo = prefs.getString("modo", "claro");
        if(modo.equals("oscuro")) {
            swModoOscuro.setChecked(true);
        }
        swModoOscuro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
                setModo(modo);
            }
        });

        //lista de colecciones personalizada
        AdaptadorListaColecciones adapCole = new AdaptadorListaColecciones(getApplicationContext(), titulosCole, imagenesCole);
        ListView listaCole = (ListView) findViewById(R.id.listaCole);
        listaCole.setAdapter(adapCole);
        listaCole.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String tituloCole = ((TextView)view.findViewById(R.id.titulo)).getText().toString();
                Integer imagenCole = ((ImageView)view.findViewById(R.id.imagen)).getId();
                Intent intent = new Intent(MainActivity.this, ColeccionActivity.class);
                intent.putExtra("usuario", usuario);
                if(!tituloCole.isEmpty()) {
                    intent.putExtra("tituloCole", tituloCole);
                    if(imagenCole != null) {
                        intent.putExtra("imagenCole", imagenCole);
                    }
                    setResult(RESULT_OK, intent);
                    startActivity(intent);
                }
            }
        });

        //definir variable para lanzar el intent de cuando se cree una coleccion
        ActivityResultLauncher<Intent> startCrearColeIntent =registerForActivityResult(new
                        ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            String coleccion = result.getData().getStringExtra("coleccion");
                            int imagen = result.getData().getIntExtra("imagen", R.drawable.postit);
                            ListView listaCole = (ListView) findViewById(R.id.listaCole);
                            listaCole.setAdapter(adapCole);

                            //guardar la nueva coleccion en el fichero
                            miBD gestorBD = new miBD(MainActivity.this, "NotasBD", null, 1);
                            SQLiteDatabase bd = gestorBD.getWritableDatabase();
                            try {
                                bd.execSQL("INSERT INTO Colecciones VALUES ('" + usuario + "', '" + coleccion + "', " + imagen + ")");
                                titulosCole.add(coleccion);
                                imagenesCole.add(imagen);

                                //desactivar la noti de que no hay colecciones si está activa
                                if(elManager.areNotificationsEnabled()) {
                                    elManager.cancel(1);
                                }
                            }
                            catch (SQLiteConstraintException e) {
                                e.printStackTrace();
                                Toast aviso = Toast.makeText(getApplicationContext(), getResources().getString(R.string.collection_exists), Toast.LENGTH_SHORT);
                                aviso.show();
                            }
                            finish();
                            startActivity(getIntent());
                        }
                    }
                });

        //boton idioma
        ImageButton botonIdioma = (ImageButton) findViewById(R.id.botonIdioma);
        botonIdioma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickIdioma(view);
            }
        });

        //boton crear coleccion
        Button botonCrearCole = (Button) findViewById(R.id.botonCrearCole);
        botonCrearCole.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CrearColeActivity.class);
                startCrearColeIntent.launch(intent);
            }
        });


        //boton mostrar usuarios
        Button botonUsuarios = (Button) findViewById(R.id.botonUsuarios);
        botonUsuarios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UsuariosActivity.class);
                startActivity(intent);
            }
        });

        //boton perfil
        ImageButton botonPerfil = (ImageButton) findViewById(R.id.botonPerfil);
        botonPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickPerfil(view);
            }
        });
    }

    //bloqueamos el botón de volver atrás (al login) para que solo se pueda volver a esta actividad al cerrar sesión
    @Override
    public void onBackPressed() {
        ;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("titulosCole", titulosCole);
        outState.putIntegerArrayList("imagenesCole", imagenesCole);
        outState.putString("usuario", usuario);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        titulosCole = savedInstanceState.getStringArrayList("titulosCole");
        imagenesCole = savedInstanceState.getIntegerArrayList("imagenesCole");
        usuario = savedInstanceState.getString("usuario");
        AdaptadorListaColecciones adapCole = new AdaptadorListaColecciones(getApplicationContext(), titulosCole, imagenesCole);
        ListView listaCole = (ListView) findViewById(R.id.listaCole);
        listaCole.setAdapter(adapCole);
    }


    public void onClickIdioma(View view) {
        DialogFragment df = new DialogoIdiomas();
        df.show(getSupportFragmentManager(), "DialogoIdiomas");
    }

    public void onClickPerfil(View view) {
        DialogFragment df = new DialogoCerrarSesion();
        df.show(getSupportFragmentManager(), "DialogoCerrarSesion");
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

        //reiniciar actividad para cargar el idioma
        finish();
        startActivity(getIntent());
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

    public void guardarPreferencesModo(String modo) {
        SharedPreferences prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("modo", modo);
        editor.commit();
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

    public void setModo(String modo) {
        if(modo.equals("oscuro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            guardarPreferencesModo("claro");
        }
        else if(modo.equals("claro")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            guardarPreferencesModo("oscuro");
        }
    }

    @Override
    public void alCerrarSesion(int i) {
        //desactivar la noti de que no hay colecciones si está activa
        if(elManager.areNotificationsEnabled()) {
            elManager.cancel(1);
        }
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

}