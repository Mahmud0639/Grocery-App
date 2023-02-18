package com.manuni.groceryapp;

public class ModelOrderedItems {
    private String pId,name,cost,price,quantity,proQuantity,prImage;

    public ModelOrderedItems() {
    }

    public ModelOrderedItems(String pId, String name, String cost, String price, String quantity,String proQuantity,String prImage) {
        this.pId = pId;
        this.name = name;
        this.cost = cost;
        this.price = price;
        this.quantity = quantity;
        this.proQuantity = proQuantity;
        this.prImage = prImage;
    }

    public String getPrImage() {
        return prImage;
    }

    public void setPrImage(String prImage) {
        this.prImage = prImage;
    }

    public String getProQuantity() {
        return proQuantity;
    }

    public void setProQuantity(String proQuantity) {
        this.proQuantity = proQuantity;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}