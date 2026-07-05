package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
     ActivityMainBinding binding;
     RecipeAdapter adapter;
     List<Recipe> recipeList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("All"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Main Dishes"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Desserts"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Appetizers"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Drinks"));

        recipeList = new ArrayList<>();
        recipeList.add(new Recipe("Chocolate Cake", "Desserts", "Flour, Sugar, Cocoa", "Mix and bake", "https://youtube.com/..."));
        recipeList.add(new Recipe("Chicken Biryani", "Main Dishes", "Rice, Chicken, Spices", "Cook rice and chicken", "https://youtube.com/..."));
        recipeList.add(new Recipe("Strawberry Smoothie", "Drinks", "Strawberry, Milk, Honey", "Blend together", "https://youtube.com/..."));

        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList);
        binding.rvRecipes.setAdapter(adapter);



        binding.tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String categoryName = tab.getText().toString();
                Toast.makeText(MainActivity.this, "Category: " + categoryName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });



        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Showing all recipes", Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });



        binding.profileIconImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                Toast.makeText(MainActivity.this, "Navigating to Profile...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.fabAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Navigating to Add Recipe...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}