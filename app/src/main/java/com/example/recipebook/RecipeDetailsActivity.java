package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class RecipeDetailsActivity extends AppCompatActivity {
    ActivityRecipeDetailsBinding binding;
    String recipeId = "";
    String recipeCreatorId = "";
    String videoUrl = "";
    String imageUrl = "";
    String currentTitle = "";
    String currentCategory = "";
    String currentIngredients = "";
    String currentSteps = "";
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.detailIngredientsTv.setMovementMethod(new ScrollingMovementMethod());
        binding.detailStepsTv.setMovementMethod(new ScrollingMovementMethod());

        recipeId = getIntent().getStringExtra("recipe_id");

        if (recipeId != null && !recipeId.isEmpty()) {
            firestore.collection("recipes").document(recipeId)
                    .addSnapshotListener((documentSnapshot, error) -> {
                        if (error != null) {
                            Toast.makeText(RecipeDetailsActivity.this, "Error loading details", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (documentSnapshot != null && documentSnapshot.exists()) {
                            currentTitle = documentSnapshot.getString("title");
                            currentCategory = documentSnapshot.getString("category");
                            currentIngredients = documentSnapshot.getString("ingredients");
                            currentSteps = documentSnapshot.getString("steps");
                            videoUrl = documentSnapshot.getString("videoUrl");
                            imageUrl = documentSnapshot.getString("imageUrl");
                            recipeCreatorId = documentSnapshot.getString("userId");

                            binding.detailTitleTv.setText(currentTitle);
                            binding.detailCategoryTv.setText("Category: " + currentCategory);

                            if (currentIngredients != null) {
                                binding.detailIngredientsTv.setText(currentIngredients.replace(",", "\n").trim());
                            }
                            if (currentSteps != null) {
                                binding.detailStepsTv.setText(currentSteps.replace(",", "\n").trim());
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                Picasso.get().load(imageUrl)
                                        .placeholder(android.R.drawable.ic_menu_gallery)
                                        .error(android.R.drawable.ic_menu_report_image)
                                        .into(binding.detailRecipeImg);
                            }

                            if (auth.getCurrentUser() != null && recipeCreatorId != null
                                    && auth.getCurrentUser().getUid().equals(recipeCreatorId)) {
                                binding.creatorActionsLayout.setVisibility(View.VISIBLE);
                            } else {
                                binding.creatorActionsLayout.setVisibility(View.GONE);
                            }
                        }
                    });
        }

        binding.detailEditBtn.setOnClickListener(v -> {
            Intent editIntent = new Intent(RecipeDetailsActivity.this, EditRecipeActivity.class);
            editIntent.putExtra("recipe_id", recipeId);
            editIntent.putExtra("recipe_title", currentTitle);
            editIntent.putExtra("recipe_category", currentCategory);
            editIntent.putExtra("recipe_ingredients", currentIngredients);
            editIntent.putExtra("recipe_steps", currentSteps);
            editIntent.putExtra("recipe_video_url", videoUrl);
            editIntent.putExtra("recipe_image_url", imageUrl);
            startActivity(editIntent);
        });

        binding.detailDeleteBtn.setOnClickListener(v -> {
            if (recipeId != null) {
                firestore.collection("recipes").document(recipeId).delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RecipeDetailsActivity.this, "Recipe deleted successfully! 🎉", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(RecipeDetailsActivity.this, "Failed to delete recipe.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        binding.watchVideoBtn.setOnClickListener(view -> {
            if (videoUrl != null && !videoUrl.isEmpty() && videoUrl.startsWith("http")) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(webIntent);
            } else {
                Toast.makeText(RecipeDetailsActivity.this, "Invalid or missing video URL", Toast.LENGTH_SHORT).show();
            }
        });
    }
}