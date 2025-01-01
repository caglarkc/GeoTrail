package com.example.geotrail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class AddCountryDataFragment extends Fragment {
    public AddCountryDataFragment(){}

    Spinner spinnerCountry;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_country_data,container,false);

        spinnerCountry = view.findViewById(R.id.spinnerCountry);

        List<String> countryList = new ArrayList<>();
        countryList.add("Select Country");

        countryList.addAll(MainMethods.getCountryList());



        CountryAdapter adapter = new CountryAdapter(getContext(), countryList);
        spinnerCountry.setAdapter(adapter);


        return view;
    }
}
