package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
     ActivityMainBinding binding;
     RecipeAdapter adapter;
     List<Recipe> recipeList;
    FirebaseFirestore firestore;
    private String currentSearchQuery = "";
    private ListenerRegistration recipesListenerRegistration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("All"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Main Dishes"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Desserts"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Appetizers"));
        binding.tabLayoutCategories.addTab(binding.tabLayoutCategories.newTab().setText("Drinks"));

        recipeList = new ArrayList<>();
        binding.rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(recipeList, false);
        binding.rvRecipes.setAdapter(adapter);

        getRecipesFromFirestore("All");

        binding.tabLayoutCategories.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String categoryName = tab.getText().toString();
                getRecipesFromFirestore(categoryName);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                applyFilter();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                applyFilter();
                return false;
            }
        });

        binding.searchView.setOnCloseListener(new androidx.appcompat.widget.SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                currentSearchQuery = "";
                applyFilter();
                return false;
            }
        });

        binding.profileIconImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Profile Clicked!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        binding.fabAddRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
                startActivity(intent);
            }
        });
    }

    private void getRecipesFromFirestore(String category) {

        if (recipesListenerRegistration != null) {
            recipesListenerRegistration.remove();
        }

        Query query = firestore.collection("recipes");

        if (!category.equals("All")) {
            query = query.whereEqualTo("category", category);
        }

        recipesListenerRegistration = query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(MainActivity.this, "Error loading recipes: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    recipeList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String title = doc.getString("title");
                        String cat = doc.getString("category");
                        String ingredients = doc.getString("ingredients");
                        String steps = doc.getString("steps");
                        String url = doc.getString("videoUrl");
                        String recipeId = doc.getString("recipeId");
                        String userId = doc.getString("userId");
                        String imageUrl = doc.getString("imageUrl");

                        Recipe recipe = new Recipe(title, cat, ingredients, steps, url, userId, imageUrl);
                        recipe.setRecipeId(recipeId);

                        recipeList.add(recipe);
                    }
                    applyFilter();
                }
            }
        });
    }
    private void applyFilter() {
    List<Recipe> filteredList = new ArrayList<>();
    for (Recipe recipe : recipeList) {
        if (recipe.getTitle() != null && recipe.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase())) {
            filteredList.add(recipe);
        }
    }
    adapter.updateList(filteredList);
}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recipesListenerRegistration != null) {
            recipesListenerRegistration.remove();
        }
    }
}