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
import com.google.android.gms.tasks.Tasks;
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

public class AddCityDataFragment extends Fragment {
    /**
     * Detail ekleme de tamam, bu sayfa tamamen tamamlandı. Sadece edittextler klavye açıldıgında gozukmuyor, scrollview ile çözülüyordu sanırım
     * Bu sorunu çöz ve diğer fragmentler ile duzenlemeye geç
     */

    public AddCityDataFragment() {}

    DatabaseReference mReferenceData;
    StorageReference mReferenceStorage;

    ActivityResultLauncher<Intent> resultLauncher;
    Uri imageUri;

    LinearLayout containerLayout, addDetailsContainer;
    Spinner spinnerCountry, spinnerCity;
    ProgressBar progressBar;
    EditText editTextMain, editTextTiny;
    TextView textViewInfo;
    ImageButton imageButtonCityImage;
    Button buttonSetImage, buttonSaveDetails;

    boolean isImageUploaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_city_data,container,false);

        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");
        mReferenceStorage = FirebaseStorage.getInstance().getReference("Data");

        spinnerCountry = view.findViewById(R.id.spinnerCountry);
        spinnerCity = view.findViewById(R.id.spinnerCity);
        progressBar = view.findViewById(R.id.progressBar);
        containerLayout = view.findViewById(R.id.containerLayout);
        addDetailsContainer = view.findViewById(R.id.addDetailsContainer);
        textViewInfo = view.findViewById(R.id.textViewInfo);
        imageButtonCityImage = view.findViewById(R.id.imageButtonCityImage);
        buttonSetImage = view.findViewById(R.id.buttonSetImage);
        editTextMain = view.findViewById(R.id.editTextMain);
        editTextTiny = view.findViewById(R.id.editTextTiny);
        buttonSaveDetails = view.findViewById(R.id.buttonSaveDetails);


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

        imageButtonCityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        buttonSetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isImageUploaded) {
                    String selectedCountry = spinnerCountry.getSelectedItem().toString();
                    String selectedCity = spinnerCity.getSelectedItem().toString();
                    if (!selectedCountry.equals("Select Country") && !selectedCity.equals("Select City")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want add this image?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uploadPhotoToFirebase(selectedCity);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        builder.show();
                    }else {
                        Toast.makeText(getContext(), "First, select a country and city...", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(getContext(), "First, choose image...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonSaveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String selectedCountry = spinnerCountry.getSelectedItem().toString();
                String selectedCity = spinnerCity.getSelectedItem().toString();
                if (!selectedCountry.equals("Select Country") && !selectedCity.equals("Select City")) {
                    String tinyDetail = editTextTiny.getText().toString();
                    String mainDetail = editTextMain.getText().toString();
                    if (!TextUtils.isEmpty(tinyDetail) && !TextUtils.isEmpty(mainDetail)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want add this details to " + selectedCity + " ?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uploadDetailsToFirebase(selectedCity, tinyDetail, mainDetail);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        builder.show();
                    }else {
                        Toast.makeText(getContext(),"Please enter all details...",Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(getContext(), "First, select a country and city...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        // Spinner item seçildiğinde yapılacak işlem
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
                                    imageButtonCityImage.setImageDrawable(null);
                                    imageButtonCityImage.setBackgroundResource(R.drawable.empty_image_icon);
                                    textViewInfo.setText("Set City Image");
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
                            imageButtonCityImage.setImageURI(imageUri);
                            imageButtonCityImage.setBackgroundResource(R.color.black);
                            Toast.makeText(getContext(), "Image chosen...", Toast.LENGTH_SHORT).show();
                            textViewInfo.setText("Change City Image");
                            isImageUploaded = true;
                        } else {
                            Toast.makeText(getContext(), "Image choosing is canceled...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void uploadPhotoToFirebase(String selectedCity) {
        mReferenceStorage.child("Cities")
                .child(selectedCity)
                .child("CityImage")
                .child("Image")
                .putFile(imageUri)
                .continueWithTask(task -> {
                    // Yükleme sırasında bir hata oluşursa
                    if (!task.isSuccessful()) {
                        Log.e("UPLOAD_ERROR", "Error uploading file: " + task.getException());
                        throw task.getException();
                    }
                    // İndirme URL'sini al
                    return mReferenceStorage.child("Cities")
                            .child(selectedCity)
                            .child("CityImage")
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

                    // İndirme URL'sini Firebase Realtime Database'e kaydet
                    return mReferenceData.child("Cities")
                            .child(selectedCity)
                            .child("image_url")
                            .setValue(downloadUrl)
                            .addOnFailureListener(e -> {
                                Log.e("DATABASE_ERROR", "Error saving URL to database: " + e.getMessage());
                            });
                })
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Tüm işlemler başarıyla tamamlandı
                        Toast.makeText(getContext(), "Image uploaded and URL saved successfully.", Toast.LENGTH_SHORT).show();
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

    private void uploadDetailsToFirebase(String selectedCity, String tinyDetail, String mainDetail) {
        Task<Void> tinyDetailTask = mReferenceData.child("Cities").child(selectedCity).child("tiny_detail").setValue(tinyDetail);
        Task<Void> mainDetailTask = mReferenceData.child("Cities").child(selectedCity).child("main_detail").setValue(mainDetail);

        // Tüm görevleri birleştir ve takip et
        Tasks.whenAll(tinyDetailTask, mainDetailTask)
                .addOnSuccessListener(unused -> {
                    // Bütün görevler başarıyla tamamlandı
                    editTextMain.setText("");
                    editTextTiny.setText("");
                    Toast.makeText(getContext(),"Details are uploaded with successfully...",Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Herhangi bir görev başarısız oldu
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                });


    }
}
