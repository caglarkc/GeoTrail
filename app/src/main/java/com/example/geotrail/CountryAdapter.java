package com.example.geotrail;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CountryAdapter extends BaseAdapter {
    private Context context;
    private List<String> countryList;

    public CountryAdapter(Context context , List<String> countryList){
        this.context = context;
        this.countryList = countryList;
    }

    @Override
    public int getCount() {
        return countryList != null ? countryList.size() : 0 ;
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.item_country,viewGroup, false);

        TextView textName = rootView.findViewById(R.id.name);

        textName.setText(countryList.get(i));


        return rootView;
    }
}
