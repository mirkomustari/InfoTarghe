package com.example.infotarghe;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Targa {
    private String numeroTarga;  // Numero di targa
    private long timestamp;      // Timestamp del rilevamento in millisecondi
    private float probabilita;   // Probabilità di rilevamento

    // Costruttore
    public Targa(String numeroTarga, long timestamp, float probabilita) {
        this.numeroTarga = numeroTarga;
        this.timestamp = timestamp;
        this.probabilita = probabilita;
    }

    // Getter e Setter
    public String getNumeroTarga() {
        return numeroTarga;
    }

    public void setNumeroTarga(String numeroTarga) {
        this.numeroTarga = numeroTarga;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getProbabilita() {
        return probabilita;
    }

    public void setProbabilita(float probabilita) {
        this.probabilita = probabilita;
    }

    /**
     * Restituisce il timestamp in formato leggibile.
     * @return Una stringa formattata con data e ora.
     */
    public String getDataOraFormattata() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return "Targa{" +
                "numeroTarga='" + numeroTarga + '\'' +
                ", timestamp=" + timestamp +
                " (" + getDataOraFormattata() + ")" +
                ", probabilita=" + probabilita +
                '}';
    }
}
