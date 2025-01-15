package com.example.geotrail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirstPageActivity extends AppCompatActivity {

    SharedPreferences sharedUser;


    AutoCompleteTextView autoCompleteTextView;
    ImageButton imageButtonArrow, imageButtonSearch, itemSearch, itemFlag, itemAirPlane, itemProfile,
    itemSettings, imageButtonCountries;
    ConstraintLayout main;
    LinearLayout countryNamesContainer;

    String sharedUserUid;
    boolean isLogin;

    List<String> countryList = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_first_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        imageButtonArrow = findViewById(R.id.imageButtonArrow);
        imageButtonSearch = findViewById(R.id.imageButtonSearch);
        itemSearch = findViewById(R.id.itemSearch);
        itemFlag = findViewById(R.id.itemFlag);
        imageButtonCountries = findViewById(R.id.imageButtonCountries);
        itemAirPlane = findViewById(R.id.itemAirPlane);
        itemProfile = findViewById(R.id.itemProfile);
        itemSettings = findViewById(R.id.itemSettings);
        main = findViewById(R.id.main);
        countryNamesContainer = findViewById(R.id.countryNamesContainer);

        sharedUser = getSharedPreferences("user_data",MODE_PRIVATE);
        sharedUserUid = sharedUser.getString("user_uid","");


        if (sharedUserUid.equals("")) {
            isLogin = false;
        }else {
            isLogin = true;
        }

        String[] countries = {
                "United States of America" , "Italy" , "Japan" , "Turkey" , "Spain" , "Germany" , "France",
                "Belgium" , "Bosnia and Herzegovina" , "Serbia"
        };

        //Normalde bütün ülkeler eklenicek ama şuanlık demo oldugu için sadece 10 ülkenin datasını ekleyeceğiz...

        countryList = Arrays.asList(countries);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.custom_dropdown_item,
                countries
        );
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setDropDownWidth(400);

        itemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(FirstPageActivity.this,"Zaten bu sayfadasınız...",Toast.LENGTH_SHORT).show();
            }
        });

        itemFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin) {
                    Intent intent = new Intent(FirstPageActivity.this,SavingsActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(FirstPageActivity.this,"Bu sayfaya erişmek için önce giriş yapmalısınız...",Toast.LENGTH_SHORT).show();
                }

            }
        });

        itemAirPlane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstPageActivity.this,CityDetailsActivity.class);
                intent.putExtra("search","random");
                startActivity(intent);
                finish();
            }
        });

        itemProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                if (isLogin) {
                    intent = new Intent(FirstPageActivity.this,EditProfileActivity.class);
                }else {
                    intent = new Intent(FirstPageActivity.this,LoginActivity.class);
                }

                startActivity(intent);
                finish();

            }
        });

        itemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FirstPageActivity.this, SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });


        countryNamesContainer.setVisibility(View.GONE);
        for (String country : countryList) {
            // TextView (Başlangıçta görünmez olacak)
            TextView cityListTextView = new TextView(this);
            cityListTextView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            cityListTextView.setTextColor(Color.WHITE);
            cityListTextView.setTextSize(16);
            cityListTextView.setText(country);

            countryNamesContainer.addView(cityListTextView);

        }




        imageButtonCountries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countryNamesContainer.getVisibility() == View.GONE) {
                    countryNamesContainer.setVisibility(View.VISIBLE);
                } else {
                    countryNamesContainer.setVisibility(View.GONE);
                }
            }
        });

        // Ana Layout'a Tıklama Olayı (TextView'i Kapatmak İçin)
        main.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (countryNamesContainer.getVisibility() == View.VISIBLE) {
                    countryNamesContainer.setVisibility(View.GONE);
                }
            }
            return false;
        });

        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTxt = autoCompleteTextView.getText().toString();
                if (!TextUtils.isEmpty(locationTxt) && countryList.contains(locationTxt)) {
                    Intent intent = new Intent(FirstPageActivity.this, CityExploringActivity.class);
                    intent.putExtra("country",locationTxt);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(FirstPageActivity.this,"Lütfen bir ülke giriniz...",Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageButtonArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String locationTxt = autoCompleteTextView.getText().toString();
                if (!TextUtils.isEmpty(locationTxt)) {
                    Intent intent = new Intent(FirstPageActivity.this, CityExploringActivity.class);
                    intent.putExtra("search",locationTxt);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(FirstPageActivity.this,"Lütfen bir ülke giriniz...",Toast.LENGTH_SHORT).show();
                }

            }
        });




    }

}