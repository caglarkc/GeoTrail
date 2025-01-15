package com.example.geotrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private List<String> list;

    public CustomAdapter(Context context , List<String> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list != null ? list.size() : 0 ;
    }

    @Override
    public Object getItem(int i) {
        return list.get(i); // Artık öğeyi döndürüyor
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item,viewGroup, false);

        TextView textName = rootView.findViewById(R.id.name);

        textName.setText(list.get(i));


        return rootView;
    }
}
