package com.manuni.groceryapp;

public class NotificationData {

    private String notificationType;
    private String buyerUid;
    private String sellerUid;
    private String orderId;
    private String notificationTitle;
    private String notificationMessage;

    public NotificationData(String notificationType, String buyerUid, String sellerUid, String orderId, String notificationTitle, String notificationMessage) {
        this.notificationType = notificationType;
        this.buyerUid = buyerUid;
        this.sellerUid = sellerUid;
        this.orderId = orderId;
        this.notificationTitle = notificationTitle;
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getBuyerUid() {
        return buyerUid;
    }

    public void setBuyerUid(String buyerUid) {
        this.buyerUid = buyerUid;
    }

    public String getSellerUid() {
        return sellerUid;
    }

    public void setSellerUid(String sellerUid) {
        this.sellerUid = sellerUid;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getNotificationTitle() {
        return notificationTitle;
    }

    public void setNotificationTitle(String notificationTitle) {
        this.notificationTitle = notificationTitle;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }
}
