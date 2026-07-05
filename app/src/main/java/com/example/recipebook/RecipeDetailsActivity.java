package com.example.recipebook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityRecipeDetailsBinding;

public class RecipeDetailsActivity extends AppCompatActivity {

     ActivityRecipeDetailsBinding binding;
     String videoUrl = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecipeDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("recipe_title");
            String category = intent.getStringExtra("recipe_category");
            String ingredients = intent.getStringExtra("recipe_ingredients");
            String steps = intent.getStringExtra("recipe_steps");
            videoUrl = intent.getStringExtra("recipe_video_url");

            binding.detailTitleTv.setText(title);
            binding.detailCategoryTv.setText("Category: " + category);
            binding.detailIngredientsTv.setText(ingredients);
            binding.detailStepsTv.setText(steps);
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

    }
}