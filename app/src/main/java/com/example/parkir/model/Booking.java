package com.example.parkir.model;

import com.google.firebase.Timestamp;

public class Booking {
    private String documentId; // ID dari dokumen booking itu sendiri
    private String locationId; // ID dari dokumen lokasi parkir
    private String locationName;
    private String areaName;
    private String slotNumber;
    private String userId;
    private Timestamp bookingTime;

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
}