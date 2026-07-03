package com.example.recipebook;

public class Recipe {
    private String title;
    private String category;
    private String ingredients;
    private String steps;
    private String videoUrl;
    public Recipe() {}

    public Recipe(String title, String category, String ingredients, String steps, String videoUrl) {
        this.title = title;
        this.category = category;
        this.ingredients = ingredients;
        this.steps = steps;
        this.videoUrl = videoUrl;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getSteps() { return steps; }
    public void setSteps(String steps) { this.steps = steps; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
}
