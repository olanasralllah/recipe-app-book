package com.example.recipebook;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityEditRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditRecipeActivity extends AppCompatActivity {
    ActivityEditRecipeBinding binding;
    FirebaseFirestore firestore;
    String recipeId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        recipeId = getIntent().getStringExtra("recipe_id");
        String title = getIntent().getStringExtra("recipe_title");
        String category = getIntent().getStringExtra("recipe_category");
        String ingredients = getIntent().getStringExtra("recipe_ingredients");
        String steps = getIntent().getStringExtra("recipe_steps");
        String videoUrl = getIntent().getStringExtra("recipe_video_url");

        if (title != null) binding.editRecipeTitleEt.setText(title);
        if (category != null) binding.editRecipeCategoryEt.setText(category);
        if (ingredients != null) binding.editRecipeIngredientsEt.setText(ingredients);
        if (steps != null) binding.editRecipeStepsEt.setText(steps);
        if (videoUrl != null) binding.editRecipeVideoUrlEt.setText(videoUrl);

        binding.updateRecipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String updatedTitle = binding.editRecipeTitleEt.getText().toString().trim();
                String updatedCategory = binding.editRecipeCategoryEt.getText().toString().trim();
                String updatedIngredients = binding.editRecipeIngredientsEt.getText().toString().trim();
                String updatedSteps = binding.editRecipeStepsEt.getText().toString().trim();
                String updatedVideoUrl = binding.editRecipeVideoUrlEt.getText().toString().trim();

                if (updatedTitle.isEmpty() || updatedCategory.isEmpty() ||
                        updatedIngredients.isEmpty() || updatedSteps.isEmpty()) {
                    Toast.makeText(EditRecipeActivity.this, "Please fill in all required fields!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> updatedRecipe = new HashMap<>();
                updatedRecipe.put("title", updatedTitle);
                updatedRecipe.put("category", updatedCategory);
                updatedRecipe.put("ingredients", updatedIngredients);
                updatedRecipe.put("steps", updatedSteps);
                updatedRecipe.put("videoUrl", updatedVideoUrl);

                if (recipeId != null && !recipeId.isEmpty()) {
                    firestore.collection("recipes").document(recipeId)
                            .update(updatedRecipe)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditRecipeActivity.this, "Recipe Updated Successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                        Toast.makeText(EditRecipeActivity.this, "Failed to update: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(EditRecipeActivity.this, "Error: Missing Recipe ID!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}