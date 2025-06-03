package com.example.parkir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkir.R;
import com.example.parkir.model.Booking; // Buat model ini
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final List<Booking> bookingList;
    private final OnHistoryItemInteractionListener listener;

    public interface OnHistoryItemInteractionListener {
        void onEndParkingClick(Booking booking);
    }

    public HistoryAdapter(List<Booking> bookingList, OnHistoryItemInteractionListener listener) {
        this.bookingList = bookingList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false); // Buat layout ini
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvLocation, tvSlot, tvTime;
        Button btnEndParking;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocation = itemView.findViewById(R.id.tvHistoryLocation);
            tvSlot = itemView.findViewById(R.id.tvHistorySlot);
            tvTime = itemView.findViewById(R.id.tvHistoryTime);
            btnEndParking = itemView.findViewById(R.id.btnEndParking);
        }

        void bind(final Booking booking) {
            tvLocation.setText(booking.getLocationName());
            tvSlot.setText("Slot " + booking.getAreaName() + " - " + booking.getSlotNumber());

            Timestamp bookingTimestamp = booking.getBookingTime();
            if (bookingTimestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
                tvTime.setText("Waktu: " + sdf.format(bookingTimestamp.toDate()));
            }

            btnEndParking.setOnClickListener(v -> listener.onEndParkingClick(booking));
        }
    }
}