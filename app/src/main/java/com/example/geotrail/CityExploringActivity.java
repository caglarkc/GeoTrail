package com.example.geotrail;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CityExploringActivity extends AppCompatActivity {

    DatabaseReference mReferenceData;
    StorageReference mReferenceStorage;

    Uri countryUri;
    ProgressBar progressBar;
    LinearLayout container;

    String countryName, lastCity;
    List<String> cities = new ArrayList<>();
    List<String> tempList = new ArrayList<>();
    HashMap<String , List<String>> citiesOfCountryHashMap = new HashMap<>();
    HashMap<String , HashMap<String , String>> citiesData = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_city_exploring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");
        mReferenceStorage = FirebaseStorage.getInstance().getReference("Data");

        Intent getIntent = getIntent();
        countryName = getIntent.getStringExtra("country");

        container = findViewById(R.id.container);
        progressBar = findViewById(R.id.progressBar);

        citiesOfCountryHashMap = MainMethods.getCountryCities();
        if (citiesOfCountryHashMap.get(countryName) != null) {
            cities = citiesOfCountryHashMap.get(countryName);
        }


        progressBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);

        mReferenceStorage.child("Countries")
                .child(countryName)
                .child("CountryImage")
                .child("Image")
                .getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            countryUri = task.getResult();
                            FrameLayout frameLayout = countryContainer();
                            container.addView(frameLayout);

                        }else {
                            Log.d("Erebus",task.getException().toString());
                            int drawableResId = R.drawable.empty_image_icon; // Görselin kaynak ID'si
                            countryUri = Uri.parse("android.resource://" + getPackageName() + "/" + drawableResId);

                            FrameLayout frameLayout = countryContainer();
                            container.addView(frameLayout);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getCitiesData();

                            }
                        },30);
                    }
                });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CityExploringActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }


    private void getCitiesData() {
        mReferenceData.child("Cities").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                        if (citySnapshot != null) {
                            String cityName = citySnapshot.getKey();
                            if (cities.contains(cityName)) {
                                HashMap<String , String> cityData = new HashMap<>();
                                for (DataSnapshot detailSnapshot : citySnapshot.getChildren()) {
                                    String detail = detailSnapshot.getKey();
                                    if (detail!= null) {
                                        if (detail.equals("tiny_detail")) {
                                            String val = detailSnapshot.getValue(String.class);
                                            cityData.put("tiny",val);
                                        }else if (detail.equals("image_url")) {
                                            String val = detailSnapshot.getValue(String.class);
                                            cityData.put("image",val);
                                        }else if (detail.equals("main_detail")) {
                                            String val = detailSnapshot.getValue(String.class);
                                            cityData.put("main",val);
                                        }else if (detail.equals("map_url")) {
                                            String val = detailSnapshot.getValue(String.class);
                                            cityData.put("map",val);
                                        }
                                    }
                                }

                                citiesData.put(cityName,cityData);
                            }
                        }
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showCountry();
                    }
                },10);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void showCountry() {
        lastCity = "Null";

        // Eğer şehir sayısı tekse son şehri ayır ve listeden kaldır
        if (cities.size() % 2 != 0) {
            lastCity = cities.get(cities.size() - 1);
            tempList = new ArrayList<>(cities);
            tempList.remove(tempList.size() - 1);
        }

        int counter = 1;
        String oldCity = "";

        for (String city : tempList) {
            if (counter == 2) {
                // Yeni layout ve aralık oluşturma
                Space space = new Space(CityExploringActivity.this);
                space.setLayoutParams(new ViewGroup.LayoutParams(
                        dpToPx(15),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                ));

                LinearLayout horizontalLayout = new LinearLayout(CityExploringActivity.this);
                LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                horizontalParams.topMargin = dpToPx(20);
                horizontalLayout.setLayoutParams(horizontalParams);

                // İlk şehrin verilerini HashMap'ten al
                HashMap<String, String> cityData1 = citiesData.get(oldCity);
                HashMap<String, String> cityData2 = citiesData.get(city);

                String city1Tiny = cityData1.getOrDefault("tiny", "");
                String city1ImageUrl = cityData1.getOrDefault("image", "");
                String city1Main = cityData1.getOrDefault("main", "");
                String city1Map = cityData1.getOrDefault("map", "");
                String city2Tiny = cityData2.getOrDefault("tiny", "");
                String city2ImageUrl = cityData2.getOrDefault("image", "");
                String city2Main = cityData2.getOrDefault("main", "");
                String city2Map = cityData2.getOrDefault("map", "");

                // İlk şehir için container oluştur
                LinearLayout containerCity1 = cityContainer(oldCity, city1Tiny, city1ImageUrl,city1Main, city1Map);
                // İkinci şehir için container oluştur
                LinearLayout containerCity2 = cityContainer(city, city2Tiny, city2ImageUrl,city2Main,city2Map);

                // Şehirleri horizontal layout'a ekle
                horizontalLayout.addView(containerCity1);
                horizontalLayout.addView(space);
                horizontalLayout.addView(containerCity2);

                // Ana container'a horizontal layout'u ekle
                container.addView(horizontalLayout);

                counter = 1; // Sıfırla
            } else {
                oldCity = city; // Eski şehri kaydet
                counter += 1;
            }
        }

        // Eğer son şehir varsa, onu işleme
        if (!lastCity.equals("Null")) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int widthPixels = displayMetrics.widthPixels;
            int width = (widthPixels / 2) - dpToPx(20) ;

            LinearLayout horizontalLayout = new LinearLayout(CityExploringActivity.this);
            LinearLayout.LayoutParams horizontalParams = new LinearLayout.LayoutParams(
                    width,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            horizontalParams.topMargin = dpToPx(20);
            horizontalLayout.setLayoutParams(horizontalParams);

            // Son şehrin verilerini HashMap'ten al
            HashMap<String, String> lastCityData = citiesData.get(lastCity);
            String lastCityTiny = lastCityData.getOrDefault("tiny", "");
            String lastCityImageUrl = lastCityData.getOrDefault("image", "");
            String lastCityMain = lastCityData.getOrDefault("main", "");
            String lastCityMap = lastCityData.getOrDefault("map", "");

            // Son şehir için container oluştur
            LinearLayout lastCityContainer = cityContainer(lastCity, lastCityTiny, lastCityImageUrl,lastCityMain,lastCityMap);

            horizontalLayout.addView(lastCityContainer);

            // Ana container'a horizontal layout'u ekle
            container.addView(horizontalLayout);
        }


        container.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

    }

    private LinearLayout cityContainer(String name, String detail, String url, String mainDetail, String mapUrl) {
        // Ana LinearLayout oluşturma
        LinearLayout linearLayout = new LinearLayout(CityExploringActivity.this);
        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
                0, // width
                LinearLayout.LayoutParams.WRAP_CONTENT, // height
                1.0f // layout_weight
        );
        linearLayout.setLayoutParams(linearLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.layout_border));

        // TextView (cityCapital) oluşturma
        TextView cityCapital = new TextView(this);
        cityCapital.setId(View.generateViewId());
        LinearLayout.LayoutParams cityCapitalParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cityCapitalParams.setMargins(dpToPx(10), dpToPx(5), 0, 0);
        cityCapital.setLayoutParams(cityCapitalParams);
        cityCapital.setTextColor(ContextCompat.getColor(this, R.color.black));
        cityCapital.setTextSize(20);
        cityCapital.setTypeface(cityCapital.getTypeface(), Typeface.BOLD_ITALIC);
        cityCapital.setText(name);

        // TextView'i LinearLayout'a ekleme
        linearLayout.addView(cityCapital);

        // ImageView (cityImage) oluşturma
        ImageView cityImage = new ImageView(this);
        cityImage.setId(View.generateViewId());
        LinearLayout.LayoutParams cityImageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(120) // Yükseklik
        );
        cityImage.setLayoutParams(cityImageParams);
        cityImage.setScaleType(ImageView.ScaleType.FIT_XY);
        if (url.equals("")) {
            cityImage.setBackgroundResource(R.drawable.map_example);
        }else {
            Glide.with(CityExploringActivity.this)
                    .load(url)
                    .into(cityImage);
        }

        // ImageView'i LinearLayout'a ekleme
        linearLayout.addView(cityImage);

        // TextView (cityExplain) oluşturma
        TextView cityExplain = new TextView(this);
        cityExplain.setId(View.generateViewId());
        LinearLayout.LayoutParams cityExplainParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dpToPx(130)
        );
        cityExplainParams.setMargins(0, dpToPx(5), 0, 0);
        cityExplain.setLayoutParams(cityExplainParams);
        cityExplain.setTextColor(ContextCompat.getColor(this, R.color.black));
        cityExplain.setTextSize(14);
        cityExplain.setTypeface(cityExplain.getTypeface(), Typeface.BOLD_ITALIC);
        cityExplain.setPadding(dpToPx(6),0,dpToPx(6),0);
        if (detail.equals("")) {
            detail = "Empty detail...";
        }
        cityExplain.setText(detail);

        // TextView'i LinearLayout'a ekleme
        linearLayout.addView(cityExplain);

        // Button (Explore) oluşturma
        Button exploreButton = new Button(this);
        exploreButton.setId(View.generateViewId());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                dpToPx(100), // width
                dpToPx(30)   // height
        );
        buttonParams.setMargins(0, 0, dpToPx(5), dpToPx(10));
        buttonParams.gravity = Gravity.END; // Button'u sağa yaslama
        exploreButton.setLayoutParams(buttonParams);
        exploreButton.setBackgroundColor(ContextCompat.getColor(this, R.color.background_color));
        exploreButton.setText("Explore");
        exploreButton.setPadding(0,0,0,0);
        exploreButton.setTextColor(ContextCompat.getColor(this, R.color.white));
        exploreButton.setAllCaps(false);

        exploreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CityExploringActivity.this,CityDetailsActivity.class);
                intent.putExtra("city",name);
                intent.putExtra("main",mainDetail);
                intent.putExtra("image",url);
                intent.putExtra("country",countryName);
                intent.putExtra("map",mapUrl);
                startActivity(intent);
                finish();
            }
        });

        // Button'u LinearLayout'a ekleme
        linearLayout.addView(exploreButton);

        return linearLayout;
    }

    private FrameLayout countryContainer() {

        FrameLayout frameLayout = new FrameLayout(CityExploringActivity.this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200)
        ));

        CardView cardView = new CardView(CityExploringActivity.this);
        // CardView boyutları ve özellikleri
        LinearLayout.LayoutParams cardLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(200) // 200dp'yi px'e çevir
        );
        cardView.setLayoutParams(cardLayoutParams);
        cardView.setRadius(dpToPx(15)); // Köşe yuvarlama
        cardView.setCardElevation(dpToPx(4)); // Gölgelendirme

        ImageView imageView = new ImageView(CityExploringActivity.this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        Glide.with(CityExploringActivity.this)
                .load(countryUri.toString())
                .into(imageView);

        cardView.addView(imageView);

        // TextView oluştur
        TextView textView = new TextView(CityExploringActivity.this);

        // TextView özelliklerini ayarla
        textView.setText(countryName.toUpperCase()); // Metin
        textView.setTextColor(getResources().getColor(R.color.white)); // Yazı rengi
        textView.setTextSize(36); // Yazı boyutu (sp olarak)
        textView.setTypeface(textView.getTypeface(), android.graphics.Typeface.BOLD_ITALIC); // Yazı stili
        textView.setElevation(5); // Yükselti (elevation)

        // TextView layout özellikleri
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        textLayoutParams.setMargins(dpToPx(10), dpToPx(5), 0, 0); // Margin ekleme
        textView.setLayoutParams(textLayoutParams);

        textView.setZ(1);
        cardView.setZ(0);

        frameLayout.addView(cardView);
        frameLayout.addView(textView);


        return frameLayout;
    }


    // DP'den PX'e dönüşüm
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}

