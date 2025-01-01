package com.example.geotrail;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser mUser;

    SharedPreferences sharedUser;
    SharedPreferences.Editor editor;

    Button buttonSignUp, buttonLogin;
    EditText editTextEmail, editTextPassword;
    ImageButton imageButtonShowPassword;

    String stringEmail, stringPassword;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        imageButtonShowPassword = findViewById(R.id.imageButtonShowPassword);


        mAuth = FirebaseAuth.getInstance();

        sharedUser = getSharedPreferences("user_data",MODE_PRIVATE);
        editor = sharedUser.edit();


        editTextEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stringPassword = editTextPassword.getText().toString();
                if (charSequence.length() > 0 && !TextUtils.isEmpty(stringPassword)) {
                    buttonLogin.setBackgroundResource(R.drawable.full_button_border);
                    buttonLogin.setTextColor(Color.WHITE);
                }else {
                    buttonLogin.setBackgroundResource(R.drawable.empty_button_border);
                    buttonLogin.setTextColor(getResources().getColor(R.color.half_light_text_color));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        editTextPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stringEmail = editTextEmail.getText().toString();
                if (charSequence.length() > 0 && !TextUtils.isEmpty(stringEmail)){
                    buttonLogin.setBackgroundResource(R.drawable.full_button_border);
                    buttonLogin.setTextColor(Color.WHITE);
                }else {
                    buttonLogin.setBackgroundResource(R.drawable.empty_button_border);
                    buttonLogin.setTextColor(getResources().getColor(R.color.half_light_text_color));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        imageButtonShowPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        imageButtonShowPassword.setBackgroundResource(R.drawable.eye_icon);
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        return true;
                    case MotionEvent.ACTION_UP:
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        imageButtonShowPassword.setBackgroundResource(R.drawable.eye_visible_icon);
                        return true;
                }
                return false;
            }
        });

        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stringEmail = editTextEmail.getText().toString();
                stringPassword = editTextPassword.getText().toString();
                if (!TextUtils.isEmpty(stringEmail) && !TextUtils.isEmpty(stringPassword)) {

                    if (!Patterns.EMAIL_ADDRESS.matcher(stringEmail).matches()) {
                        Toast.makeText(LoginActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Show progress indicator
                    ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                    progressDialog.setMessage("Logging in...");
                    progressDialog.show();

                    mAuth.signInWithEmailAndPassword(stringEmail,stringPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                mUser = mAuth.getCurrentUser();
                                Toast.makeText(LoginActivity.this,"You login with successfully...",Toast.LENGTH_SHORT).show();
                                editor.putString("user_uid", mUser.getUid());
                                editor.apply();
                                Intent intent = new Intent(LoginActivity.this,FirstPageActivity.class);
                                startActivity(intent);
                                finish();
                                progressDialog.dismiss();
                            }else {
                                Toast.makeText(LoginActivity.this,"Yanlış email veya şifre...",Toast.LENGTH_SHORT).show();
                                System.out.println(task.getException().toString());
                            }
                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this,"Please enter details...",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(LoginActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }
}