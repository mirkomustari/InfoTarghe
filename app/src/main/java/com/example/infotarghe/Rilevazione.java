package com.example.infotarghe;

public class Rilevazione {
    private final String plateNumber;
    private int count; // Numero di rilevamenti
    private final long firstTimestamp; // Timestamp del primo rilevamento

    public Rilevazione(String plateNumber, long firstTimestamp) {
        this.plateNumber = plateNumber;
        this.firstTimestamp = firstTimestamp;
        this.count = 1; // Inizia con 1 rilevamento
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public int getCount() {
        return count;
    }

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public void incrementCount() {
        count++;
    }
}
