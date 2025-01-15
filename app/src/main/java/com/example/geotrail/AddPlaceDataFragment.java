package com.example.geotrail;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class AddPlaceDataFragment extends Fragment {

    public AddPlaceDataFragment() {}

    DatabaseReference mReferenceData;
    StorageReference mReferenceStorage;

    ActivityResultLauncher<Intent> resultLauncher;
    Uri imageUri;

    Spinner spinnerCountry, spinnerCity;
    ProgressBar progressBar;
    LinearLayout containerLayout, addDetailsContainer;
    ImageButton imageButtonPlaceImage;
    Button buttonSaveDetails;
    EditText editTextMain, editTextOpeningHours, editTextEntranceFee, editTextPlaceName;
    TextView textViewInfo;

    boolean isImageUploaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_place_data,container,false);

        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");
        mReferenceStorage = FirebaseStorage.getInstance().getReference("Data");

        imageButtonPlaceImage = view.findViewById(R.id.imageButtonPlaceImage);
        buttonSaveDetails = view.findViewById(R.id.buttonSaveDetails);
        editTextMain = view.findViewById(R.id.editTextMain);
        editTextOpeningHours = view.findViewById(R.id.editTextOpeningHours);
        editTextEntranceFee = view.findViewById(R.id.editTextEntranceFee);
        editTextPlaceName = view.findViewById(R.id.editTextPlaceName);
        textViewInfo = view.findViewById(R.id.textViewInfo);
        containerLayout = view.findViewById(R.id.containerLayout);
        progressBar = view.findViewById(R.id.progressBar);
        spinnerCountry = view.findViewById(R.id.spinnerCountry);
        spinnerCity = view.findViewById(R.id.spinnerCity);
        addDetailsContainer = view.findViewById(R.id.addDetailsContainer);

        List<String> countryList = new ArrayList<>();
        countryList.add("Select Country");

        countryList.addAll(MainMethods.getCountryList());

        CustomAdapter countryAdapter = new CustomAdapter(getContext(), countryList);
        spinnerCountry.setAdapter(countryAdapter);


        spinnerCity.setVisibility(View.GONE);
        containerLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        addDetailsContainer.setVisibility(View.GONE);

        registerResult();

        spinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Seçilen item
                String selectedCountry = parent.getItemAtPosition(position).toString();

                // "Select Country" hariç bir şey seçildiğinde işlem yap
                if (!selectedCountry.equals("Select Country")) {
                    progressBar.setVisibility(View.VISIBLE);
                    containerLayout.setVisibility(View.GONE);

                    List<String> cityList = new ArrayList<>();
                    cityList.add("Select City");
                    mReferenceData.child("Countries").child(selectedCountry).child("Cities").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot citySnapshot : snapshot.getChildren()) {
                                    String cityName = citySnapshot.getKey();
                                    if (cityName != null) {
                                        cityList.add(cityName);
                                    }
                                }
                            }
                            new Handler().postDelayed(() -> {
                                progressBar.setVisibility(View.GONE);
                                containerLayout.setVisibility(View.VISIBLE);

                                // Eğer sadece "Select City" varsa uyarı göster ve spinnerı gizle
                                if (cityList.size() == 1) {
                                    Toast.makeText(getContext(), "No cities found for the selected country.", Toast.LENGTH_SHORT).show();
                                    addDetailsContainer.setVisibility(View.GONE);
                                    imageButtonPlaceImage.setImageDrawable(null);
                                    imageButtonPlaceImage.setBackgroundResource(R.drawable.empty_image_icon);
                                    textViewInfo.setText("Set Place Image");
                                    spinnerCity.setVisibility(View.GONE);

                                } else {
                                    // City listesiyle Spinner'ı güncelle
                                    CustomAdapter cityAdapter = new CustomAdapter(getContext(), cityList);
                                    addDetailsContainer.setVisibility(View.VISIBLE);
                                    spinnerCity.setAdapter(cityAdapter);
                                    spinnerCity.setVisibility(View.VISIBLE);

                                }
                            }, 30);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Hata durumunda gecikmeli olarak ProgressBar'ı gizle
                            new Handler().postDelayed(() -> {
                                progressBar.setVisibility(View.GONE);
                                containerLayout.setVisibility(View.VISIBLE);

                                Log.e("FirebaseError", "Error while fetching cities: " + error.getMessage());
                            }, 30);
                        }
                    });


                    spinnerCity.setVisibility(View.VISIBLE);
                }else {
                    containerLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    spinnerCity.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Hiçbir şey seçilmezse
                spinnerCity.setVisibility(View.GONE);
            }
        });

        imageButtonPlaceImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });


        buttonSaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedCountry = spinnerCountry.getSelectedItem().toString();
                String selectedCity = spinnerCity.getSelectedItem().toString();
                if (!selectedCountry.equals("Select Country") && !selectedCity.equals("Select City")) {
                    String placeName = editTextPlaceName.getText().toString();
                    String entranceFee = editTextEntranceFee.getText().toString();
                    String openingHours = editTextOpeningHours.getText().toString();
                    String mainDetail = editTextMain.getText().toString();
                    if (!TextUtils.isEmpty(placeName) && !TextUtils.isEmpty(entranceFee)
                            && !TextUtils.isEmpty(openingHours) && !TextUtils.isEmpty(mainDetail)) {
                        if (isImageUploaded) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                            builder.setTitle("Confirm");
                            builder.setMessage("Do you want add this details to " + selectedCity + " ?");
                            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    uploadDetailsToFirebase(selectedCountry, selectedCity, placeName, entranceFee, openingHours, mainDetail);
                                }
                            });
                            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            });

                            builder.show();
                        }else {
                            Toast.makeText(getContext(),"Please choose a place photo...",Toast.LENGTH_SHORT).show();
                        }

                    }else {
                        Toast.makeText(getContext(),"Please enter all details...",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "First, select a country and city...", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); // Çoklu seçim izni
        resultLauncher.launch(intent);
    }

    private void registerResult() {
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            // Seçilen resimleri alma
                            imageUri = result.getData().getData();
                            imageButtonPlaceImage.setImageURI(imageUri);
                            imageButtonPlaceImage.setBackgroundResource(R.color.black);
                            Toast.makeText(getContext(), "Image chosen...", Toast.LENGTH_SHORT).show();
                            textViewInfo.setText("Change Place Image");
                            isImageUploaded = true;
                        } else {
                            Toast.makeText(getContext(), "Image choosing is canceled...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void uploadDetailsToFirebase(String country, String city, String placeName, String entranceFee, String openingHours, String mainDetail) {
        mReferenceStorage.child("Places")
                .child(placeName)
                .child("Image")
                .putFile(imageUri)
                .continueWithTask(task -> {
                    // Yükleme sırasında bir hata oluşursa
                    if (!task.isSuccessful()) {
                        Log.e("UPLOAD_ERROR", "Error uploading file: " + task.getException());
                        throw task.getException();
                    }
                    // İndirme URL'sini al
                    return mReferenceStorage.child("Places")
                            .child(placeName)
                            .child("Image")
                            .getDownloadUrl();
                })
                .continueWithTask(downloadUrlTask -> {
                    // İndirme URL'sini alma sırasında bir hata oluşursa
                    if (!downloadUrlTask.isSuccessful()) {
                        Log.e("DOWNLOAD_URL_ERROR", "Error getting download URL: " + downloadUrlTask.getException());
                        throw downloadUrlTask.getException();
                    }

                    // İndirme URL'sini String formatına çevir
                    String downloadUrl = downloadUrlTask.getResult().toString();
                    Log.d("DOWNLOAD_URL", "Download URL: " + downloadUrl);
                    HashMap<String, String> mData = new HashMap<>();
                    mData.put("image_url",downloadUrl);
                    mData.put("country",country);
                    mData.put("city",city);
                    mData.put("entrance_fee",entranceFee);
                    mData.put("opening_hours",openingHours);
                    mData.put("main_detail",mainDetail);

                    // İndirme URL'sini Firebase Realtime Database'e kaydet
                    return mReferenceData.child("Places")
                            .child(placeName)
                            .setValue(mData)
                            .addOnFailureListener(e -> {
                                Log.e("DATABASE_ERROR", "Error saving URL to database: " + e.getMessage());
                            });
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mReferenceData.child("Cities").child(city).child("Places").child(placeName).setValue(placeName).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // Tüm işlemler başarıyla tamamlandı
                                    Toast.makeText(getContext(), "Places is added with successfully...", Toast.LENGTH_SHORT).show();
                                    editTextPlaceName.setText("");
                                    editTextEntranceFee.setText("");
                                    editTextOpeningHours.setText("");
                                    editTextMain.setText("");
                                    imageButtonPlaceImage.setBackground(null);
                                    imageButtonPlaceImage.setImageResource(R.drawable.empty_image_icon);
                                    textViewInfo.setText("Set Place Image");
                                }else {
                                    Log.e("UPLOAD_COMPLETE_ERROR", "Error during upload: " + task.getException().toString());
                                }
                            }
                        });

                    } else {
                        // İşlemler sırasında bir hata oluştu
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error occurred";
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                        Log.e("UPLOAD_COMPLETE_ERROR", "Error during upload: " + error);
                    }
                })
                .addOnFailureListener(e -> {
                    // Genel hata yönetimi
                    Log.e("UPLOAD_FAILURE", "Error during upload process: " + e.getMessage());
                    Toast.makeText(getContext(), "Failed to upload image. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }
}