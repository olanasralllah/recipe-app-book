package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

public class RecipeDetailsActivity extends AppCompatActivity {
    ActivityRecipeDetailsBinding binding;
    String videoUrl = "";
    String recipeId = "";
    String recipeCreatorId = "";

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        Intent intent = getIntent();

        if (intent != null) {
            recipeId = intent.getStringExtra("recipe_id");
            recipeCreatorId = intent.getStringExtra("user_id");

            final String title = intent.getStringExtra("recipe_title");
            final String category = intent.getStringExtra("recipe_category");
            String ingredients = intent.getStringExtra("recipe_ingredients");
            String steps = intent.getStringExtra("recipe_steps");
            videoUrl = intent.getStringExtra("recipe_video_url");

            binding.detailTitleTv.setText(title);
            binding.detailCategoryTv.setText("Category: " + category);

            if (ingredients != null) {
                ingredients = ingredients.replace(",", "\n").trim();
                binding.detailIngredientsTv.setText(ingredients);
            }

            if (steps != null) {
                steps = steps.replace(",", "\n").trim();
                binding.detailStepsTv.setText(steps);
            }

            if (auth.getCurrentUser() != null && recipeCreatorId != null
                    && auth.getCurrentUser().getUid().equals(recipeCreatorId)) {
                binding.creatorActionsLayout.setVisibility(View.VISIBLE);
            } else {
                binding.creatorActionsLayout.setVisibility(View.GONE);
            }


            final String finalIngredients = intent.getStringExtra("recipe_ingredients");
            final String finalSteps = intent.getStringExtra("recipe_steps");

            binding.detailEditBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent editIntent = new Intent(RecipeDetailsActivity.this, EditRecipeActivity.class);
                    editIntent.putExtra("recipe_id", recipeId);
                    editIntent.putExtra("recipe_title", title);
                    editIntent.putExtra("recipe_category", category);
                    editIntent.putExtra("recipe_ingredients", finalIngredients);
                    editIntent.putExtra("recipe_steps", finalSteps);
                    editIntent.putExtra("recipe_video_url", videoUrl);
                    startActivity(editIntent);
                    finish();
                }
            });

            binding.detailDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (recipeId != null) {
                        firestore.collection("recipes").document(recipeId).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(RecipeDetailsActivity.this, "Recipe deleted successfully!", Toast.LENGTH_SHORT).show();
                                            finish();
                                        } else {
                                            Toast.makeText(RecipeDetailsActivity.this, "Failed to delete recipe.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                }
            });
        }

        binding.watchVideoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoUrl != null && !videoUrl.isEmpty() && videoUrl.startsWith("http")) {
                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                    startActivity(webIntent);
                } else {
                    Toast.makeText(RecipeDetailsActivity.this, "Invalid or missing video URL", Toast.LENGTH_SHORT).show();
                }
            }
        });







//        if (intent != null) {
//            String title = intent.getStringExtra("recipe_title");
//            String category = intent.getStringExtra("recipe_category");
//            String ingredients = intent.getStringExtra("recipe_ingredients");
//            String steps = intent.getStringExtra("recipe_steps");
//            videoUrl = intent.getStringExtra("recipe_video_url");
//
//            binding.detailTitleTv.setText(title);
//            binding.detailCategoryTv.setText("Category: " + category);
//            binding.detailIngredientsTv.setText(ingredients);
//            binding.detailStepsTv.setText(steps);
//        }

//
//        binding.watchVideoBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (videoUrl != null && !videoUrl.isEmpty() && videoUrl.startsWith("http")) {
//                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
//                    startActivity(webIntent);
//                } else {
//                    Toast.makeText(RecipeDetailsActivity.this, "Invalid or missing video URL", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//        });

    }
}