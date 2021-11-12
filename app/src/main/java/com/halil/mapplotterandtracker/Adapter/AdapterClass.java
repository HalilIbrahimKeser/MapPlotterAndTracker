package com.halil.mapplotterandtracker.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.halil.mapplotterandtracker.Entities.Trip;
import com.halil.mapplotterandtracker.R;
import java.util.List;

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.ViewHolder> {

    private List<Trip> trips;
    private Context context;
    private ItemClickListener itemClickListener;

    public AdapterClass(Context context, List<Trip> trips){
        this.trips = trips;
        this.context = context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(this.context);
        View view = layoutInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTripidLine.setText(String.valueOf(trips.get(position).mTripId));
        holder.tvTripidLine.setText(String.valueOf(trips.get(position).mTripId));

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("From adress: " + trips.get(position).mFromAddress + "\nTo adress: " + trips.get(position).mToAddress +
                "\nLength: " + trips.get(position).mLength + ", Points: " + trips.get(position).mNodes +
                "\nDuration: " + trips.get(position).mDuration + ", Elevation: " + trips.get(position).mElevation);
        holder.tvTrip.setText(stringBuilder);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvTripidLine;
        TextView tvTrip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripidLine = itemView.findViewById(R.id.tvTripidLine);
            tvTrip = itemView.findViewById(R.id.tvTrip);
            tvTripidLine.setOnClickListener(this::onClick);
            tvTrip.setOnClickListener(this::onClick);
        }

        @Override
        public void onClick(View view) {
            if(AdapterClass.this.itemClickListener != null){
                int pos = getAbsoluteAdapterPosition();
                itemClickListener.onItemClick(view, pos);
            }
        }
    }

    public void setClickListener(AdapterClass.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
}
