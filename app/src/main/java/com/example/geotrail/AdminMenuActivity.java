package com.example.geotrail;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class AdminMenuActivity extends AppCompatActivity {

    Button buttonAddCountryData, buttonAddCityData, buttonAddPlaceData, buttonAddMap, buttonAddActivityData;
    LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonAddCountryData = findViewById(R.id.buttonAddCountryData);
        buttonAddCityData = findViewById(R.id.buttonAddCityData);
        buttonAddPlaceData = findViewById(R.id.buttonAddPlaceData);
        buttonAddMap = findViewById(R.id.buttonAddMap);
        buttonAddActivityData = findViewById(R.id.buttonAddActivityData);
        container = findViewById(R.id.container);

        buttonAddCountryData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Fragment fragment = new AddCountryDataFragment();
                goToFragment(fragment);

            }
        });

        buttonAddCityData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Fragment fragment = new AddCityDataFragment();
                goToFragment(fragment);
            }
        });

        buttonAddPlaceData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Fragment fragment = new AddPlaceDataFragment();
                goToFragment(fragment);
            }
        });


        buttonAddMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Fragment fragment = new AddMapFragment();
                goToFragment(fragment);
            }
        });


        buttonAddActivityData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                container.setVisibility(View.GONE);
                Fragment fragment = new AddActivityDataFragment();
                goToFragment(fragment);
            }
        });

    }

    private void goToFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
        );
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            container.setVisibility(View.VISIBLE);
        } else {
            // Fragment yoksa LoginActivity'e yönlendir
            Intent intent = new Intent(AdminMenuActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }
}