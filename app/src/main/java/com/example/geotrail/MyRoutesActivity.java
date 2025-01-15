package com.example.geotrail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
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

public class MyRoutesActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference mReferenceUser, mReferenceData;

    ImageButton itemSearch, itemFlag, itemAirPlane, itemProfile, itemSettings;
    ImageView imageViewMap;
    Spinner spinnerCity;
    ProgressBar progressBar;
    LinearLayout containerLayout, container;

    List<String> savedPlaces = new ArrayList<>();
    List<String> citiesList = new ArrayList<>();
    HashMap<String , String> mapsOfCities = new HashMap<>();

    String strBackActivity;
    Intent backActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_routes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mReferenceUser = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");


        imageViewMap = findViewById(R.id.imageViewMap);
        spinnerCity = findViewById(R.id.spinnerCity);
        container = findViewById(R.id.container);
        progressBar = findViewById(R.id.progressBar);
        containerLayout = findViewById(R.id.containerLayout);

        itemSearch = findViewById(R.id.itemSearch);
        itemFlag = findViewById(R.id.itemFlag);
        itemAirPlane = findViewById(R.id.itemAirPlane);
        itemProfile = findViewById(R.id.itemProfile);
        itemSettings = findViewById(R.id.itemSettings);



        containerLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        mReferenceData.child("Cities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                    String city = citySnapshot.getKey();
                    if (city != null) {
                        citiesList.add(city);
                        for (DataSnapshot detailSnapshot : citySnapshot.getChildren()) {
                            String cityDetail = detailSnapshot.getKey();
                            if (cityDetail != null && cityDetail.equals("map_url")) {
                                mapsOfCities.put(city,detailSnapshot.getValue(String.class));
                            }
                        }
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        CustomAdapter cityAdapter = new CustomAdapter(MyRoutesActivity.this, citiesList);
                        spinnerCity.setAdapter(cityAdapter);

                        mReferenceUser.child("user_savedPlaces").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                                    String place = placeSnapshot.getKey();
                                    if (place != null) {
                                        savedPlaces.add(place);
                                    }
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        containerLayout.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.GONE);

                                    }
                                },20);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                },20);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        spinnerCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                imageViewMap.setBackgroundResource(R.color.white);
                String city = spinnerCity.getSelectedItem().toString();
                List<String> cityPlaces = MainMethods.getCityPlaces().get(city);
                List<String> willShowPlaces = new ArrayList<>();
                if (cityPlaces != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    containerLayout.setVisibility(View.GONE);

                    String url = mapsOfCities.get(city);
                    if (url != null) {
                        Glide.with(MyRoutesActivity.this)
                                .load(url)
                                .into(imageViewMap);
                    }else {
                        Glide.with(MyRoutesActivity.this).clear(imageViewMap);
                        imageViewMap.setBackgroundResource(R.drawable.map_example);
                    }

                    container.removeAllViews();

                    for (String place : cityPlaces) {
                        if (savedPlaces.contains(place)) {
                            willShowPlaces.add(place);
                        }
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showPlaces(willShowPlaces);
                            progressBar.setVisibility(View.GONE);
                            containerLayout.setVisibility(View.VISIBLE);
                        }
                    },5);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        itemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRoutesActivity.this,FirstPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        itemFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRoutesActivity.this,SavingsActivity.class);
                startActivity(intent);
                finish();
            }
        });
        itemAirPlane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRoutesActivity.this,CityDetailsActivity.class);
                intent.putExtra("search","random");
                startActivity(intent);
                finish();
            }
        });
        itemProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent;
                if (mUser != null) {
                    intent = new Intent(MyRoutesActivity.this,EditProfileActivity.class);
                }else {
                    intent = new Intent(MyRoutesActivity.this,LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyRoutesActivity.this,SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void showPlaces(List<String> places) {
        int size = places.size();
        for (int i = 0 ; i < size ; i++) {
            LinearLayout linearLayout = new LinearLayout(MyRoutesActivity.this);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            // ImageView
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    dpToPx(30), // Genişlik
                    dpToPx(30)  // Yükseklik
            );
            imageParams.setMargins(dpToPx(3), 0, dpToPx(1), 0);
            imageView.setLayoutParams(imageParams);
            imageView.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageResource(R.drawable.menu_icon);

            // TextView
            TextView textView = new TextView(this);
            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            );
            textParams.setMargins(0, 0, dpToPx(3), 0);
            textView.setLayoutParams(textParams);
            textView.setText((i+1) +  ". " + places.get(i).toUpperCase());
            textView.setPadding(dpToPx(5), 0, 0, 0);

            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);

            if (i == 0) {
                imageView.setBackgroundResource(R.drawable.top_image_border);
                textView.setBackgroundResource(R.drawable.top_image_border);
            }else if (i == size - 1) {
                imageView.setBackgroundResource(R.drawable.bottom_image_border);
                textView.setBackgroundResource(R.drawable.bottom_image_border);
            }else {
                imageView.setBackgroundResource(R.color.xxxxxxx);
                textView.setBackgroundResource(R.color.xxxxxxx);
            }

            linearLayout.addView(imageView);
            linearLayout.addView(textView);

            container.addView(linearLayout);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MyRoutesActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }

    // DP'den PX'e dönüşüm
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}