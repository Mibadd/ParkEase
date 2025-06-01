package com.example.parkir.adapter;

import android.content.Context;
import android.graphics.Color;
import android.util.Log; // Tambahkan Log untuk debugging di adapter jika perlu
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.R;
import com.example.parkir.model.ParkingSlot;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<ParkingSlotAdapter.SlotViewHolder> {

    private Context context;
    // Gunakan list internal yang berbeda dari yang di-pass di konstruktor untuk data utama
    private List<ParkingSlot> internalSlotList;
    private OnSlotClickListener listener;
    private int selectedPosition = -1;
    private String currentUserId;

    public interface OnSlotClickListener {
        void onSlotClick(ParkingSlot slot, int position);
    }

    public ParkingSlotAdapter(Context context, List<ParkingSlot> initialSlots, String currentUserId, OnSlotClickListener listener) {
        this.context = context;
        this.currentUserId = currentUserId;
        this.listener = listener;
        // Buat instance baru untuk list internal adapter
        this.internalSlotList = new ArrayList<>();
        if (initialSlots != null) {
            // Anda bisa memilih untuk mengisi initialSlots di sini,
            // atau biarkan kosong dan selalu mengandalkan updateSlots.
            // Untuk konsistensi, kita akan mengandalkan updateSlots.
            // this.internalSlotList.addAll(initialSlots);
        }
    }

    @NonNull
    @Override
    public SlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_parking_slot_visual, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlotViewHolder holder, int position) {
        // Gunakan internalSlotList
        ParkingSlot slot = internalSlotList.get(position);
        holder.bind(slot, position);
    }

    @Override
    public int getItemCount() {
        // Gunakan internalSlotList
        return internalSlotList == null ? 0 : internalSlotList.size();
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        if (previousSelected != -1 && previousSelected < internalSlotList.size()) { // Tambahkan pengecekan batas
            notifyItemChanged(previousSelected);
        }
        if (selectedPosition != -1 && selectedPosition < internalSlotList.size()) { // Tambahkan pengecekan batas
            notifyItemChanged(selectedPosition);
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public ParkingSlot getSelectedSlot() {
        // Gunakan internalSlotList
        if (selectedPosition != -1 && selectedPosition < internalSlotList.size()) {
            return internalSlotList.get(selectedPosition);
        }
        return null;
    }

    public void updateSlots(List<ParkingSlot> newSlots) {
        this.internalSlotList.clear(); // Hapus semua data lama dari list internal
        if (newSlots != null) {
            this.internalSlotList.addAll(newSlots); // Tambahkan semua data baru ke list internal
            Log.d("ParkingSlotAdapter", "updateSlots: internalSlotList size: " + this.internalSlotList.size());
        } else {
            Log.d("ParkingSlotAdapter", "updateSlots: newSlots is null.");
        }
        this.selectedPosition = -1;
        notifyDataSetChanged(); // Beritahu RecyclerView bahwa data telah berubah total
    }

    // ... (SlotViewHolder class tetap sama, pastikan menggunakan context dari itemView jika perlu)
    class SlotViewHolder extends RecyclerView.ViewHolder {
        CardView cardSlot;
        LinearLayout layoutSlot;
        TextView tvSlotNumber;
        TextView tvSlotStatus;

        SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            layoutSlot = itemView.findViewById(R.id.layoutSlot);
            tvSlotNumber = itemView.findViewById(R.id.tvSlotNumber);
            tvSlotStatus = itemView.findViewById(R.id.tvSlotStatus);
        }

        void bind(final ParkingSlot slot, final int position) {
            tvSlotNumber.setText(slot.getSlot_number());
            itemView.setEnabled(true);
            cardSlot.setAlpha(1.0f);

            if ("occupied".equalsIgnoreCase(slot.getStatus())) {
                layoutSlot.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.light_red)); // Gunakan itemView.getContext()
                tvSlotStatus.setText("TERISI");
                tvSlotNumber.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_gray));
                itemView.setEnabled(false);
                cardSlot.setAlpha(0.6f);

                if (currentUserId != null && currentUserId.equals(slot.getOccupied_by_user_id())) {
                    tvSlotStatus.setText("ANDA");
                    layoutSlot.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.gray));
                }
            } else { // "empty"
                layoutSlot.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.light_blue));
                tvSlotStatus.setText("KOSONG");
                tvSlotNumber.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.dark_gray));
            }

            if (selectedPosition == position) {
                layoutSlot.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.teal));
                tvSlotStatus.setText("DIPILIH");
                tvSlotNumber.setTextColor(Color.WHITE);
            }

            itemView.setOnClickListener(v -> {
                if ("empty".equalsIgnoreCase(slot.getStatus())) {
                    listener.onSlotClick(slot, position);
                }
            });
        }
    }
}