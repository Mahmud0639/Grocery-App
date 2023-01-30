package com.manuni.groceryapp;

public class ModelReview {
    private String uid, ratings, reviews,timestamp;

    public ModelReview() {
    }

    public ModelReview(String uid, String ratings, String reviews, String timestamp) {
        this.uid = uid;
        this.ratings = ratings;
        this.reviews = reviews;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRatings() {
        return ratings;
    }

    public void setRatings(String ratings) {
        this.ratings = ratings;
    }

    public String getReviews() {
        return reviews;
    }

    public void setReviews(String reviews) {
        this.reviews = reviews;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
