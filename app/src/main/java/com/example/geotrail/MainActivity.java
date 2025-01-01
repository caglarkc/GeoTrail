package com.example.geotrail;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    ImageView imageContainer;


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



        imageContainer = findViewById(R.id.imageContainer);

        int number = getRandomNumber(1,13);
        String imagePath = "firstpage" + number;
        int imageResId = getResources().getIdentifier(imagePath, "drawable", getPackageName());

        imageContainer.setBackgroundResource(imageResId);

        // 3 saniye bekleyip yeni sayfayı aç
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Yeni bir sayfaya geçiş yap
            Intent intent = new Intent(MainActivity.this, AdminMenuActivity.class);
            startActivity(intent);

            // Şu anki aktiviteyi kapatmak isterseniz
            finish();
        }, 30);



    }

    public static int getRandomNumber(int start, int end) {
        Random random = new Random();
        return random.nextInt((end - start) + 1) + start;
    }

}

