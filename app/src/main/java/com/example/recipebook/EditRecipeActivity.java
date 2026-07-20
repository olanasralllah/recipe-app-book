package com.example.recipebook;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.example.recipebook.databinding.ActivityEditRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditRecipeActivity extends AppCompatActivity {
    ActivityEditRecipeBinding binding;
    FirebaseFirestore firestore;
    Retrofit retrofit;
    ApiRequests apiRequests;
    String recipeId;
    String currentImageUrl;
    private Bitmap newSelectedBitmap = null;


    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri uri) {
                    if (uri != null) {
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(uri);
                            newSelectedBitmap = BitmapFactory.decodeStream(inputStream);
                            binding.editRecipeImgPreview.setImageBitmap(newSelectedBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(EditRecipeActivity.this, "Failed to load new image!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        retrofit = new Retrofit.Builder()
                .baseUrl("https://bilzduvkqyssofbzugve.supabase.co/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiRequests = retrofit.create(ApiRequests.class);

        recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");
        String category = getIntent().getStringExtra("recipe_category");
        String ingredients = getIntent().getStringExtra("recipe_ingredients");
        String steps = getIntent().getStringExtra("recipe_steps");
        String videoUrl = getIntent().getStringExtra("recipe_video_url");
        currentImageUrl = getIntent().getStringExtra("recipe_image_url");

        if (title != null) binding.editRecipeTitleEt.setText(title);
        if (category != null) binding.editRecipeCategoryEt.setText(category);
        if (ingredients != null) binding.editRecipeIngredientsEt.setText(ingredients);
        if (steps != null) binding.editRecipeStepsEt.setText(steps);
        if (videoUrl != null) binding.editRecipeVideoUrlEt.setText(videoUrl);

        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Picasso.get().load(currentImageUrl).placeholder(android.R.drawable.ic_menu_gallery).into(binding.editRecipeImgPreview);
        }

        binding.editSelectImgBtn.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        binding.updateRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedTitle = binding.editRecipeTitleEt.getText().toString().trim();
                String rawCategory = binding.editRecipeCategoryEt.getText().toString().trim();
                String updatedIngredients = binding.editRecipeIngredientsEt.getText().toString().trim();
                String updatedSteps = binding.editRecipeStepsEt.getText().toString().trim();
                String updatedVideoUrl = binding.editRecipeVideoUrlEt.getText().toString().trim();

                if (updatedTitle.isEmpty() || rawCategory.isEmpty() ||
                        updatedIngredients.isEmpty() || updatedSteps.isEmpty()) {
                    Toast.makeText(EditRecipeActivity.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String updatedCategory = formatCategoryString(rawCategory);
                binding.editProgressBar.setVisibility(View.VISIBLE);

                if (newSelectedBitmap != null) {
                    uploadNewImageAndUpdate(updatedTitle, updatedCategory, updatedIngredients, updatedSteps, updatedVideoUrl);
                } else {
                    updateFirestoreData(updatedTitle, updatedCategory, updatedIngredients, updatedSteps, updatedVideoUrl, currentImageUrl);
                }

            }
        });

    }
    private void uploadNewImageAndUpdate(String title, String category, String ingredients, String steps, String videoUrl) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading new image... ");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            String imageName = "recipe_" + System.currentTimeMillis() + ".png";
            File imageFile = bitmapToFile(newSelectedBitmap, imageName);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", imageName, requestFile);

            Call<ResponseBody> call = apiRequests.uploadFile("recipe-images", "recipes/" + imageName, body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        String finalImageUrl = "https://bilzduvkqyssofbzugve.supabase.co/storage/v1/object/public/recipe-images/recipes/" + imageName;
                        updateFirestoreData(title, category, ingredients, steps, videoUrl, finalImageUrl);
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Image Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.show();
                    Toast.makeText(EditRecipeActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    private void updateFirestoreData(String title, String category, String ingredients, String steps, String videoUrl, String imageUrl) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating recipe details... ");
        progressDialog.setCancelable(false);
        progressDialog.show();

        Map<String, Object> updatedRecipe = new HashMap<>();
        updatedRecipe.put("title", title);
        updatedRecipe.put("category", category);
        updatedRecipe.put("ingredients", ingredients);
        updatedRecipe.put("steps", steps);
        updatedRecipe.put("videoUrl", videoUrl);
        updatedRecipe.put("imageUrl", imageUrl);

        firestore.collection("recipes").document(recipeId)
                .update(updatedRecipe)
                .addOnCompleteListener(task -> {
                    binding.editProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(EditRecipeActivity.this, "Recipe Updated Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditRecipeActivity.this, "Failed to update!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private File bitmapToFile(Bitmap bitmap, String name) throws IOException {
    File f = new File(getApplicationContext().getCacheDir(), name);
    f.createNewFile();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
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