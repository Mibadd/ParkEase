package com.example.parkir.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.R;
import com.example.parkir.model.ParkingArea;
import java.util.List;

public class ParkingAreaAdapter extends RecyclerView.Adapter<ParkingAreaAdapter.ViewHolder> {

    private final List<ParkingArea> parkingAreaList;

    public ParkingAreaAdapter(List<ParkingArea> parkingAreaList) {
        this.parkingAreaList = parkingAreaList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_parking_area, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        ParkingArea area = parkingAreaList.get(position);
        holder.tvAreaName.setText(area.getArea_name());
        holder.tvFloorLevel.setText("Lantai " + area.getFloor_level());
        holder.tvAvailableSpots.setText(area.getAvailable_spots_count() + " Tersedia");
    }

    @Override
    public int getItemCount() {
        return parkingAreaList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAreaName, tvFloorLevel, tvAvailableSpots;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAreaName = itemView.findViewById(R.id.tvAreaName);
            tvFloorLevel = itemView.findViewById(R.id.tvFloorLevel);
            tvAvailableSpots = itemView.findViewById(R.id.tvAvailableSpots);
        }
    }
}