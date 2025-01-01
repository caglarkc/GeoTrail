package com.example.geotrail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class FirstPageActivity extends AppCompatActivity {

    SharedPreferences sharedUser;


    AutoCompleteTextView autoCompleteTextView;
    ImageButton imageButtonArrow, imageButtonSearch, itemSearch, itemFlag, itemAirPlane, itemProfile,
    itemSettings;

    String sharedUserUid;
    boolean isLogin;

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

        itemAirPlane = findViewById(R.id.itemAirPlane);
        itemProfile = findViewById(R.id.itemProfile);
        itemSettings = findViewById(R.id.itemSettings);

        sharedUser = getSharedPreferences("user_data",MODE_PRIVATE);
        sharedUserUid = sharedUser.getString("user_uid","");
        Log.d("EREBUS",sharedUserUid);


        if (sharedUserUid.equals("")) {
            isLogin = false;
        }else {
            isLogin = true;
        }

        String[] countries = {
                "United States of America" , "Italy" , "Japan" , "Turkey" , "Spain" , "Germany" , "France",
                "Belgium" , "Bosnia and Herzegovina" , "Serbia"
        };

        /**
         * Normalde bütün ülkeler eklenicek ama şuanlık demo oldugu için sadece 10 ülkenin datasını ekleyeceğiz...
         */
        /*
        String[] countries = {
                "Afghanistan", "Albania", "Algeria", "Andorra", "Angola", "Antigua and Barbuda", "Argentina", "Armenia",
                "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus",
                "Belgium", "Belize", "Benin", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Brazil",
                "Brunei", "Bulgaria", "Burkina Faso", "Burundi", "Cabo Verde", "Cambodia", "Cameroon", "Canada",
                "Central African Republic", "Chad", "Chile", "China", "Colombia", "Comoros", "Congo (Congo-Brazzaville)",
                "Congo (Democratic Republic)", "Costa Rica", "Croatia", "Cuba", "Cyprus", "Czech Republic (Czechia)",
                "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea",
                "Eritrea", "Estonia", "Eswatini (fmr. \"Swaziland\")", "Ethiopia", "Fiji", "Finland", "France", "Gabon",
                "Gambia", "Georgia", "Germany", "Ghana", "Greece", "Grenada", "Guatemala", "Guinea", "Guinea-Bissau", "Guyana",
                "Haiti", "Holy See", "Honduras", "Hungary", "Iceland", "India", "Indonesia", "Iran", "Iraq", "Ireland",
                "Israel", "Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea (North)",
                "Korea (South)", "Kosovo", "Kuwait", "Kyrgyzstan", "Laos", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libya",
                "Liechtenstein", "Lithuania", "Luxembourg", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
                "Marshall Islands", "Mauritania", "Mauritius", "Mexico", "Micronesia", "Moldova", "Monaco", "Mongolia",
                "Montenegro", "Morocco", "Mozambique", "Myanmar (formerly Burma)", "Namibia", "Nauru", "Nepal", "Netherlands",
                "New Zealand", "Nicaragua", "Niger", "Nigeria", "North Macedonia (formerly Macedonia)", "Norway", "Oman",
                "Pakistan", "Palau", "Palestine State", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines",
                "Poland", "Portugal", "Qatar", "Romania", "Russia", "Rwanda", "Saint Kitts and Nevis", "Saint Lucia",
                "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal",
                "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia",
                "South Africa", "South Sudan", "Spain", "Sri Lanka", "Sudan", "Suriname", "Sweden", "Switzerland", "Syria",
                "Tajikistan", "Tanzania", "Thailand", "Timor-Leste", "Togo", "Tonga", "Trinidad and Tobago", "Tunisia",
                "Turkey", "Turkmenistan", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom",
                "United States of America", "Uruguay", "Uzbekistan", "Vanuatu", "Vatican City", "Venezuela", "Vietnam",
                "Yemen", "Zambia", "Zimbabwe"
        };
         */


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
                    Intent intent = new Intent(FirstPageActivity.this,MyRoutesActivity.class);
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
                Intent intent = new Intent(FirstPageActivity.this,CityExploringActivity.class);
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


        imageButtonSearch.setOnClickListener(new View.OnClickListener() {
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