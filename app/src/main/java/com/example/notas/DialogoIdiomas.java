package com.example.notas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DialogoIdiomas extends DialogFragment {

    ListenerDialogoIdiomas miListener;

    public interface ListenerDialogoIdiomas {
        void alElegirIdioma(int i, ArrayList<Integer> opciones);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.language_selection);
        ArrayList<Integer> opciones = new ArrayList<Integer>();
        opciones.add(R.string.basque);
        opciones.add(R.string.spanish);
        opciones.add(R.string.english);
        AdaptadorDialogoIdiomas dialogAdapter = new AdaptadorDialogoIdiomas(getContext(), opciones);
        miListener = (ListenerDialogoIdiomas) getActivity();
        builder.setAdapter(dialogAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                miListener.alElegirIdioma(i, opciones);
            }
        });
        return builder.create();
    }
}
