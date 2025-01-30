package com.example.infotarghe;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {

    // Lista di oggetti Targa
    private List<Targa> plateList = new ArrayList<>();

    /**
     * Imposta un'intera lista di oggetti Targa.
     * @param list La lista di oggetti Targa da impostare.
     */
    public void setPlateList(List<Targa> list) {
        plateList = list;
    }

    /**
     * Restituisce la lista di targhe. Se è vuota, la inizializza con un valore predefinito.
     * @return La lista di oggetti Targa.
     */
    public List<Targa> getPlateList() {
        if (plateList.isEmpty()) {
            // Aggiungi un esempio predefinito con il timestamp corrente
            long timestamp = System.currentTimeMillis(); // Ottieni il timestamp attuale
            plateList.add(new Targa("ES520DV", timestamp, 0.95f)); // Valore di esempio
        }
        return plateList;
    }
}
