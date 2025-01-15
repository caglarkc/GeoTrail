package com.example.geotrail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class SavingsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    DatabaseReference mReferenceUser, mReferenceData;
    ImageButton itemSearch, itemFlag, itemAirPlane, itemProfile, itemSettings;

    ProgressBar progressBar;
    Button buttonMyRoutes;
    LinearLayout container;
    HashMap<String, HashMap<String , String>> placesData = new HashMap<>();
    List<String> cityList = new ArrayList<>();
    List<String> placeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_savings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mReferenceUser = FirebaseDatabase.getInstance().getReference("Users").child(mUser.getUid());
        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");


        itemSearch = findViewById(R.id.itemSearch);
        itemFlag = findViewById(R.id.itemFlag);
        itemAirPlane = findViewById(R.id.itemAirPlane);
        itemProfile = findViewById(R.id.itemProfile);
        itemSettings = findViewById(R.id.itemSettings);

        progressBar = findViewById(R.id.progressBar);
        buttonMyRoutes = findViewById(R.id.buttonMyRoutes);
        container = findViewById(R.id.container);

        progressBar.setVisibility(View.VISIBLE);
        container.setVisibility(View.GONE);

        itemSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingsActivity.this,FirstPageActivity.class);
                startActivity(intent);
                finish();
            }
        });
        itemFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(SavingsActivity.this,"You are already in this page...",Toast.LENGTH_SHORT).show();
            }
        });
        itemAirPlane.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingsActivity.this,CityDetailsActivity.class);
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
                    intent = new Intent(SavingsActivity.this,EditProfileActivity.class);
                }else {
                    intent = new Intent(SavingsActivity.this,LoginActivity.class);
                }

                startActivity(intent);
                finish();
            }
        });
        itemSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingsActivity.this,SettingsActivity.class);
                startActivity(intent);
                finish();
            }
        });


        buttonMyRoutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SavingsActivity.this,MyRoutesActivity.class);
                startActivity(intent);
                finish();
            }
        });


        getData();
    }


    // DP'den PX'e dönüşüm
    private int dpToPx(float dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void getData() {
        List<String> places = new ArrayList<>();
        mReferenceUser.child("user_savedPlaces").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                    String place = placeSnapshot.getKey();
                    if (place != null) {
                        places.add(place);
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mReferenceData.child("Places").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot placeSnapshot : snapshot.getChildren()) {
                                    String place = placeSnapshot.getKey();
                                    HashMap<String , String> placeData = new HashMap<>();
                                    if (place != null) {
                                        if (places.contains(place)) {
                                            placeList.add(place);
                                            for (DataSnapshot detailSnapshot : placeSnapshot.getChildren()) {
                                                String detail = detailSnapshot.getKey();
                                                if (detail != null) {
                                                    if (detail.equals("country")) {
                                                        placeData.put("country",detailSnapshot.getValue(String.class));
                                                    }else if (detail.equals("city")) {
                                                        if (!cityList.contains(detailSnapshot.getValue(String.class))) {
                                                            cityList.add(detailSnapshot.getValue(String.class));
                                                        }
                                                        placeData.put("city",detailSnapshot.getValue(String.class));
                                                    }else if (detail.equals("entrance_fee")) {
                                                        placeData.put("entrance_fee",detailSnapshot.getValue(String.class));
                                                    }else if (detail.equals("opening_hours")) {
                                                        placeData.put("opening_hours",detailSnapshot.getValue(String.class));
                                                    }else if (detail.equals("image_url")) {
                                                        placeData.put("image_url",detailSnapshot.getValue(String.class));
                                                    }else if (detail.equals("main_detail")) {
                                                        placeData.put("main_detail",detailSnapshot.getValue(String.class));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    placesData.put(place,placeData);
                                }
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        createLayouts();
                                    }
                                },20);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                },10);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void createLayouts() {
        for (String city : cityList) {
            LinearLayout cityLayout = createCityLayout(city);

            container.addView(cityLayout);
        }
        progressBar.setVisibility(View.GONE);
        container.setVisibility(View.VISIBLE);
    }

    private LinearLayout createCityLayout(String cityName) {
        final boolean[] isClicked = {false};
        LinearLayout cityLayout = new LinearLayout(SavingsActivity.this);
        LinearLayout.LayoutParams cityLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cityLayoutParams.setMargins(0,dpToPx(15),0,0);
        cityLayout.setLayoutParams(cityLayoutParams);
        cityLayout.setOrientation(LinearLayout.VERTICAL);


        LinearLayout cityCapitalLayout = new LinearLayout(SavingsActivity.this);
        cityCapitalLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        cityCapitalLayout.setOrientation(LinearLayout.HORIZONTAL);


        ImageButton imageButtonCityTick = new ImageButton(SavingsActivity.this);
        imageButtonCityTick.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(36),dpToPx(36)));
        imageButtonCityTick.setBackgroundResource(R.drawable.down_arrow);
        imageButtonCityTick.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView textViewCityName = new TextView(SavingsActivity.this);
        textViewCityName.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        textViewCityName.setGravity(Gravity.CENTER);
        textViewCityName.setTextColor(Color.BLACK);
        textViewCityName.setTextSize(24);
        textViewCityName.setText(cityName);

        cityCapitalLayout.addView(imageButtonCityTick);
        cityCapitalLayout.addView(textViewCityName);

        List<String> cityPlaces = MainMethods.getCityPlaces().get(cityName);

        cityLayout.addView(cityCapitalLayout);

        LinearLayout tempLayout = new LinearLayout(SavingsActivity.this);
        tempLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        tempLayout.setOrientation(LinearLayout.VERTICAL);

        for (String place : cityPlaces) {
            if (placeList.contains(place)) {
                HashMap<String, String> placeData = placesData.get(place);
                String url = placeData.get("image_url");
                String main = placeData.get("main_detail");
                String openingHours = placeData.get("opening_hours");
                String entranceFee = placeData.get("entrance_fee");
                LinearLayout linearLayout = createPlaceLayout(place,url,main,entranceFee,openingHours);

                tempLayout.addView(linearLayout);
            }

        }

        cityLayout.addView(tempLayout);
        imageButtonCityTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isClicked[0]) {
                    imageButtonCityTick.setBackgroundResource(R.drawable.up_arrow);
                    tempLayout.setVisibility(View.GONE);
                    isClicked[0] = true;
                }else {
                    imageButtonCityTick.setBackgroundResource(R.drawable.down_arrow);
                    tempLayout.setVisibility(View.VISIBLE);
                    isClicked[0] = false;
                }
            }
        });

        return cityLayout;
    }

    private LinearLayout createPlaceLayout(String placeName, String placeUrl, String detail, String entranceFee, String openingHours) {
        final boolean[] isClicked = {false};
        Map<String , String> map = formatTextForTwoTextViews(detail);
         String text1 = map.get("text1");
        String text2 = map.get("text2");


        LinearLayout placeContainer = new LinearLayout(SavingsActivity.this);
        LinearLayout.LayoutParams placeContainerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        placeContainerParams.setMargins(dpToPx(30),dpToPx(10),0,0);
        placeContainer.setLayoutParams(placeContainerParams);
        placeContainer.setOrientation(LinearLayout.VERTICAL);


        LinearLayout placeCapitalContainer = new LinearLayout(SavingsActivity.this);
        placeCapitalContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        placeCapitalContainer.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton imageButtonPlaceTick = new ImageButton(SavingsActivity.this);
        imageButtonPlaceTick.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(36),dpToPx(36)));
        imageButtonPlaceTick.setBackgroundResource(R.drawable.down_arrow);
        imageButtonPlaceTick.setScaleType(ImageView.ScaleType.FIT_XY);

        TextView textViewPlaceName = new TextView(SavingsActivity.this);
        textViewPlaceName.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        textViewPlaceName.setGravity(Gravity.CENTER);
        textViewPlaceName.setTextColor(Color.BLACK);
        textViewPlaceName.setTextSize(24);
        textViewPlaceName.setText(placeName);

        placeCapitalContainer.addView(imageButtonPlaceTick);
        placeCapitalContainer.addView(textViewPlaceName);

        LinearLayout horizontal = new LinearLayout(SavingsActivity.this);
        horizontal.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(154)
        ));
        horizontal.setOrientation(LinearLayout.HORIZONTAL);

        CardView cardView = new CardView(SavingsActivity.this);
        cardView.setLayoutParams(new ViewGroup.LayoutParams(dpToPx(250),dpToPx(150)));

        ImageView placeImageView = new ImageView(SavingsActivity.this);
        LinearLayout.LayoutParams placeImageViewParams = new LinearLayout.LayoutParams(dpToPx(242),dpToPx(142));
        placeImageViewParams.setMargins(dpToPx(4),dpToPx(4),0,0);
        placeImageView.setLayoutParams(placeImageViewParams);
        Glide.with(SavingsActivity.this)
                .load(placeUrl)
                .into(placeImageView);
        placeImageView.setScaleType(ImageView.ScaleType.FIT_XY);

        cardView.addView(placeImageView);

        TextView textViewPart1 = new TextView(SavingsActivity.this);
        LinearLayout.LayoutParams part1Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        part1Params.setMargins(dpToPx(5),0,0,0);
        textViewPart1.setLayoutParams(part1Params);
        textViewPart1.setTextColor(Color.BLACK);
        textViewPart1.setTextSize(13);
        textViewPart1.setText(text1);

        horizontal.addView(cardView);
        horizontal.addView(textViewPart1);

        TextView textViewPart2 = new TextView(SavingsActivity.this);
        LinearLayout.LayoutParams part2Params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        part2Params.setMargins(0,dpToPx(5),0,0);
        textViewPart2.setLayoutParams(part2Params);
        textViewPart2.setTextColor(Color.BLACK);
        textViewPart2.setTextSize(13);
        textViewPart2.setText(text2);


        LinearLayout detailLayout = new LinearLayout(SavingsActivity.this);
        detailLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        detailLayout.setOrientation(LinearLayout.VERTICAL);

        TextView first = new TextView(SavingsActivity.this);
        LinearLayout.LayoutParams fParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        fParams.setMargins(0,dpToPx(10),0,0);
        first.setLayoutParams(fParams);
        first.setText("Entrance Fee:");
        first.setTextColor(Color.BLACK);
        first.setTextSize(14);
        first.setTypeface(null,Typeface.BOLD);

        TextView second = new TextView(SavingsActivity.this);
        LinearLayout.LayoutParams sParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        sParams.setMargins(dpToPx(12),0,0,0);
        second.setLayoutParams(sParams);
        second.setText(entranceFee);
        second.setTextSize(14);

        TextView third = new TextView(SavingsActivity.this);
        third.setLayoutParams(fParams);
        third.setText("Opening Hours:");
        third.setTextColor(Color.BLACK);
        third.setTextSize(14);
        third.setTypeface(null,Typeface.BOLD);

        TextView fourth = new TextView(SavingsActivity.this);
        fourth.setLayoutParams(sParams);
        fourth.setText(openingHours);
        fourth.setTextSize(14);

        detailLayout.addView(first);
        detailLayout.addView(second);
        detailLayout.addView(third);
        detailLayout.addView(fourth);

        LinearLayout lastLayout = new LinearLayout(SavingsActivity.this);
        lastLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        lastLayout.setOrientation(LinearLayout.VERTICAL);


        placeContainer.addView(placeCapitalContainer);

        imageButtonPlaceTick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isClicked[0]) {
                    imageButtonPlaceTick.setBackgroundResource(R.drawable.up_arrow);
                    lastLayout.setVisibility(View.GONE);
                    isClicked[0] = true;
                }else {
                    imageButtonPlaceTick.setBackgroundResource(R.drawable.down_arrow);
                    lastLayout.setVisibility(View.VISIBLE);
                    isClicked[0] = false;
                }
            }
        });

        lastLayout.addView(horizontal);
        lastLayout.addView(textViewPart2);
        lastLayout.addView(detailLayout);

        placeContainer.addView(lastLayout);

        return placeContainer;
    }


    public Map<String, String> formatTextForTwoTextViews(String input) {
        String[] words = input.split(" "); // Metni kelimelere böl
        StringBuilder text1Builder = new StringBuilder();
        StringBuilder currentLine = new StringBuilder();
        int currentLineCount = 0;

        // İlk metin için metni formatla
        int wordIndex = 0;
        for (; wordIndex < words.length; wordIndex++) {
            String word = words[wordIndex];
            // Eğer bu kelimeyi eklersek satır uzunluğu aşacaksa
            if (currentLine.length() + word.length() + 1 > 12) {
                // Satırı Text1'e ekle
                text1Builder.append(currentLine).append("\n");
                currentLine.setLength(0); // Yeni bir satır başlat
                currentLineCount++;

                // Eğer maksimum satır sayısına ulaşıldıysa döngüyü kır
                if (currentLineCount >= 10) {
                    break;
                }
            }

            // Mevcut satıra kelimeyi ekle
            if (currentLine.length() > 0) {
                currentLine.append(" "); // Kelimeler arasında boşluk ekle
            }
            currentLine.append(word);
        }

        // Eğer kalan kelimeler varsa, son satırı Text1'e ekle
        if (currentLine.length() > 0 && currentLineCount < 10) {
            text1Builder.append(currentLine);
        }

        // Geriye kalan kelimeleri Text2'ye ekle
        StringBuilder text2Builder = new StringBuilder();
        for (int i = wordIndex; i < words.length; i++) {
            if (text2Builder.length() > 0) {
                text2Builder.append(" ");
            }
            text2Builder.append(words[i]);
        }

        // Sonuçları Map ile döndür
        Map<String, String> result = new HashMap<>();
        result.put("text1", text1Builder.toString().trim());
        result.put("text2", text2Builder.toString().trim());
        return result;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SavingsActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }
}