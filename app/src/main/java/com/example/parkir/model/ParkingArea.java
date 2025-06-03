package com.example.parkir.model;

import java.io.Serializable;
import java.util.List;

public class ParkingArea implements Serializable {
    private String area_name;
    private String vehicle_type;
    private List<ParkingSlot> slots;
    private int total_spots;
    private int occupied_spots_count; // TAMBAHKAN INI
    private int available_spots_count; // TAMBAHKAN INI
    private int price_per_hour; // TAMBAHKAN INI
    private int floor_level; // TAMBAHKAN INI

    public ParkingArea() {}

    // Constructor lengkap
    public ParkingArea(String area_name, String vehicle_type, List<ParkingSlot> slots, int total_spots, int occupied_spots_count, int available_spots_count, int price_per_hour, int floor_level) {
        this.area_name = area_name;
        this.vehicle_type = vehicle_type;
        this.slots = slots;
        this.total_spots = total_spots;
        this.occupied_spots_count = occupied_spots_count;
        this.available_spots_count = available_spots_count;
        this.price_per_hour = price_per_hour;
        this.floor_level = floor_level;
    }

    // Getters and Setters
    public String getArea_name() {
        return area_name;
    }

    public void setArea_name(String area_name) {
        this.area_name = area_name;
    }

    public String getVehicle_type() {
        return vehicle_type;
    }

    public void setVehicle_type(String vehicle_type) {
        this.vehicle_type = vehicle_type;
    }

    public List<ParkingSlot> getSlots() {
        return slots;
    }

    public void setSlots(List<ParkingSlot> slots) {
        this.slots = slots;
    }

    public int getTotal_spots() {
        return total_spots;
    }

    public void setTotal_spots(int total_spots) {
        this.total_spots = total_spots;
    }

    public int getOccupied_spots_count() {
        return occupied_spots_count;
    }

    public void setOccupied_spots_count(int occupied_spots_count) {
        this.occupied_spots_count = occupied_spots_count;
    }

    public int getAvailable_spots_count() {
        return available_spots_count;
    }

    public void setAvailable_spots_count(int available_spots_count) {
        this.available_spots_count = available_spots_count;
    }

    public int getPrice_per_hour() {
        return price_per_hour;
    }

    public void setPrice_per_hour(int price_per_hour) {
        this.price_per_hour = price_per_hour;
    }

    public int getFloor_level() {
        return floor_level;
    }

    public void setFloor_level(int floor_level) {
        this.floor_level = floor_level;
    }
}