package com.example.recipebook;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.recipebook.databinding.ActivityEditProfileBinding;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String currentName = getIntent().getStringExtra("user_name");
        String currentEmail = getIntent().getStringExtra("user_email");

        if (currentName != null) binding.editNameEt.setText(currentName);
        if (currentEmail != null) binding.editEmailEt.setText(currentEmail);


        binding.updateProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newName = binding.editNameEt.getText().toString().trim();
                String newEmail = binding.editEmailEt.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty()) {
                    Toast.makeText(EditProfileActivity.this, "Fields cannot be empty!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Profile Updated Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

    }
}