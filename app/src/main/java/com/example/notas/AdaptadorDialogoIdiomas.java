package com.example.notas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AdaptadorDialogoIdiomas extends BaseAdapter {
    private Context contexto;
    private LayoutInflater inflater;
    private ArrayList<Integer> idiomas;

    public AdaptadorDialogoIdiomas(Context pcontext, ArrayList<Integer> pidiomas)
    {
        contexto = pcontext;
        idiomas = pidiomas;
        inflater = (LayoutInflater) contexto.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return idiomas.size();
    }

    @Override
    public Object getItem(int i) {
        return idiomas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view=inflater.inflate(android.R.layout.simple_list_item_1,null);
        TextView idioma = (TextView) view.findViewById(android.R.id.text1);
        idioma.setText(idiomas.get(i));
        return view;
    }
}
