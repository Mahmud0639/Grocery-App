package com.manuni.groceryapp;

public class ModelCalculation {
    private String timestamp,completed,cancelled,deliveryFeeFullDay,shopName,fullAddress,inProgress,totalOfCompleted,totalOfCancelled,totalOfCost,totalOfInProgress,shopDeliveryFee,shopUid,paymentStatus,key;

    public ModelCalculation() {
    }

    public ModelCalculation(String timestamp, String completed, String cancelled, String deliveryFeeFullDay, String shopName, String fullAddress, String inProgress, String totalOfCompleted, String totalOfCancelled, String totalOfCost, String totalOfInProgress, String shopDeliveryFee, String shopUid, String paymentStatus, String key) {
        this.timestamp = timestamp;
        this.completed = completed;
        this.cancelled = cancelled;
        this.deliveryFeeFullDay = deliveryFeeFullDay;
        this.shopName = shopName;
        this.fullAddress = fullAddress;
        this.inProgress = inProgress;
        this.totalOfCompleted = totalOfCompleted;
        this.totalOfCancelled = totalOfCancelled;
        this.totalOfCost = totalOfCost;
        this.totalOfInProgress = totalOfInProgress;
        this.shopDeliveryFee = shopDeliveryFee;
        this.shopUid = shopUid;
        this.paymentStatus = paymentStatus;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getCancelled() {
        return cancelled;
    }

    public void setCancelled(String cancelled) {
        this.cancelled = cancelled;
    }

    public String getDeliveryFeeFullDay() {
        return deliveryFeeFullDay;
    }

    public void setDeliveryFeeFullDay(String deliveryFeeFullDay) {
        this.deliveryFeeFullDay = deliveryFeeFullDay;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getInProgress() {
        return inProgress;
    }

    public void setInProgress(String inProgress) {
        this.inProgress = inProgress;
    }

    public String getTotalOfCompleted() {
        return totalOfCompleted;
    }

    public void setTotalOfCompleted(String totalOfCompleted) {
        this.totalOfCompleted = totalOfCompleted;
    }

    public String getTotalOfCancelled() {
        return totalOfCancelled;
    }

    public void setTotalOfCancelled(String totalOfCancelled) {
        this.totalOfCancelled = totalOfCancelled;
    }

    public String getTotalOfCost() {
        return totalOfCost;
    }

    public void setTotalOfCost(String totalOfCost) {
        this.totalOfCost = totalOfCost;
    }

    public String getTotalOfInProgress() {
        return totalOfInProgress;
    }

    public void setTotalOfInProgress(String totalOfInProgress) {
        this.totalOfInProgress = totalOfInProgress;
    }

    public String getShopDeliveryFee() {
        return shopDeliveryFee;
    }

    public void setShopDeliveryFee(String shopDeliveryFee) {
        this.shopDeliveryFee = shopDeliveryFee;
    }

    public String getShopUid() {
        return shopUid;
    }

    public void setShopUid(String shopUid) {
        this.shopUid = shopUid;
    }
}
