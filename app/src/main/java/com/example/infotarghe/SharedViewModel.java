package com.example.infotarghe;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<Map<String, Rilevazione>> plateRilevazioniLiveData = new MutableLiveData<>(new HashMap<>());
    private int totalValidPlates = 0;  // Contatore per targhe valide
    private int totalInvalidPlates = 0; // Contatore per targhe non valide

    // Aggiunge una targa valida o aggiorna i conteggi di rilevamenti
    public void addValidPlate(String plateNumber, long timestamp) {
        Map<String, Rilevazione> plateMap = plateRilevazioniLiveData.getValue();
        if (plateMap == null) {
            plateMap = new HashMap<>();
        }

        // Se la targa esiste, incrementa il conteggio; altrimenti, crea una nuova rilevazione
        if (plateMap.containsKey(plateNumber)) {
            plateMap.get(plateNumber).incrementCount();
        } else {
            plateMap.put(plateNumber, new Rilevazione(plateNumber, timestamp));
        }

        // Incrementa il contatore di targhe valide
        totalValidPlates++;
        plateRilevazioniLiveData.setValue(plateMap); // Aggiorna la LiveData
    }

    // Incrementa il contatore per targhe non valide
    public void incrementInvalidPlates() {
        totalInvalidPlates++;
    }

    public MutableLiveData<Map<String, Rilevazione>> getPlateRilevazioniLiveData() {
        return plateRilevazioniLiveData;
    }

    public int getTotalValidPlates() {
        return totalValidPlates;
    }

    public int getTotalInvalidPlates() {
        return totalInvalidPlates;
    }
}
