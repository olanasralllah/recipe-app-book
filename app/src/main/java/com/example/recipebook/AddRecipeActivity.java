package com.example.recipebook;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityAddRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddRecipeActivity extends AppCompatActivity {
    Retrofit retrofit;
    ApiRequests apiRequests;
    ActivityAddRecipeBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    private Bitmap selectedBitmap = null;

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            selectedBitmap = BitmapFactory.decodeStream(inputStream);

                            binding.recipeImgPreview.setImageBitmap(selectedBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AddRecipeActivity.this, "Failed to load image!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://bilzduvkqyssofbzugve.supabase.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiRequests = retrofit.create(ApiRequests.class);


        binding.selectImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagePickerLauncher.launch("image/*");
            }
        });

        binding.saveRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = binding.recipeTitleEt.getText().toString().trim();
                String category = binding.recipeCategoryEt.getText().toString().trim();
                String ingredients = binding.recipeIngredientsEt.getText().toString().trim();
                String steps = binding.recipeStepsEt.getText().toString().trim();
                String url = binding.recipeVideoUrlEt.getText().toString().trim();

                if (title.isEmpty() || category.isEmpty() || ingredients.isEmpty() || steps.isEmpty() || url.isEmpty()) {
                    Toast.makeText(AddRecipeActivity.this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedBitmap == null) {
                    Toast.makeText(AddRecipeActivity.this, "Please select an image for the recipe! ", Toast.LENGTH_SHORT).show();
                    return;
                }
                String formattedCategory = formatCategoryString(category);

                uploadImageAndSaveRecipe(title, formattedCategory, ingredients, steps, url);


            }

        });
    }
    private void uploadImageAndSaveRecipe(String title, String category, String ingredients, String steps, String videoUrl) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading image & saving recipe...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            String imageName = "recipe_" + System.currentTimeMillis() + ".png";

            File imageFile = bitmapToFile(selectedBitmap, imageName);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageName, requestFile);

            Call<ResponseBody> call = apiRequests.uploadFile("recipe-images", "recipes/" + imageName, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.d("UPLOAD", "Image uploaded successfully to Supabase!");

                        String finalImageUrl = "https://bilzduvkqyssofbzugve.supabase.co/storage/v1/object/public/recipe-images/recipes/" + imageName;

                        saveToFirestore(title, category, ingredients, steps, videoUrl, finalImageUrl, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(AddRecipeActivity.this, "Supabase Upload Failed! Code: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    Toast.makeText(AddRecipeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("UPLOAD", "Error: " + t.getMessage());
                }
            });

        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
            Toast.makeText(this, "Failed to prepare image file for upload", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToFirestore(String title, String category, String ingredients, String steps, String videoUrl, String imageUrl, ProgressDialog progressDialog) {
        String currentUserId = "";
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        DocumentReference newRecipeRef = firestore.collection("recipes").document();
        String generatedRecipeId = newRecipeRef.getId();

        Recipe newRecipe = new Recipe(title, category, ingredients, steps, videoUrl, currentUserId, imageUrl);
        newRecipe.setRecipeId(generatedRecipeId);

        newRecipeRef.set(newRecipe)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(AddRecipeActivity.this, "Recipe Saved & Published Successfully!", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(AddRecipeActivity.this, "Failed to save to Firestore: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private File bitmapToFile(Bitmap bitmap, String name) throws IOException {
        File f = new File(getApplicationContext().getCacheDir(), name);
        f.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();

        FileOutputStream fos = new FileOutputStream(f);
        fos.write(bitmapdata);
        fos.flush();
        fos.close();
        return f;
    }

    private String formatCategoryString(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        String[] words = input.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}