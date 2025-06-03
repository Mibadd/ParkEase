package com.example.parkir.model;

import java.io.Serializable;

public class ParkingSlot implements Serializable {
    private String slot_number;
    private String status;

    public ParkingSlot() {}

    public ParkingSlot(String slot_number, String status) {
        this.slot_number = slot_number;
        this.status = status;
    }

    public String getSlot_number() {
        return slot_number;
    }

    public void setSlot_number(String slot_number) {
        this.slot_number = slot_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
