package com.example.notas;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DialogoCerrarSesion extends DialogFragment {
   ListenerDialogoCerrarSesion miListener;

    public interface ListenerDialogoCerrarSesion {
        void alCerrarSesion(int i);
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sign_out);
        builder.setMessage(R.string.sign_out_msg);
        miListener = (ListenerDialogoCerrarSesion) getActivity();
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                miListener.alCerrarSesion(i);
            }
        });

        builder.setNegativeButton(R.string.no,  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ;
            }
        });

        return builder.create();
    }
}
