package com.example.geotrail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
/*
* Data eklerken var mı diye kontrol etmeyi ekle, mesela place ekledıgınde hiçbir cityde o ısımde place olmaması lazım.
*
* */
public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference mReferenceData;

    ImageView imageContainer;

    HashMap<String, List<String>> citiesOfCountry = new HashMap<>();
    HashMap<String, List<String>> placesOfCity = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");

        imageContainer = findViewById(R.id.imageContainer);

        int number = getRandomNumber(1,13);
        String imagePath = "firstpage" + number;
        int imageResId = getResources().getIdentifier(imagePath, "drawable", getPackageName());

        imageContainer.setBackgroundResource(imageResId);

        mReferenceData.child("Countries").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot countrySnapshot : snapshot.getChildren()) {
                    String countryName = countrySnapshot.getKey();
                    List<String> cities = new ArrayList<>();
                    for (DataSnapshot detailSnapshot : countrySnapshot.getChildren()) {
                        String detail = detailSnapshot.getKey();
                        if (detail != null && detail.equals("Cities")) {
                            for (DataSnapshot citySnapshot : detailSnapshot.getChildren()) {
                                String cityName = citySnapshot.getKey();
                                cities.add(cityName);
                            }
                        }
                    }

                    citiesOfCountry.put(countryName,cities);

                }
                MainMethods.setCountryCities(citiesOfCountry);
                // 3 saniye bekleyip yeni sayfayı aç
                new Handler(Looper.getMainLooper()).postDelayed(() -> {

                    mReferenceData.child("Cities").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                                String cityName = citySnapshot.getKey();
                                List<String> places = new ArrayList<>();
                                for (DataSnapshot detailSnapshot : citySnapshot.getChildren()) {
                                    String detail = detailSnapshot.getKey();
                                    if (detail != null && detail.equals("Places")) {
                                        for (DataSnapshot placeSnapshot : detailSnapshot.getChildren()) {
                                            String placeName = placeSnapshot.getKey();
                                            places.add(placeName);
                                        }
                                    }
                                }

                                placesOfCity.put(cityName,places);

                            }
                            MainMethods.setCityPlaces(placesOfCity);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    // Yeni bir sayfaya geçiş yap


                                    normal();
                                    //admin();
                                }
                            },20);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }, 10);


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





    }

    public static int getRandomNumber(int start, int end) {
        Random random = new Random();
        return random.nextInt((end - start) + 1) + start;
    }

    private void normal() {
        Intent intent = new Intent(MainActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }

    private void admin() {
        Intent intent = new Intent(MainActivity.this, AdminMenuActivity.class);
        startActivity(intent);
        finish();
    }
}

