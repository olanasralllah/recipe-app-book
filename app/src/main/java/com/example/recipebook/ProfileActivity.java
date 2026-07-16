package com.example.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.recipebook.databinding.ActivityProfileBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {
    ActivityProfileBinding binding;
    FirebaseAuth auth;
    FirebaseFirestore firestore;

    RecipeAdapter adapter;
    List<Recipe> myRecipesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        myRecipesList = new ArrayList<>();
        binding.myRecipesRv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(myRecipesList);
        binding.myRecipesRv.setAdapter(adapter);

        loadUserDataFromFirestore();
        loadMyRecipesFromFirestore();

        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(ProfileActivity.this, "Logging out...", Toast.LENGTH_SHORT).show();

                if (auth.getCurrentUser() != null) {
                    auth.signOut();
                }

                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        binding.editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("user_name", binding.profileNameTv.getText().toString());
                intent.putExtra("user_email", binding.profileEmailTv.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void loadUserDataFromFirestore() {
        if (auth.getCurrentUser() != null) {
            String userID = auth.getCurrentUser().getUid();

            firestore.collection("users").document(userID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    String name = document.getString("name");
                                    String email = document.getString("email");

                                    if (name != null) binding.profileNameTv.setText(name);
                                    if (email != null) binding.profileEmailTv.setText(email);
                                }
                            } else {
                                Toast.makeText(ProfileActivity.this, "Failed to load profile data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void loadMyRecipesFromFirestore() {
        if (auth.getCurrentUser() != null) {
            String currentUserId = auth.getCurrentUser().getUid();

            firestore.collection("recipes")
                    .whereEqualTo("userId", currentUserId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(ProfileActivity.this, "Error loading your recipes", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (value != null) {
                                myRecipesList.clear();
                                for (DocumentSnapshot doc : value.getDocuments()) {
                                    String title = doc.getString("title");
                                    String cat = doc.getString("category");
                                    String ingredients = doc.getString("ingredients");
                                    String steps = doc.getString("steps");
                                    String url = doc.getString("videoUrl");
                                    String recipeId = doc.getString("recipeId");
                                    String userId = doc.getString("userId");

                                    Recipe recipe = new Recipe(title, cat, ingredients, steps, url, userId);
                                    recipe.setRecipeId(recipeId);

                                    myRecipesList.add(recipe);
                                }

                                adapter.notifyDataSetChanged();

                                binding.myRecipesCountTv.setText(String.valueOf(myRecipesList.size()));
                            }
                        }
                    });
        }
    }
}