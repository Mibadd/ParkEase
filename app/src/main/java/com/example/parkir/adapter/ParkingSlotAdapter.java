package com.example.parkir.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.R;
import com.example.parkir.model.ParkingSlot;
import java.util.ArrayList;
import java.util.List;

public class ParkingSlotAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<ParkingSlot> internalSlotList;
    private final OnSlotClickListener listener;
    private final String vehicleType; // Tambahkan tipe kendaraan
    private int selectedPosition = -1;

    private static final int VIEW_TYPE_SLOT = 1;
    private static final int VIEW_TYPE_AISLE = 2;

    public interface OnSlotClickListener {
        void onSlotClick(ParkingSlot slot, int position);
    }

    public ParkingSlotAdapter(Context context, String vehicleType, OnSlotClickListener listener) {
        this.context = context;
        this.vehicleType = vehicleType; // Inisialisasi
        this.listener = listener;
        this.internalSlotList = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        if ("AISLE".equals(internalSlotList.get(position).getSlot_number())) {
            return VIEW_TYPE_AISLE;
        }
        return VIEW_TYPE_SLOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_AISLE) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_aisle, parent, false);
            return new AisleViewHolder(view);
        }
        View view = LayoutInflater.from(context).inflate(R.layout.item_select_slot, parent, false);
        return new SlotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_SLOT) {
            ParkingSlot slot = internalSlotList.get(position);
            ((SlotViewHolder) holder).bind(slot, position);
        }
    }

    @Override
    public int getItemCount() {
        return internalSlotList.size();
    }

    public void updateSlots(List<ParkingSlot> newSlots) {
        this.internalSlotList.clear();
        if (newSlots != null) {
            this.internalSlotList.addAll(newSlots);
        }
        this.selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public ParkingSlot getSelectedSlot() {
        if (selectedPosition != -1 && selectedPosition < internalSlotList.size()) {
            return internalSlotList.get(selectedPosition);
        }
        return null;
    }

    class SlotViewHolder extends RecyclerView.ViewHolder {
        CardView cardSlot;
        ImageView ivCar;
        TextView tvSlotNumber;

        SlotViewHolder(@NonNull View itemView) {
            super(itemView);
            cardSlot = itemView.findViewById(R.id.cardSlot);
            ivCar = itemView.findViewById(R.id.ivCar);
            tvSlotNumber = itemView.findViewById(R.id.tvSlotNumber);
        }

        void bind(final ParkingSlot slot, final int position) {
            ivCar.setVisibility(View.GONE);
            tvSlotNumber.setVisibility(View.GONE);

            // Logika untuk status terisi (occupied) tetap sama
            if ("occupied".equalsIgnoreCase(slot.getStatus())) {
                ivCar.setVisibility(View.VISIBLE);
                // Atur warna latar menjadi merah untuk slot terisi
                cardSlot.setCardBackgroundColor(ContextCompat.getColor(context, R.color.red));
                itemView.setEnabled(false);
                cardSlot.setAlpha(0.7f);
            } else {
                tvSlotNumber.setVisibility(View.VISIBLE);
                tvSlotNumber.setText(slot.getSlot_number());
                // Kembalikan warna latar default untuk slot kosong
                cardSlot.setCardBackgroundColor(Color.parseColor("#28303E"));
                itemView.setEnabled(true);
                cardSlot.setAlpha(1.0f);
            }

            // PERUBAHAN LOGIKA SELEKSI:
            // Hapus kode 'if (selectedPosition == position)' yang lama
            // Ganti dengan satu baris ini:
            itemView.setSelected(selectedPosition == position);

            itemView.setOnClickListener(v -> {
                if ("empty".equalsIgnoreCase(slot.getStatus())) {
                    listener.onSlotClick(slot, getAdapterPosition());
                }
            });
        }
    }


    class AisleViewHolder extends RecyclerView.ViewHolder {
        AisleViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}