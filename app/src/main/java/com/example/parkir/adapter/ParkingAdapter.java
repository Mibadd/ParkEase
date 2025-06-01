package com.example.parkir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.R;
import com.example.parkir.model.ParkingArea;
import com.example.parkir.model.ParkingLocation;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import com.example.parkir.model.ParkingSlot;
public class ParkingAdapter extends RecyclerView.Adapter<ParkingAdapter.ParkingViewHolder> {

    private Context context;
    private List<ParkingLocation> parkingLocations;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ParkingLocation location);
    }

    public ParkingAdapter(Context context, List<ParkingLocation> parkingLocations, OnItemClickListener listener) {
        this.context = context;
        this.parkingLocations = parkingLocations;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParkingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_spot, parent, false);
        return new ParkingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkingViewHolder holder, int position) {
        ParkingLocation location = parkingLocations.get(position);
        holder.bind(location, listener);
    }

    @Override
    public int getItemCount() {
        return parkingLocations == null ? 0 : parkingLocations.size();
    }

    public void updateData(List<ParkingLocation> newLocations) {
        this.parkingLocations.clear();
        if (newLocations != null) {
            this.parkingLocations.addAll(newLocations);
        }
        notifyDataSetChanged();
    }

    class ParkingViewHolder extends RecyclerView.ViewHolder {
        ImageView parkingIcon;
        TextView nameText;
        TextView addressText;
        LinearLayout parkingAreasContainer;
        ImageView arrowIcon;

        ParkingViewHolder(@NonNull View itemView) {
            super(itemView);
            parkingIcon = itemView.findViewById(R.id.parkingIcon);
            nameText = itemView.findViewById(R.id.nameText);
            addressText = itemView.findViewById(R.id.addressText);
            parkingAreasContainer = itemView.findViewById(R.id.parkingAreasContainer);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }

        void bind(final ParkingLocation location, final OnItemClickListener listener) {
            nameText.setText(location.getName());
            addressText.setText(location.getAddress());
            parkingAreasContainer.removeAllViews();

            if (location.getParking_areas() != null && !location.getParking_areas().isEmpty()) {
                for (ParkingArea area : location.getParking_areas()) {
                    // ... (kode untuk inflate areaDetailLayout tetap sama)
                    LinearLayout areaDetailLayout = new LinearLayout(context);
                    areaDetailLayout.setOrientation(LinearLayout.HORIZONTAL);
                    areaDetailLayout.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    areaDetailLayout.setPadding(0, dpToPx(2), 0, dpToPx(2));

                    TextView availabilityInfo = new TextView(context);
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    availabilityInfo.setLayoutParams(textParams);


                    // Hitung spot tersedia dari list slots
                    int availableSpots = 0;
                    int totalSpotsInArea = area.getTotal_spots();
                    int occupiedSpotsInArea = area.getOccupied_spots_count(); // Gunakan method helper baru
                    availableSpots = totalSpotsInArea - occupiedSpotsInArea;

                    String vehicleEmoji = "car".equalsIgnoreCase(area.getVehicle_type()) ? "🚗" : "🛵";
                    String details = String.format(Locale.getDefault(), "%s %s: %d/%d kosong",
                            vehicleEmoji, area.getArea_name(), availableSpots, totalSpotsInArea);
                    availabilityInfo.setText(details);
                    availabilityInfo.setTextSize(13);
                    availabilityInfo.setTextColor(ContextCompat.getColor(context,
                            availableSpots > 0 ? R.color.dark_gray : R.color.red));

                    TextView priceInfo = new TextView(context);
                    priceInfo.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    currencyFormat.setMaximumFractionDigits(0);
                    priceInfo.setText(currencyFormat.format(area.getPrice_per_hour()) + "/jam");
                    priceInfo.setTextSize(13);
                    priceInfo.setTextColor(ContextCompat.getColor(context, R.color.purple_500));
                    priceInfo.setPadding(dpToPx(4),0,0,0);

                    areaDetailLayout.addView(availabilityInfo);
                    areaDetailLayout.addView(priceInfo);
                    parkingAreasContainer.addView(areaDetailLayout);
                }
            } else {
                TextView noData = new TextView(context);
                noData.setText("Info ketersediaan parkir tidak tersedia.");
                noData.setTextSize(13);
                parkingAreasContainer.addView(noData);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(location));
        }
        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }
    }
}