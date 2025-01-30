package com.example.infotarghe.ui.registra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.infotarghe.SharedViewModel;
import com.example.infotarghe.Targa; // Import della classe Targa
import com.example.infotarghe.databinding.FragmentRegistraBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class RegistraFragment extends Fragment {

    private SharedViewModel sharedViewModel; // ViewModel condiviso
    private FragmentRegistraBinding binding; // Binding del layout
    private LinearLayout lista; // Contenitore dinamico delle targhe

    /**
     * Verifica se la stringa è compatibile con il formato di una targa europea.
     * Formato: Due lettere, seguite da numeri e due lettere finali (es. AB123CD).
     */
    private boolean isPlateAllowed(String plate) {
        // Espressione regolare per il formato di targa europea.
        String platePattern = "^[A-Z]{2}[0-9]{3}[A-Z]{2}$";
        Pattern pattern = Pattern.compile(platePattern, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(plate).matches();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Collegamento al ViewModel condiviso
        sharedViewModel = new ViewModelProvider(getActivity()).get(SharedViewModel.class);
        List<Targa> plateList = sharedViewModel.getPlateList(); // Ottiene la lista condivisa delle targhe

        // Binding del layout
        binding = FragmentRegistraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Collegamento al layout dinamico
        lista = binding.lista;

        // Inizializzazione della lista
        for (Targa targa : plateList) {
            if (isPlateAllowed(targa.getNumeroTarga())) {
                aggiungiElementoAllaVista(targa); // Aggiungi solo targhe compatibili
            }
        }

        return root;
    }

    /**
     * Aggiunge un elemento (targa) dinamicamente alla vista.
     */
    private void aggiungiElementoAllaVista(Targa targa) {
        // Crea un TextView per visualizzare la targa e altre informazioni
        TextView txt = new TextView(getContext());
        txt.setText("Targa: " + targa.getNumeroTarga() +
                ", Data/Ora: " + targa.getDataOraFormattata() +
                ", Probabilità: " + targa.getProbabilita());
        txt.setTextSize(18); // Imposta una dimensione del testo
        txt.setPadding(16, 16, 16, 16); // Aggiunge padding
        lista.addView(txt); // Aggiunge il TextView alla lista dinamica
    }

    /**
     * Metodo per aggiungere una targa alla lista e sincronizzarla con il ViewModel.
     */
    public void aggiungiTarga(String plate) {
        // Verifica che la targa sia valida
        if (isPlateAllowed(plate)) {
            // Controlla se la targa esiste già nella lista
            for (Targa targa : sharedViewModel.getPlateList()) {
                if (targa.getNumeroTarga().equals(plate)) {
                    // La targa esiste già, non aggiungere duplicati
                    return;
                }
            }

            // Genera il timestamp corrente in millisecondi
            long timestampCorrente = System.currentTimeMillis();

            // Crea un nuovo oggetto Targa con una probabilità predefinita
            Targa nuovaTarga = new Targa(plate, timestampCorrente, 0.95f);

            // Aggiungi la targa alla lista condivisa
            sharedViewModel.getPlateList().add(nuovaTarga);

            // Aggiorna la vista dinamica
            aggiungiElementoAllaVista(nuovaTarga);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
