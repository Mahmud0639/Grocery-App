package com.manuni.groceryapp;

public class ModelProduct {
    private String productId,productTitle,productDesc,productCategory,productQuantity,productIcon,productOriginalPrice,productDiscountPrice,
            productDiscountNote,productDiscountAvailable,productAvailable,timestamp,uid;

    public ModelProduct() {
    }

    public ModelProduct(String productId, String productTitle, String productDesc, String productCategory, String productQuantity, String productIcon, String productOriginalPrice, String productDiscountPrice, String productDiscountNote, String productDiscountAvailable,String productAvailable, String timestamp, String uid) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.productDesc = productDesc;
        this.productCategory = productCategory;
        this.productQuantity = productQuantity;
        this.productIcon = productIcon;
        this.productOriginalPrice = productOriginalPrice;
        this.productDiscountPrice = productDiscountPrice;
        this.productDiscountNote = productDiscountNote;
        this.productDiscountAvailable = productDiscountAvailable;
        this.timestamp = timestamp;
        this.uid = uid;
        this.productAvailable = productAvailable;
    }

    public String getProductAvailable() {
        return productAvailable;
    }

    public void setProductAvailable(String productAvailable) {
        this.productAvailable = productAvailable;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductDesc() {
        return productDesc;
    }

    public void setProductDesc(String productDesc) {
        this.productDesc = productDesc;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(String productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getProductIcon() {
        return productIcon;
    }

    public void setProductIcon(String productIcon) {
        this.productIcon = productIcon;
    }

    public String getProductOriginalPrice() {
        return productOriginalPrice;
    }

    public void setProductOriginalPrice(String productOriginalPrice) {
        this.productOriginalPrice = productOriginalPrice;
    }

    public String getProductDiscountPrice() {
        return productDiscountPrice;
    }

    public void setProductDiscountPrice(String productDiscountPrice) {
        this.productDiscountPrice = productDiscountPrice;
    }

    public String getProductDiscountNote() {
        return productDiscountNote;
    }

    public void setProductDiscountNote(String productDiscountNote) {
        this.productDiscountNote = productDiscountNote;
    }

    public String getProductDiscountAvailable() {
        return productDiscountAvailable;
    }

    public void setProductDiscountAvailable(String productDiscountAvailable) {
        this.productDiscountAvailable = productDiscountAvailable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
