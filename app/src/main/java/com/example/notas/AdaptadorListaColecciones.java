package com.example.notas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorListaColecciones extends BaseAdapter {
    private Context contexto;
    private LayoutInflater inflater;
    private ArrayList<String> titulos;
    private ArrayList<Integer> imagenes;

    public AdaptadorListaColecciones(Context pcontext, ArrayList<String> ptitulos, ArrayList<Integer> pimagenes)
    {
        contexto = pcontext;
        titulos = ptitulos;
        imagenes = pimagenes;
        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return titulos.size();
    }

    @Override
    public Object getItem(int i) {
        return titulos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view=inflater.inflate(R.layout.item_colecciones,null);
        TextView titulo = (TextView) view.findViewById(R.id.titulo);
        ImageView img=(ImageView) view.findViewById(R.id.imagen);
        titulo.setText(titulos.get(i));
        img.setImageResource(imagenes.get(i));
        return view;
    }
}
