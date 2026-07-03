package com.example.recipebook;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityAddRecipeBinding;

public class AddRecipeActivity extends AppCompatActivity {
    ActivityAddRecipeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddRecipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                    Toast.makeText(AddRecipeActivity.this, "Recipe Saved Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

    }
}