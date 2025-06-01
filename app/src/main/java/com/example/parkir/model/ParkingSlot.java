package com.example.parkir.model;

public class ParkingSlot {
    private String slot_number;
    private String status; // "empty", "occupied"
    private String occupied_by_user_id;

    // Konstruktor kosong untuk Firestore
    public ParkingSlot() {}

    public ParkingSlot(String slot_number, String status, String occupied_by_user_id) {
        this.slot_number = slot_number;
        this.status = status;
        this.occupied_by_user_id = occupied_by_user_id;
    }

    // Getters
    public String getSlot_number() { return slot_number; }
    public String getStatus() { return status; }
    public String getOccupied_by_user_id() { return occupied_by_user_id; }

    // Setters
    public void setSlot_number(String slot_number) { this.slot_number = slot_number; }
    public void setStatus(String status) { this.status = status; }
    public void setOccupied_by_user_id(String occupied_by_user_id) { this.occupied_by_user_id = occupied_by_user_id; }
}