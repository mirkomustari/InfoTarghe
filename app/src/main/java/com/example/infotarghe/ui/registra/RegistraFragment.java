package com.example.infotarghe.ui.registra;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infotarghe.R;
import com.example.infotarghe.Rilevazione;
import com.example.infotarghe.SharedViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RegistraFragment extends Fragment {

    private SharedViewModel sharedViewModel;
    private TextView summaryTextView;
    private RecyclerView recyclerView;
    private RegistraAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_registra, container, false);

        // Inizializza le view
        summaryTextView = root.findViewById(R.id.summaryTextView);
        recyclerView = root.findViewById(R.id.recyclerView);

        // Configura il RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RegistraAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ottieni il ViewModel
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Osserva la lista delle targhe valide
        sharedViewModel.getPlateRilevazioniLiveData().observe(getViewLifecycleOwner(), plateMap -> {
            if (plateMap != null) {
                // Converte la mappa in una lista e ordina per timestamp del primo rilevamento
                List<Rilevazione> sortedRilevazioni = new ArrayList<>(plateMap.values());
                sortedRilevazioni.sort(Comparator.comparingLong(Rilevazione::getFirstTimestamp));

                // Aggiorna il RecyclerView con i dati ordinati
                adapter.updateData(sortedRilevazioni);
            }
        });

        // Aggiorna il riepilogo
        updateSummary();
    }

    /**
     * Aggiorna il riepilogo con i dati totali.
     */
    private void updateSummary() {
        int totalOCR = sharedViewModel.getTotalValidPlates() + sharedViewModel.getTotalInvalidPlates();
        int totalValid = sharedViewModel.getTotalValidPlates();
        int totalInvalid = sharedViewModel.getTotalInvalidPlates();

        String summary = String.format(Locale.getDefault(),
                "Totale OCR: %d\nTarghe valide: %d\nTarghe non valide: %d",
                totalOCR, totalValid, totalInvalid);

        summaryTextView.setText(summary);
    }
}
