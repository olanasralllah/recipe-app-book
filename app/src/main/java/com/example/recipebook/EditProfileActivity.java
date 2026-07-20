package com.example.recipebook;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    private Uri selectedImageUri = null;
    private String currentImageUrl = "";
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();


        binding.backBtn.setOnClickListener(v -> finish());

        String currentName = getIntent().getStringExtra("user_name");
        String currentEmail = getIntent().getStringExtra("user_email");
        currentImageUrl = getIntent().getStringExtra("profile_image_url");

        if (currentName != null) binding.editNameEt.setText(currentName);
        if (currentEmail != null) binding.editEmailEt.setText(currentEmail);

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Picasso.get().load(currentImageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_gallery)
                    .into(binding.editProfileImg);
        }

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        binding.editProfileImg.setImageURI(uri);
                    }
                }
        );

        binding.editProfileImg.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.updateProfileBtn.setOnClickListener(v -> {
            String newName = binding.editNameEt.getText().toString().trim();
            String newEmail = binding.editEmailEt.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(EditProfileActivity.this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                binding.updateProfileBtn.setEnabled(false);
                Toast.makeText(EditProfileActivity.this, "Updating profile...", Toast.LENGTH_SHORT).show();

                String userID = user.getUid();

                if (selectedImageUri != null) {
                    uploadImageToSupabaseAndSave(userID, newName, newEmail, user);
                } else {
                    handleEmailAndFirestoreUpdate(userID, newName, newEmail, currentImageUrl, user);
                }
            }
        });
    }

    private void uploadImageToSupabaseAndSave(String userID, String newName, String newEmail, FirebaseUser user) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream inputStream = resolver.openInputStream(selectedImageUri);
            if (inputStream == null) {
                binding.updateProfileBtn.setEnabled(true);
                return;
            }
            byte[] bytes = getBytes(inputStream);
            String mimeType = resolver.getType(selectedImageUri);
            if (mimeType == null) mimeType = "image/png";

            RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), bytes);
            String folderName = "profiles";
            String fileName = "profile_" + userID + ".png";

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
                        String newPublicImageUrl = "https://bilzduvkqyssofbzugve.supabase.co/storage/v1/object/public/recipe-images/" + folderName + "/" + fileName;
                        handleEmailAndFirestoreUpdate(userID, newName, newEmail, newPublicImageUrl, user);
                    } else {
                        binding.updateProfileBtn.setEnabled(true);
                        Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    binding.updateProfileBtn.setEnabled(true);
                    Toast.makeText(EditProfileActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (IOException e) {
            binding.updateProfileBtn.setEnabled(true);
            e.printStackTrace();
        }
    }

    private void handleEmailAndFirestoreUpdate(String userID, String newName, String newEmail, String imageUrl, FirebaseUser user) {
        String currentAuthEmail = user.getEmail();

        if (currentAuthEmail != null && !currentAuthEmail.equalsIgnoreCase(newEmail)) {
            user.updateEmail(newEmail).addOnCompleteListener(authTask -> {
                if (authTask.isSuccessful()) {
                    updateFirestoreData(userID, newName, newEmail, imageUrl);
                } else {
                    binding.updateProfileBtn.setEnabled(true);
                    String error = authTask.getException() != null ? authTask.getException().getMessage() : "Error";
                    Toast.makeText(EditProfileActivity.this, "Email update failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            updateFirestoreData(userID, newName, newEmail, imageUrl);
        }
    }

    private void updateFirestoreData(String userID, String name, String email, String imageUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("email", email);
        updates.put("profileImageUrl", imageUrl);

        firestore.collection("users").document(userID).update(updates)
                .addOnCompleteListener(dbTask -> {
                    binding.updateProfileBtn.setEnabled(true);
                    if (dbTask.isSuccessful()) {
                        Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfileActivity.this, "Failed to update database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}