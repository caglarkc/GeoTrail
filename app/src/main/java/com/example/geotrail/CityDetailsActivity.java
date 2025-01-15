package com.example.geotrail;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class CityDetailsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference mReferenceData, mReferenceUser;

    ImageButton itemSearch, itemFlag, itemAirPlane, itemProfile, itemSettings;
    ProgressBar progressBar;
    LinearLayout container;

    HashMap<String , HashMap<String, String>> placesData = new HashMap<>();
    HashMap<String , List<String>> citiesData = new HashMap<>();
    List<String> places = new ArrayList<>();
    List<String> savedUserPlaces = new ArrayList<>();
    String city, country, mainDetail, imageUrl, mapUrl;
    boolean isSpecificSearch, isLogin;

    Intent backActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_city_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        isLogin = mUser != null;

        if (isLogin) {
            mReferenceUser = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        }
        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");


        container = findViewById(R.id.container);
        progressBar = findViewById(R.id.progressBar);

        itemSearch = findViewById(R.id.itemSearch);
        itemFlag = findViewById(R.id.itemFlag);
        itemAirPlane = findViewById(R.id.itemAirPlane);
        itemProfile = findViewById(R.id.itemProfile);
        itemSettings = findViewById(R.id.itemSettings);

        Intent getIntent = getIntent();
        city = getIntent.getStringExtra("city");
        country = getIntent.getStringExtra("country");
        mainDetail = getIntent.getStringExtra("main");
        imageUrl = getIntent.getStringExtra("image");
        mapUrl = getIntent.getStringExtra("map");

        container.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        if (city == null) {
            isSpecificSearch = false;
            backActivity = new Intent(CityDetailsActivity.this, FirstPageActivity.class);
        }else {
            isSpecificSearch = true;
            citiesData = MainMethods.getCityPlaces();
            places = citiesData.get(city);
            backActivity = new Intent(CityDetailsActivity.this, CityExploringActivity.class);
            backActivity.putExtra("country",country);

        }




        if (isSpecificSearch) {
            getPlaceData();

        }else {
            getRandomCity();
        }

        itemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityDetailsActivity.this,FirstPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        itemFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityDetailsActivity.this,SavingsActivity.class);
                intent.putExtra("search","random");
                startActivity(intent);
                finish();
            }
        });
        itemAirPlane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityDetailsActivity.this,CityDetailsActivity.class);
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
                    intent = new Intent(CityDetailsActivity.this,EditProfileActivity.class);
                }else {
                    intent = new Intent(CityDetailsActivity.this,LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityDetailsActivity.this,SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
    private void getPlaceData() {
        mReferenceData.child("Places").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    String place = placeSnapshot.getKey();
                    if (places.contains(place)) {
                        HashMap<String, String> placeData = new HashMap<>();
                        for (DataSnapshot detailSnapshot : placeSnapshot.getChildren()) {
                            String detail = detailSnapshot.getKey();
                            if (detail != null) {
                                if (detail.equals("entrance_fee")) {
                                    String val = detailSnapshot.getValue(String.class);
                                    placeData.put("entrance_fee",val);
                                }else if (detail.equals("opening_hours")) {
                                    String val = detailSnapshot.getValue(String.class);
                                    placeData.put("opening_hours",val);
                                }else if (detail.equals("main_detail")) {
                                    String val = detailSnapshot.getValue(String.class);
                                    placeData.put("main_detail",val);
                                }else if (detail.equals("image_url")) {
                                    String val = detailSnapshot.getValue(String.class);
                                    placeData.put("image_url",val);
                                }
                            }
                        }
                        placesData.put(place,placeData);
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isLogin) {
                            mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                                            String detail = detailSnapshot.getKey();
                                            if (detail != null && detail.equals("user_savedPlaces")) {
                                                for (DataSnapshot placeSnapshot : detailSnapshot.getChildren()) {
                                                    String place = placeSnapshot.getKey();
                                                    if (place != null) {
                                                        savedUserPlaces.add(place);
                                                    }
                                                }
                                            }
                                        }
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                FrameLayout frameLayout = getCity();
                                                container.addView(frameLayout);

                                                generatePlaces();

                                                FrameLayout frameLayout2 = createCityMapLayout();
                                                container.addView(frameLayout2);


                                                container.setVisibility(View.VISIBLE);
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        },10);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            FrameLayout frameLayout = getCity();
                            container.addView(frameLayout);

                            generatePlaces();

                            FrameLayout frameLayout2 = createCityMapLayout();
                            container.addView(frameLayout2);


                            container.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }

                    }
                },10);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private FrameLayout getCity() {
        FrameLayout frameLayout = new FrameLayout(CityDetailsActivity.this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));

        ImageView imageViewCity = new ImageView(CityDetailsActivity.this);
        imageViewCity.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(300)
        ));
        imageViewCity.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(CityDetailsActivity.this)
                .load(imageUrl)
                .into(imageViewCity);

        frameLayout.addView(imageViewCity);

        // LinearLayout oluştur
        LinearLayout linearLayout = new LinearLayout(CityDetailsActivity.this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                dpToPx(250), // Genişlik 250dp
                LinearLayout.LayoutParams.WRAP_CONTENT // Yükseklik wrap_content
        );
        linearLayoutParams.setMargins(dpToPx(20), dpToPx(250), 0, 0); // Margin'leri ayarla
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL); // Dikey düzen
        linearLayout.setPadding(dpToPx(8), 0, dpToPx(6), dpToPx(8)); // Padding değerleri
        linearLayout.setBackgroundColor(getResources().getColor(R.color.white)); // Arka plan rengi

        // İlk TextView oluştur
        TextView textViewTitle = new TextView(CityDetailsActivity.this);
        textViewTitle.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textViewTitle.setText(city.toUpperCase() + ",");
        textViewTitle.setTextColor(getResources().getColor(R.color.black));
        textViewTitle.setTextSize(48);

        // İkinci TextView oluştur
        TextView textViewDescription = new TextView(CityDetailsActivity.this);
        textViewDescription.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textViewDescription.setText(mainDetail);
        textViewDescription.setTextColor(getResources().getColor(R.color.black));
        textViewDescription.setTextSize(14);

        // TextView'leri LinearLayout'a ekle
        linearLayout.addView(textViewTitle);
        linearLayout.addView(textViewDescription);


        frameLayout.addView(linearLayout);

        return frameLayout;
    }

    @SuppressLint("ClickableViewAccessibility")
    private FrameLayout createCityMapLayout() {
        FrameLayout frameLayout = new FrameLayout(CityDetailsActivity.this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        ));


        // Ana LinearLayout oluştur
        LinearLayout mainLayout = new LinearLayout(this);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainParams.setMargins(dpToPx(10), 0, dpToPx(10), 0);
        mainLayout.setLayoutParams(mainParams);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // TextView oluştur
        TextView titleTextView = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        titleTextView.setLayoutParams(titleParams);
        titleTextView.setText("CITY MAP");
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTextSize(22);
        titleTextView.setTypeface(null, Typeface.BOLD);

        ImageButton mapImageButton = new ImageButton(CityDetailsActivity.this);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(234)
        );
        mapImageButton.setPadding(dpToPx(6),dpToPx(6),dpToPx(6),dpToPx(6));
        mapImageButton.setLayoutParams(imageParams);
        mapImageButton.setBackgroundResource(R.color.white);
        Glide.with(CityDetailsActivity.this)
                .load(mapUrl)
                .into(mapImageButton);
        mapImageButton.setScaleType(ImageView.ScaleType.FIT_XY);

        mapImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    // Dokunulan yerin X ve Y koordinatlarını al
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();

                    // Log ile koordinatları kontrol et
                    Log.d("Erebus", "X: " + x + ", Y: " + y);

                    // Tıklanan koordinatlara bir view eklemek isterseniz:

                    return true; // İşlem tamamlandı
                }
                return false; // Diğer olaylara devam et
            }
        });


        /*
        ayasfoya X: 581.97363, Y: 488.9082
        galata X: 584.95215, Y: 473.92773
        rumeli hisarı X: 639.9424, Y: 425.9248
        şükrü saraçoğlu X: 627.9541, Y: 505.87402
         */
        // Ana layout'a TextView ve CardView'i ekle
        mainLayout.addView(titleTextView);
        mainLayout.addView(mapImageButton);


        frameLayout.addView(mainLayout);
        return frameLayout;
    }

    private void generatePlaces() {
        int counter = 0;
        for (String place : places) {
            counter += 1;
            HashMap<String , String> data = placesData.get(place);
            String entrance = data.getOrDefault("entrance_fee","");
            String opening = data.getOrDefault("opening_hours","");
            String main = data.getOrDefault("main_detail","");
            String url = data.getOrDefault("image_url","");

            LinearLayout linearLayout =  createPlaceLayout(counter,place,entrance,opening,main,url);

            container.addView(linearLayout);

        }
    }

    private LinearLayout createPlaceLayout(int counter,String place, String entrance, String opening, String main, String url) {
        // Ana LinearLayout oluştur
        LinearLayout mainLayout = new LinearLayout(this);
        LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        mainParams.setMargins(dpToPx(10), 0, dpToPx(10), 0);
        mainLayout.setLayoutParams(mainParams);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        // İlk alt layout (Vertical LinearLayout) oluştur
        LinearLayout firstVerticalLayout = new LinearLayout(this);
        firstVerticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        firstVerticalLayout.setOrientation(LinearLayout.VERTICAL);

        // İlk horizontal LinearLayout oluştur
        LinearLayout horizontalLayout = new LinearLayout(this);
        LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        horizontalParams.topMargin = dpToPx(20);
        horizontalLayout.setLayoutParams(horizontalParams);
        horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);

        // ImageButton oluştur
        ImageButton imageButton = new ImageButton(this);
        LinearLayout.LayoutParams imageButtonParams = new LinearLayout.LayoutParams(
                dpToPx(30),
                dpToPx(30)
        );
        final boolean[] isClicked = {false};
        imageButton.setLayoutParams(imageButtonParams);
        imageButton.setBackgroundResource(R.drawable.empty_save);
        if (isLogin) {
            if (savedUserPlaces.contains(place)) {
                isClicked[0] = true;
                imageButton.setBackgroundResource(R.drawable.full_save_button_icon);
            }
        }
        imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageButton.setBackgroundTintMode(null);


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin) {
                    if (!isClicked[0]) {
                        isClicked[0] = true;
                        imageButton.setBackgroundResource(R.drawable.full_save_button_icon);
                        progressBar.setVisibility(View.VISIBLE);
                        container.setVisibility(View.GONE);
                        mReferenceUser.child("user_savedPlaces").child(place).setValue("true").addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                container.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(CityDetailsActivity.this,"Başarı ile " + place + " kaydedildi.",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(CityDetailsActivity.this,"HATA",Toast.LENGTH_SHORT).show();
                                    Log.d("Erebus",task.getException().toString());
                                }
                            }
                        });
                    }else {
                        isClicked[0] = false;
                        imageButton.setBackgroundResource(R.drawable.empty_save);
                        progressBar.setVisibility(View.VISIBLE);
                        container.setVisibility(View.GONE);
                        mReferenceUser.child("user_savedPlaces").child(place).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                container.setVisibility(View.VISIBLE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(CityDetailsActivity.this,"Başarı ile " + place + " kaldırıldı.",Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(CityDetailsActivity.this,"HATA",Toast.LENGTH_SHORT).show();
                                    Log.d("Erebus",task.getException().toString());
                                }
                            }
                        });
                    }
                }else {
                    Toast.makeText(CityDetailsActivity.this,"Kaydetmek icin giris yapmalisiniz...",Toast.LENGTH_SHORT).show();
                }

            }
        });

        // TextView oluştur (1. COLOSSEUM)
        TextView titleTextView = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(30)
        );
        titleTextView.setLayoutParams(titleParams);
        String val = counter + ". " + place.toUpperCase();
        titleTextView.setText(val);
        titleTextView.setTextColor(getResources().getColor(R.color.black));
        titleTextView.setTextSize(16);
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setGravity(Gravity.CENTER);

        // Horizontal layout'a ekle
        horizontalLayout.addView(imageButton);
        horizontalLayout.addView(titleTextView);

        // İlk CardView oluştur
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
        );
        cardParams.topMargin = dpToPx(6);
        cardView.setLayoutParams(cardParams);

        // ImageView oluştur (CardView'in içinde)
        ImageView imageView = new ImageView(this);
        LinearLayout.LayoutParams imageViewParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(188)
        );
        imageViewParams.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), 0);
        imageView.setLayoutParams(imageViewParams);
        Glide.with(CityDetailsActivity.this)
                .load(url)
                .into(imageView);

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        // CardView'e ImageView'i ekle
        cardView.addView(imageView);

        // Açıklama TextView'i oluştur
        TextView descriptionTextView = new TextView(this);
        LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descriptionParams.setMargins(dpToPx(6), dpToPx(6), dpToPx(6), 0);
        descriptionTextView.setLayoutParams(descriptionParams);
        descriptionTextView.setText(main);
        descriptionTextView.setTextColor(getResources().getColor(R.color.black));
        descriptionTextView.setTextSize(14);

        // İlk Vertical Layout'a elemanları ekle
        firstVerticalLayout.addView(horizontalLayout);
        firstVerticalLayout.addView(cardView);
        firstVerticalLayout.addView(descriptionTextView);

        // İkinci alt layout (Vertical LinearLayout) oluştur
        LinearLayout secondVerticalLayout = new LinearLayout(this);
        secondVerticalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        secondVerticalLayout.setOrientation(LinearLayout.VERTICAL);

        // Ücret ve saat bilgisi TextView'lerini oluştur
        TextView feeTitle = new TextView(this);
        feeTitle.setText("Entrance fee:");
        feeTitle.setTextColor(getResources().getColor(R.color.black));
        feeTitle.setTextSize(14);
        feeTitle.setTypeface(null, Typeface.BOLD);
        feeTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        feeTitle.setPadding(0, dpToPx(10), 0, 0);

        TextView feeValue = new TextView(this);
        feeValue.setText(entrance);
        feeValue.setTextSize(14);
        feeValue.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        feeValue.setPadding(dpToPx(12), 0, 0, 0);

        TextView hoursTitle = new TextView(this);
        hoursTitle.setText("Opening hours:");
        hoursTitle.setTextColor(getResources().getColor(R.color.black));
        hoursTitle.setTextSize(14);
        hoursTitle.setTypeface(null, Typeface.BOLD);
        hoursTitle.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        hoursTitle.setPadding(0, dpToPx(10), 0, 0);

        TextView hoursValue = new TextView(this);
        hoursValue.setText(opening);
        hoursValue.setTextSize(14);
        hoursValue.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        hoursValue.setPadding(dpToPx(12), 0, 0, 0);

        // İkinci Layout'a TextView'leri ekle
        secondVerticalLayout.addView(feeTitle);
        secondVerticalLayout.addView(feeValue);
        secondVerticalLayout.addView(hoursTitle);
        secondVerticalLayout.addView(hoursValue);

        // Ana Layout'a alt layout'ları ekle
        mainLayout.addView(firstVerticalLayout);
        mainLayout.addView(secondVerticalLayout);

        return mainLayout;
    }

    private FrameLayout createPin(String number, float x, float y) {
        FrameLayout frameLayout = new FrameLayout(CityDetailsActivity.this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(x) , dpToPx(y) , 0 ,0);
        frameLayout.setLayoutParams(layoutParams);

        TextView textView = new TextView(CityDetailsActivity.this);
        textView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(number);
        textView.setTextColor(Color.WHITE);

        ImageView imageView = new ImageView(CityDetailsActivity.this);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                dpToPx(16),
                dpToPx(16)
        ));
        imageView.setBackgroundResource(R.drawable.location_pin);

        frameLayout.addView(textView);
        frameLayout.addView(imageView);



        return frameLayout;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(backActivity);
        finish();
    }



    // DP'den PX'e dönüşüm
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void getRandomCity() {
        List<String> allCities = new ArrayList<>();
        HashMap<String , List<String>> data = MainMethods.getCountryCities();
        for (Map.Entry<String , List<String>> entry : data.entrySet()) {
            String country = entry.getKey();
            List<String> cities = entry.getValue();

            allCities.addAll(cities);
        }

        Random random = new Random();
        String randomCity  = allCities.get(random.nextInt(allCities.size()));

        getRandomCityData(randomCity);



    }

    private void getRandomCityData(String val) {
        places = MainMethods.getCityPlaces().get(val);
        city = val;

        mReferenceData.child("Cities").child(val).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                    String detail = detailSnapshot.getKey();
                    if (detail != null) {
                        if (detail.equals("country")) {
                            country = detailSnapshot.getValue(String.class);
                        }else if (detail.equals("image_url")) {
                            imageUrl = detailSnapshot.getValue(String.class);
                        }else if (detail.equals("main_detail")) {
                            mainDetail = detailSnapshot.getValue(String.class);
                        }else if (detail.equals("map_url")) {
                            mapUrl = detailSnapshot.getValue(String.class);
                        }
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getPlaceData();
                    }
                },20);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}