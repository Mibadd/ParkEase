package com.example.parkir.model;

import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.List;

public class ParkingLocation {
    private String id; // To store Firestore document ID
    private String name;
    private String address;
    private GeoPoint coordinates;
    private List<ParkingArea> parking_areas;
    @ServerTimestamp
    private Date last_updated;

    public ParkingLocation() {}

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public GeoPoint getCoordinates() { return coordinates; }
    public List<ParkingArea> getParking_areas() { return parking_areas; }
    public Date getLast_updated() { return last_updated; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAddress(String address) { this.address = address; }
    public void setCoordinates(GeoPoint coordinates) { this.coordinates = coordinates; }
    public void setParking_areas(List<ParkingArea> parking_areas) { this.parking_areas = parking_areas; }
    public void setLast_updated(Date last_updated) { this.last_updated = last_updated; }
}