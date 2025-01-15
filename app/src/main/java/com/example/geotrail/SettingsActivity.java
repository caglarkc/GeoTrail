package com.example.geotrail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {
    FirebaseAuth mAuth;

    SharedPreferences sharedUser;

    ImageView imageViewLogOut;
    Button buttonDarkMode, buttonSuggestLocation, buttonFeedback, buttonLogOut;

    String sharedUserUid;
    boolean isLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        buttonDarkMode = findViewById(R.id.buttonDarkMode);
        buttonSuggestLocation = findViewById(R.id.buttonSuggestLocation);
        buttonFeedback = findViewById(R.id.buttonFeedback);
        buttonLogOut = findViewById(R.id.buttonLogOut);
        imageViewLogOut = findViewById(R.id.imageViewLogOut);

        sharedUser = getSharedPreferences("user_data",MODE_PRIVATE);
        sharedUserUid = sharedUser.getString("user_uid","");

        if (sharedUserUid.equals("")) {
            isLogin = false;
            buttonLogOut.setText("Log in");
            imageViewLogOut.setImageResource(R.drawable.baseline_profile_24);
        }else {
            isLogin = true;
            buttonLogOut.setText("Log out");
            imageViewLogOut.setImageResource(R.drawable.logout_icon);
        }

        buttonLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin) {
                    SharedPreferences.Editor editor = sharedUser.edit();
                    editor.remove("user_uid");
                    editor.apply();
                    mAuth.signOut();
                    Toast.makeText(SettingsActivity.this,"",Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingsActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }
}