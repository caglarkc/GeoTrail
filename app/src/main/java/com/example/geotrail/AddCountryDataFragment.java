package com.example.geotrail;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddCountryDataFragment extends Fragment {
    public AddCountryDataFragment(){}
    /*
    DATA EKLEMELERİ KONTROL ET VE SIFIRDAN EKLEME YAP, BAZILARI FAKRLI KAYDEDİLİYOR, DUZENSIZ ŞEKİLDE KAYDEDİLİYOR. DUZENLE
     */
    DatabaseReference mReferenceData;
    StorageReference mReferenceStorage;

    ActivityResultLauncher<Intent> resultLauncher;
    Uri imageUri;

    Spinner spinnerCountry;
    TextView textViewInfo;
    ImageButton imageButtonCountryImage;
    EditText editTextCityName;
    Button buttonAdd, buttonSetImage;

    int counter = 1;
    List<String> cityNames = new ArrayList<>();
    boolean isImageUploaded = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_country_data,container,false);

        mReferenceData = FirebaseDatabase.getInstance().getReference("Data");
        mReferenceStorage = FirebaseStorage.getInstance().getReference("Data");

        spinnerCountry = view.findViewById(R.id.spinnerCountry);
        imageButtonCountryImage = view.findViewById(R.id.imageButtonCountryImage);
        editTextCityName = view.findViewById(R.id.editTextCityName);
        buttonAdd = view.findViewById(R.id.buttonAdd);
        textViewInfo = view.findViewById(R.id.textViewInfo);
        buttonSetImage = view.findViewById(R.id.buttonSetImage);

        List<String> countryList = new ArrayList<>();
        countryList.add("Select Country");

        countryList.addAll(MainMethods.getCountryList());

        CustomAdapter adapter = new CustomAdapter(getContext(), countryList);
        spinnerCountry.setAdapter(adapter);

        registerResult();


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCity();

            }
        });

        imageButtonCountryImage.setOnClickListener(new View.OnClickListener() {
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
                    if (!selectedCountry.equals("Select Country")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want add this city to " + selectedCountry + " ?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                uploadPhotoToFirebase(selectedCountry);
                            }
                        });
                        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        builder.show();
                    }else {
                        Toast.makeText(getContext(), "First, select a country...", Toast.LENGTH_SHORT).show();
                    }

                }else {
                    Toast.makeText(getContext(), "First, choose image...", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
    }


    private void addCity() {
        counter = 1;
        cityNames = new ArrayList<>();
        String cityName = editTextCityName.getText().toString();
        if (!TextUtils.isEmpty(cityName)) {
            String selectedCountry = spinnerCountry.getSelectedItem().toString();
            if (!TextUtils.isEmpty(selectedCountry) && !selectedCountry.equals("Select Country")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Confirm");
                builder.setMessage("Do you want add this city to " + selectedCountry + " ?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mReferenceData.child("Countries").child(selectedCountry).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot detailSnapshot : snapshot.getChildren()) {
                                        String detail = detailSnapshot.getKey();
                                        if (detail != null) {
                                            if (detail.equals("Cities")) {
                                                for (DataSnapshot citySnapshot : detailSnapshot.getChildren()) {
                                                    String val = citySnapshot.getKey();
                                                    val = val.toLowerCase();
                                                    cityNames.add(val);
                                                    counter += 1;
                                                }
                                            }
                                        }
                                    }
                                    if (!cityNames.contains(cityName.toLowerCase())) {
                                        Task<Void> task1 = mReferenceData.child("Countries").child(selectedCountry).child("Cities").child(cityName).setValue(cityName);
                                        Task<Void> task2 = mReferenceData.child("Cities").child(cityName).child("country").setValue(selectedCountry);
                                        Tasks.whenAllComplete(task1,task2).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                                            @Override
                                            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getContext(),"City added with successfully...",Toast.LENGTH_SHORT).show();
                                                    editTextCityName.setText("");
                                                }else {
                                                    Toast.makeText(getContext(),"HATA",Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                    }else {
                                        Toast.makeText(getContext(),"Bu şehir zaten var...",Toast.LENGTH_SHORT).show();
                                    }
                                }else {
                                    Task<Void> task1 = mReferenceData.child("Countries").child(selectedCountry).child("Cities").child(cityName).setValue(cityName);
                                    Task<Void> task2 = mReferenceData.child("Cities").child(cityName).child("country").setValue(selectedCountry);
                                    Tasks.whenAllComplete(task1,task2).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                                        @Override
                                        public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext(),"City added with successfully...",Toast.LENGTH_SHORT).show();
                                                editTextCityName.setText("");
                                            }else {
                                                Toast.makeText(getContext(),"HATA",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.show();

            }else {
                Toast.makeText(getContext(),"Please select a country...",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getContext(),"Please enter a city name...",Toast.LENGTH_SHORT).show();
        }
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
                            imageButtonCountryImage.setImageURI(imageUri);
                            imageButtonCountryImage.setBackgroundResource(R.color.black);
                            Toast.makeText(getContext(), "Image chosen...", Toast.LENGTH_SHORT).show();
                            textViewInfo.setText("Change Country Image");
                            isImageUploaded = true;
                        } else {
                            Toast.makeText(getContext(), "Image choosing is canceled...", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void uploadPhotoToFirebase(String selectedCountry) {
        mReferenceStorage.child("Countries")
                .child(selectedCountry)
                .child("CountryImage")
                .child("Image")
                .putFile(imageUri)
                .continueWithTask(task -> {
                    // Yükleme sırasında bir hata oluşursa
                    if (!task.isSuccessful()) {
                        Log.e("UPLOAD_ERROR", "Error uploading file: " + task.getException());
                        throw task.getException();
                    }
                    // İndirme URL'sini al
                    return mReferenceStorage.child("Countries")
                            .child(selectedCountry)
                            .child("CountryImage")
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
                    return mReferenceData.child("Countries")
                            .child(selectedCountry)
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

    private void addCities() {
        //Cityleri toplu eklemek için kullandıgım method...
        Map<String, List<String>> countryCityMap = new HashMap<>();

        countryCityMap.put("United States of America", Arrays.asList(
                "New York", "Los Angeles", "Chicago", "Houston", "Phoenix",
                "Philadelphia", "San Antonio", "San Diego", "Dallas", "San Jose"));

        countryCityMap.put("Italy", Arrays.asList(
                "Rome", "Milan", "Naples", "Turin", "Palermo",
                "Genoa", "Bologna", "Florence", "Venice", "Verona"));

        countryCityMap.put("Japan", Arrays.asList(
                "Tokyo", "Osaka", "Yokohama", "Nagoya", "Sapporo",
                "Fukuoka", "Kobe", "Kyoto", "Sendai", "Hiroshima"));

        countryCityMap.put("Turkey", Arrays.asList(
                "Istanbul", "Ankara", "Izmir", "Bursa", "Antalya",
                "Gaziantep", "Konya", "Adana", "Mersin", "Diyarbakır"));

        countryCityMap.put("Spain", Arrays.asList(
                "Madrid", "Barcelona", "Valencia", "Seville", "Zaragoza",
                "Malaga", "Murcia", "Palma", "Las Palmas", "Bilbao"));

        countryCityMap.put("Germany", Arrays.asList(
                "Berlin", "Munich", "Frankfurt", "Hamburg", "Cologne",
                "Stuttgart", "Düsseldorf", "Dortmund", "Essen", "Leipzig"));

        countryCityMap.put("France", Arrays.asList(
                "Paris", "Marseille", "Lyon", "Toulouse", "Nice",
                "Nantes", "Strasbourg", "Montpellier", "Bordeaux", "Lille"));

        countryCityMap.put("Belgium", Arrays.asList(
                "Brussels", "Antwerp", "Ghent", "Charleroi", "Liège",
                "Bruges", "Namur", "Leuven", "Mons", "Mechelen"));

        countryCityMap.put("Bosnia and Herzegovina", Arrays.asList(
                "Sarajevo", "Banja Luka", "Mostar", "Tuzla", "Zenica",
                "Bijeljina", "Prijedor", "Trebinje", "Doboj", "Brčko"));

        countryCityMap.put("Serbia", Arrays.asList(
                "Belgrade", "Novi Sad", "Niš", "Kragujevac", "Subotica",
                "Zrenjanin", "Pančevo", "Čačak", "Kruševac", "Kraljevo"));



        for (Map.Entry<String, List<String>> entry : countryCityMap.entrySet()) {
            String country = entry.getKey();
            List<String> cities = entry.getValue();
            int counter = 0;
            for (String cityName : cities) {
                counter +=1;
                mReferenceData.child("Countries").child(country).child("Cities").child(cityName).setValue(counter + "");
            }

        }
    }

}
