package com.example.infotarghe;

public class Targa {
    private final String plateNumber;
    private final long timestamp;

    public Targa(String plateNumber, long timestamp) {
        this.plateNumber = plateNumber;
        this.timestamp = timestamp;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
