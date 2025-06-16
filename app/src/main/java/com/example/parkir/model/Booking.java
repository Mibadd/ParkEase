package com.example.parkir.model;

import com.google.firebase.Timestamp;

public class Booking {
    private String documentId;
    private String locationId;
    private String locationName;
    private String areaName;
    private String slotNumber;
    private String userId;
    private Timestamp bookingTime;
    private long totalPrice;
    private long durationInHours;
    // Tambahkan field baru
    private String paymentMethod;


    // Diperlukan constructor kosong untuk Firestore
    public Booking() {}

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getLocationId() { return locationId; }
    public void setLocationId(String locationId) { this.locationId = locationId; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }
    public String getSlotNumber() { return slotNumber; }
    public void setSlotNumber(String slotNumber) { this.slotNumber = slotNumber; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Timestamp getBookingTime() { return bookingTime; }
    public void setBookingTime(Timestamp bookingTime) { this.bookingTime = bookingTime; }
    public long getTotalPrice() { return totalPrice; }
    public void setTotalPrice(long totalPrice) { this.totalPrice = totalPrice; }
    public long getDurationInHours() { return durationInHours; }
    public void setDurationInHours(long durationInHours) { this.durationInHours = durationInHours; }

    // Getter and Setter untuk paymentMethod
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}