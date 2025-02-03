package com.example.infotarghe.ui.registra;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.infotarghe.R;
import com.example.infotarghe.Rilevazione;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RegistraAdapter extends RecyclerView.Adapter<RegistraAdapter.ViewHolder> {

    private final List<Rilevazione> rilevazioni;

    public RegistraAdapter(List<Rilevazione> rilevazioni) {
        this.rilevazioni = rilevazioni;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_rilevazione, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Rilevazione rilevazione = rilevazioni.get(position);

        // Formatta il timestamp in formato leggibile
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy  HH:mm:ss", Locale.getDefault());
        String formattedDate = dateFormat.format(rilevazione.getFirstTimestamp());

        // Imposta i dati nella ViewHolder
        holder.plateNumberTextView.setText(rilevazione.getPlateNumber());
        holder.countTextView.setText(String.valueOf(rilevazione.getCount()));
        holder.timestampTextView.setText(formattedDate);
    }


    @Override
    public int getItemCount() {
        return rilevazioni.size();
    }

    public void updateData(List<Rilevazione> newData) {
        rilevazioni.clear();
        rilevazioni.addAll(newData);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView plateNumberTextView;
        TextView countTextView;
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            plateNumberTextView = itemView.findViewById(R.id.plateNumberTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
            timestampTextView = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
