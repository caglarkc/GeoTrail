package com.example.geotrail;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * UsernameFragment: This fragment allows the user to enter a username during the registration process.
 * - Contains an EditText for username input and a Continue button.
 * - The Continue button is dynamically styled based on the presence of text in the EditText.
 * - When a valid username is entered, the Continue button initiates a method in the parent RegisterActivity to proceed with the registration process.
 */

public class UsernameFragment extends Fragment {
    public UsernameFragment() {}

    DatabaseReference mReferenceUsers;

    LinearLayout containerLayout;
    ProgressBar progressBar;
    Button buttonContinue;
    EditText editTextUsername, editTextRegion;

    String stringUsername, stringRegion;
    boolean isExist = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_username, container, false);


        buttonContinue = view.findViewById(R.id.buttonContinue);
        editTextUsername = view.findViewById(R.id.editTextUsername);
        editTextRegion = view.findViewById(R.id.editTextRegion);
        containerLayout = view.findViewById(R.id.containerLayout);
        progressBar = view.findViewById(R.id.progressBar);


        containerLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        mReferenceUsers = FirebaseDatabase.getInstance().getReference("Users");

        editTextUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stringUsername = editTextUsername.getText().toString();
                stringRegion = editTextRegion.getText().toString();

                if (charSequence.length()>0 && !TextUtils.isEmpty(stringUsername) && !TextUtils.isEmpty(stringRegion)){
                    buttonContinue.setBackgroundResource(R.drawable.full_button_border);
                    buttonContinue.setTextColor(Color.WHITE);
                }else {
                    buttonContinue.setBackgroundResource(R.drawable.empty_button_border);
                    buttonContinue.setTextColor(getResources().getColor(R.color.half_light_text_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editTextRegion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                stringUsername = editTextUsername.getText().toString();
                stringRegion = editTextRegion.getText().toString();

                if (charSequence.length()>0 && !TextUtils.isEmpty(stringUsername) && !TextUtils.isEmpty(stringRegion)){
                    buttonContinue.setBackgroundResource(R.drawable.full_button_border);
                    buttonContinue.setTextColor(Color.WHITE);
                }else {
                    buttonContinue.setBackgroundResource(R.drawable.empty_button_border);
                    buttonContinue.setTextColor(getResources().getColor(R.color.half_light_text_color));
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                stringUsername = editTextUsername.getText().toString();
                stringRegion = editTextRegion.getText().toString();
                if (!TextUtils.isEmpty(stringRegion) && !TextUtils.isEmpty(stringUsername)){
                    containerLayout.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    // Kullanıcı adını kontrol et
                    isUsernameExist(stringUsername, new UsernameExistCallback() {
                        @Override
                        public void onCheckComplete(boolean isExist) {
                            if (isExist) {
                                // Kullanıcı adı zaten mevcut
                                progressBar.setVisibility(View.GONE);
                                containerLayout.setVisibility(View.VISIBLE);
                                Toast.makeText(getActivity(), "This username already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Kullanıcı adı mevcut değil, devam et
                                ((RegisterActivity) requireActivity()).getUsername(stringUsername, stringRegion);
                                progressBar.setVisibility(View.GONE); // İşlem tamamlandı
                            }
                        }
                    });

                }else {
                    Toast.makeText(getActivity(),"Enter a value...",Toast.LENGTH_SHORT).show();
                }
            }
        });



        return view;
    }

    public void isUsernameExist(String username, UsernameExistCallback callback) {
        mReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExist = false;
                for (DataSnapshot userUidSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot detailSnapshot : userUidSnapshot.getChildren()) {
                        String detail = detailSnapshot.getKey();
                        if (detail != null && detail.equals("user_username")) {
                            String val = detailSnapshot.getValue(String.class);
                            String tempUsername = username;
                            tempUsername = tempUsername.toLowerCase();
                            if (val != null) {
                                val = val.toLowerCase();
                                if (val.equals(tempUsername)) {
                                    isExist = true;
                                    break;
                                }
                            }

                        }
                    }
                    if (isExist) break;
                }
                // Callback ile sonucu bildir
                callback.onCheckComplete(isExist);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Hata durumunda false döndür
                callback.onCheckComplete(false);
            }
        });
    }

    // Callback arayüzü
    public interface UsernameExistCallback {
        void onCheckComplete(boolean isExist);
    }
}
