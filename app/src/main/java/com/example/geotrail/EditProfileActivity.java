package com.example.geotrail;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {
    DatabaseReference mReferenceUser;

    SharedPreferences sharedUser;

    EditText editTextUsername, editTextRegion, editTextPassword;
    TextView textViewEmail;
    Button buttonSave;
    ImageButton imageButtonShowPassword;

    String stringEmail, stringRegion, stringUsername, stringHashedPassword, sharedUserUid;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextRegion = findViewById(R.id.editTextRegion);
        editTextPassword = findViewById(R.id.editTextPassword);
        textViewEmail = findViewById(R.id.textViewEmail);
        buttonSave = findViewById(R.id.buttonSave);
        imageButtonShowPassword = findViewById(R.id.imageButtonShowPassword);

        sharedUser = getSharedPreferences("user_data",MODE_PRIVATE);
        sharedUserUid = sharedUser.getString("user_uid","");

        mReferenceUser = FirebaseDatabase.getInstance().getReference("Users").child(sharedUserUid);

        // Show progress indicator
        ProgressDialog progressDialog = new ProgressDialog(EditProfileActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        mReferenceUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                    String detail = detailSnapshot.getKey();
                    if (detail != null) {
                        if (detail.equals("user_email")) {
                            stringEmail = detailSnapshot.getValue(String.class);
                            textViewEmail.setHint(stringEmail);
                        }else if (detail.equals("user_username")) {
                            stringUsername = detailSnapshot.getValue(String.class);
                            editTextUsername.setHint(stringUsername);
                        }else if (detail.equals("user_region")) {
                            stringRegion = detailSnapshot.getValue(String.class);
                            editTextRegion.setHint(stringRegion);
                        }else if (detail.equals("user_hashedPassword")) {
                            stringHashedPassword = detailSnapshot.getValue(String.class);
                        }
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                    }
                },100);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
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

        textViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Email can't change...", Toast.LENGTH_SHORT).show();
            }
        });


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputUsername = editTextUsername.getText().toString();
                String inputRegion = editTextRegion.getText().toString();
                String inputPassword = MainMethods.hashPassword(editTextPassword.getText().toString());

                if (TextUtils.isEmpty(inputUsername) && TextUtils.isEmpty(inputRegion) && TextUtils.isEmpty(inputPassword)) {
                    Toast.makeText(EditProfileActivity.this,"All infos empty...",Toast.LENGTH_SHORT).show();
                }else {
                    if (stringUsername.equals(inputUsername) && stringRegion.equals(inputRegion) && stringHashedPassword.equals(inputPassword)) {
                        Toast.makeText(EditProfileActivity.this,"Infos are same...",Toast.LENGTH_SHORT).show();
                    }else {
                        HashMap<String, String> mData = new HashMap<>();
                        mData.put("user_hashedPassword",inputPassword);
                        mData.put("user_username",inputUsername);
                        mData.put("user_region",inputRegion);
                        progressDialog.show();
                        mReferenceUser.setValue(mData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()) {

                                    Toast.makeText(EditProfileActivity.this,"Infos are updated with successfully...",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }else {
                                    Log.d("ERROR",task.getException().toString());
                                    Toast.makeText(EditProfileActivity.this,"Progress failed...",Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    }
                }

            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(EditProfileActivity.this, FirstPageActivity.class);
        startActivity(intent);
        finish();
    }
}