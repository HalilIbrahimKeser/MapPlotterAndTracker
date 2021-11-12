package com.halil.mapplotterandtracker.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.R;

import java.util.ArrayList;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolder> {
    ArrayList<Trip> data = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_item, null));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(data.get(position).mFromAddress);
        StringBuilder stringBuilder = new StringBuilder();

        holder.trip.setText(data.get(position).age.toString());
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, trip;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvAddress);
            trip = itemView.findViewById(R.id.tvTrip);
        }
    }
}
