package com.example.recipebook;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    ActivityRegisterBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    private Uri selectedImageUri = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        String[] countries = {"Select Country", "Palestine", "Jordan", "Egypt", "Saudi Arabia", "Türkiye"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.countrySpn.setAdapter(adapter);


        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.userProfileImg.setImageURI(uri);
                    }
                }
        );

        binding.userProfileImg.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = binding.registerNameEt.getText().toString().trim();
                String email = binding.registerEmailEt.getText().toString().trim();
                String password = binding.registerPasswordEt.getText().toString().trim();
                String selectedCountry = binding.countrySpn.getSelectedItem().toString();

                if (name.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter your full name", Toast.LENGTH_SHORT).show();
                } else if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter your email address", Toast.LENGTH_SHORT).show();
                } else if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please enter your password", Toast.LENGTH_SHORT).show();
                } else if (selectedCountry.equals("Select Country")) {
                    Toast.makeText(RegisterActivity.this, "Please select a country", Toast.LENGTH_SHORT).show();
                } else if (selectedImageUri == null) {
                    Toast.makeText(RegisterActivity.this, "Please click on the profile icon to choose an image!", Toast.LENGTH_LONG).show();
                }else {
                    binding.registerBtn.setEnabled(false);
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String userID = auth.getCurrentUser().getUid();

                                uploadProfileImageAndSave(userID, name, email, selectedCountry);
//                                if (selectedImageUri != null) {
//                                    uploadProfileImageAndSave(userID, name, email, selectedCountry);
//                                } else {
//                                    saveUserDataToFirestore(userID, name, email, selectedCountry, "");
//                                }
                            } else {
                                binding.registerBtn.setEnabled(true);
                                Toast.makeText(RegisterActivity.this, "Registration Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        binding.loginTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void uploadProfileImageAndSave(String userID, String name, String email, String country) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(selectedImageUri);
            if (inputStream == null) {
                binding.registerBtn.setEnabled(true);
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show();
                return;
            }
            byte[] bytes = getBytes(inputStream);


            String mimeType = resolver.getType(selectedImageUri);
            if (mimeType == null) {
                mimeType = "image/png";
            }

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            String folderName = "profiles";
            String fileName = "profile_" + userID + "_" + System.currentTimeMillis() + ".png";

            SupabaseService service = RetrofitClient.getService();
            Call<ResponseBody> call = service.uploadImage(
                    "Bearer " + RetrofitClient.SUPABASE_KEY,
                    RetrofitClient.SUPABASE_KEY,
                    mimeType,
                    folderName,
                    fileName,
                    requestFile
            );

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        String publicImageUrl = "https://bilzduvkqyssofbzugve.supabase.co/storage/v1/object/public/recipe-images/" + folderName + "/" + fileName;

                        saveUserDataToFirestore(userID, name, email, country, publicImageUrl);
                    } else {
                        binding.registerBtn.setEnabled(true);
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                            android.util.Log.e("SupabaseRegisterError", "Code: " + response.code() + " | Body: " + errorBody);
                            Toast.makeText(RegisterActivity.this, "Upload Error (" + response.code() + "): " + errorBody, Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this, "Failed to upload image to Supabase", Toast.LENGTH_SHORT).show();
                        }                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.registerBtn.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            binding.registerBtn.setEnabled(true);
            e.printStackTrace();
        }
    }

    private void saveUserDataToFirestore(String userID, String name, String email, String country, String imageUrl) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("email", email);
        map.put("name", name);
        map.put("country", country);
        map.put("profileImageUrl", imageUrl);

        firestore.collection("users").document(userID).set(map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        binding.registerBtn.setEnabled(true);
                        if (task.isSuccessful()) {
                            auth.signOut();
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Failed to save data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}