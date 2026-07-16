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

import com.example.recipebook.databinding.ActivityAddRecipeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


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
                } else {

                    String currentUserId = "";
                    if (auth.getCurrentUser() != null) {
                        currentUserId = auth.getCurrentUser().getUid();
                    }

                    DocumentReference newRecipeRef = firestore.collection("recipes").document();
                    String generatedRecipeId = newRecipeRef.getId();

                    Recipe newRecipe = new Recipe(title, category, ingredients, steps, url, currentUserId);
                    newRecipe.setRecipeId(generatedRecipeId);

                    newRecipeRef.set(newRecipe)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AddRecipeActivity.this, "Recipe Saved & Published Successfully!", Toast.LENGTH_LONG).show();
                                        finish();
                                    } else {
                                        Toast.makeText(AddRecipeActivity.this, "Failed to save recipe: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}