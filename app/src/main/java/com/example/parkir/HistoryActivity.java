package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.parkir.adapter.HistoryAdapter;
import com.example.parkir.databinding.ActivityHistoryBinding;
import com.example.parkir.model.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryItemInteractionListener {

    private ActivityHistoryBinding binding;
    private HistoryAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // --- Fungsionalitas Tombol Kembali ---
// --- Fungsionalitas Tombol Kembali ---
        binding.ivBack.setOnClickListener(v -> {
            // Membuat Intent untuk secara spesifik membuka HomeActivity
            Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);

            // Menambahkan flag untuk membersihkan semua activity di atas HomeActivity
            // Ini mencegah penumpukan activity yang tidak perlu
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            startActivity(intent);
            finish(); // Menutup HistoryActivity saat ini
        });

        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        // Pastikan Anda memiliki listener jika diperlukan, atau set null/this
        adapter = new HistoryAdapter(bookingList, this);
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login untuk melihat riwayat", Toast.LENGTH_SHORT).show();
            binding.tvEmptyHistory.setText("Silakan login untuk melihat riwayat.");
            binding.tvEmptyHistory.setVisibility(View.VISIBLE);
            return;
        }

        binding.progressBarHistory.setVisibility(View.VISIBLE);
        binding.tvEmptyHistory.setVisibility(View.GONE);
        binding.historyRecyclerView.setVisibility(View.GONE);

        db.collection("bookings")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    binding.progressBarHistory.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Booking booking = document.toObject(Booking.class);
                            booking.setDocumentId(document.getId());
                            bookingList.add(booking);
                        }
                        adapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            binding.tvEmptyHistory.setVisibility(View.VISIBLE);
                            binding.historyRecyclerView.setVisibility(View.GONE);
                        } else {
                            binding.tvEmptyHistory.setVisibility(View.GONE);
                            binding.historyRecyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.e("HistoryActivity", "Error loading history", task.getException());
                        Toast.makeText(HistoryActivity.this, "Gagal memuat riwayat.", Toast.LENGTH_LONG).show();
                        binding.tvEmptyHistory.setText("Gagal memuat riwayat.");
                        binding.tvEmptyHistory.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onEndParkingClick(Booking booking) {
        final DocumentReference locationRef = db.collection("parking_locations").document(booking.getLocationId());
        final DocumentReference bookingRef = db.collection("bookings").document(booking.getDocumentId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            List<Map<String, Object>> areasFromDB = (List<Map<String, Object>>) transaction.get(locationRef).get("parking_areas");
            if (areasFromDB == null) throw new RuntimeException("Area parkir tidak ditemukan.");

            boolean slotFound = false;
            for (Map<String, Object> areaMap : areasFromDB) {
                if (Objects.equals(areaMap.get("area_name"), booking.getAreaName())) {
                    long occupiedCount = (long) areaMap.get("occupied_spots_count");
                    long availableCount = (long) areaMap.get("available_spots_count");
                    areaMap.put("occupied_spots_count", occupiedCount - 1);
                    areaMap.put("available_spots_count", availableCount + 1);

                    List<Map<String, Object>> slotsFromDB = (List<Map<String, Object>>) areaMap.get("slots");
                    if (slotsFromDB != null) {
                        for (Map<String, Object> slotMap : slotsFromDB) {
                            if (Objects.equals(slotMap.get("slot_number"), booking.getSlotNumber())) {
                                slotMap.put("status", "empty");
                                slotMap.put("occupied_by_user_id", "");
                                slotFound = true;
                                break;
                            }
                        }
                    }
                }
                if (slotFound) break;
            }

            if (!slotFound) throw new RuntimeException("Slot yang ingin dikosongkan tidak ditemukan.");

            transaction.update(locationRef, "parking_areas", areasFromDB);
            transaction.delete(bookingRef);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Sesi parkir selesai.", Toast.LENGTH_SHORT).show();
            loadHistory();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal menyelesaikan sesi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}
