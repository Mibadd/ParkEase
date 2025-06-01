package com.example.parkir.model;

import java.util.List; // Tambahkan import ini

public class ParkingArea {
    private String area_name;
    private String vehicle_type;
    private int total_spots;
    // private int occupied_spots; // Komentari atau hapus, akan dihitung dari list slots
    private double price_per_hour;
    private int floor_level;
    private List<ParkingSlot> slots; // Tambahkan field ini

    public ParkingArea() {}

    // Getters
    public String getArea_name() { return area_name; }
    public String getVehicle_type() { return vehicle_type; }
    public int getTotal_spots() { return total_spots; }
    public double getPrice_per_hour() { return price_per_hour; }
    public int getFloor_level() { return floor_level; }
    public List<ParkingSlot> getSlots() { return slots; } // Getter untuk slots

    // Setters
    public void setArea_name(String area_name) { this.area_name = area_name; }
    public void setVehicle_type(String vehicle_type) { this.vehicle_type = vehicle_type; }
    public void setTotal_spots(int total_spots) { this.total_spots = total_spots; }
    public void setPrice_per_hour(double price_per_hour) { this.price_per_hour = price_per_hour; }
    public void setFloor_level(int floor_level) { this.floor_level = floor_level; }
    public void setSlots(List<ParkingSlot> slots) { this.slots = slots; } // Setter untuk slots

    // Method helper untuk menghitung slot terisi dari list
    public int getOccupied_spots_count() {
        if (slots == null) return 0;
        int count = 0;
        for (ParkingSlot slot : slots) {
            if ("occupied".equalsIgnoreCase(slot.getStatus())) {
                count++;
            }
        }
        return count;
    }

    // Method helper untuk menghitung slot kosong
    public int getAvailable_spots_count() {
        if (slots == null) return total_spots; // Fallback jika list slots null
        return total_spots - getOccupied_spots_count();
    }
}