package com.manuni.groceryapp;

public class DeleteCategoryModel {
    private String category,categoryId;

    public DeleteCategoryModel() {
    }

    public DeleteCategoryModel(String category, String categoryId) {
        this.category = category;
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }
}
