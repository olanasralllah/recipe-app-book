package com.example.recipebook;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipebook.databinding.ItemRecipeBinding;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder>{
    private List<Recipe> recipeList;
    private boolean showActions;

    public RecipeAdapter(List<Recipe> recipeList, boolean showActions) {
        this.recipeList = recipeList;
        this.showActions = showActions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecipeBinding binding = ItemRecipeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.binding.recipeTitleTv.setText(recipe.getTitle());
        holder.binding.recipeCategoryTv.setText("Category: " + recipe.getCategory());

        if (showActions) {
            holder.binding.actionsLayout.setVisibility(View.VISIBLE);
        } else {
            holder.binding.actionsLayout.setVisibility(View.GONE);
        }

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().trim().isEmpty()) {
            try {
                com.squareup.picasso.Picasso.get()
                        .load(recipe.getImageUrl().trim())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(holder.binding.recipeImg);
            } catch (Exception e) {
                holder.binding.recipeImg.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.binding.recipeImg.setImageResource(android.R.drawable.ic_menu_gallery);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), RecipeDetailsActivity.class);
                intent.putExtra("recipe_id", recipe.getRecipeId());
                intent.putExtra("user_id", recipe.getUserId());
                intent.putExtra("recipe_title", recipe.getTitle());
                intent.putExtra("recipe_category", recipe.getCategory());
                intent.putExtra("recipe_ingredients", recipe.getIngredients());
                intent.putExtra("recipe_steps", recipe.getSteps());
                intent.putExtra("recipe_video_url", recipe.getVideoUrl());
                intent.putExtra("recipe_image_url", recipe.getImageUrl());

                view.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateList(List<Recipe> newList) {
        this.recipeList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemRecipeBinding binding;
        public ViewHolder(@NonNull ItemRecipeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
