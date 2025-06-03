package com.example.parkir.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.R;
import com.example.parkir.model.ParkingLocation;
import java.util.List;

public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private List<ParkingLocation> parkingLocations;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ParkingLocation location);
    }

    public ParkingAdapter(List<ParkingLocation> parkingLocations, OnItemClickListener listener) {
        this.parkingLocations = parkingLocations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Gunakan layout item_recommendation yang baru
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recommendation, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingLocation location = parkingLocations.get(position);
        holder.bind(location, listener);
    }

    @Override
    public int getItemCount() {
        return parkingLocations.size();
    }

    // Metode untuk memperbarui data saat ada pencarian
    public void updateData(List<ParkingLocation> newLocations) {
        this.parkingLocations = newLocations;
        notifyDataSetChanged();
    }

    // Sesuaikan ViewHolder dengan ID dari item_recommendation.xml
    static class ParkingViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocationName, tvLocationAddress;
        // ImageView ivLocationImage; // Uncomment jika ingin memuat gambar

        ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvLocationAddress = itemView.findViewById(R.id.tvLocationAddress);
            // ivLocationImage = itemView.findViewById(R.id.ivLocationImage);
        }

        void bind(final ParkingLocation location, final OnItemClickListener listener) {
            tvLocationName.setText(location.getName());
            tvLocationAddress.setText(location.getAddress());

            // Tambahkan logika untuk memuat gambar dengan Glide/Picasso di sini jika perlu
            // Glide.with(itemView.getContext()).load(location.getImageUrl()).into(ivLocationImage);

            itemView.setOnClickListener(v -> listener.onItemClick(location));
        }
    }
}