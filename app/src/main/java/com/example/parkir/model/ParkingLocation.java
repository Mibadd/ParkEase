package com.example.parkir.model;

import com.google.firebase.firestore.GeoPoint;
import java.util.List;

public class ParkingLocation {
    private String documentId; // PASTIKAN FIELD INI ADA
    private String name;
    private String address;
    private GeoPoint coordinates;
    private List<ParkingArea> parking_areas;

    // Diperlukan constructor kosong
    public ParkingLocation() {}

    // GETTER DAN SETTER UNTUK documentId
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    // ... getter dan setter lainnya ...
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public GeoPoint getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(GeoPoint coordinates) {
        this.coordinates = coordinates;
    }

    public List<ParkingArea> getParking_areas() {
        return parking_areas;
    }

    public void setParking_areas(List<ParkingArea> parking_areas) {
        this.parking_areas = parking_areas;
    }
}