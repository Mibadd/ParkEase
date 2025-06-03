package com.example.parkir;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkir.adapter.HistoryAdapter;
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

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<Booking> bookingList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;
    private TextView tvEmptyHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history); // Pastikan layout Anda memiliki RecyclerView, ProgressBar, dan TextView

        recyclerView = findViewById(R.id.historyRecyclerView); // Ganti dengan ID Anda
        progressBar = findViewById(R.id.progressBarHistory); // Ganti dengan ID Anda
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory); // Ganti dengan ID Anda

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        adapter = new HistoryAdapter(bookingList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        if (currentUser == null) {
            Toast.makeText(this, "Silakan login untuk melihat riwayat", Toast.LENGTH_SHORT).show();
            tvEmptyHistory.setText("Silakan login untuk melihat riwayat.");
            tvEmptyHistory.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmptyHistory.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        db.collection("bookings")
                .whereEqualTo("userId", currentUser.getUid())
                .orderBy("bookingTime", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        bookingList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Booking booking = document.toObject(Booking.class);
                            booking.setDocumentId(document.getId());
                            bookingList.add(booking);
                        }
                        adapter.notifyDataSetChanged();

                        if (bookingList.isEmpty()) {
                            tvEmptyHistory.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            tvEmptyHistory.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // INI BAGIAN PENTING UNTUK DIAGNOSIS
                        Log.e("HistoryActivity", "Error loading history", task.getException());
                        Toast.makeText(HistoryActivity.this, "Gagal memuat riwayat. Periksa Logcat untuk detail.", Toast.LENGTH_LONG).show();
                        tvEmptyHistory.setText("Gagal memuat riwayat.");
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                    }
                });
    }

    @Override
    public void onEndParkingClick(Booking booking) {
        final DocumentReference locationRef = db.collection("parking_locations").document(booking.getLocationId());
        final DocumentReference bookingRef = db.collection("bookings").document(booking.getDocumentId());

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. Baca data lokasi parkir
            List<Map<String, Object>> areasFromDB = (List<Map<String, Object>>) transaction.get(locationRef).get("parking_areas");
            if (areasFromDB == null) throw new RuntimeException("Area parkir tidak ditemukan.");

            // 2. Cari area dan slot, lalu kembalikan statusnya
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
                                slotMap.put("occupied_by_user_id", ""); // Kosongkan user ID
                                slotFound = true;
                                break;
                            }
                        }
                    }
                }
                if (slotFound) break;
            }

            if (!slotFound) throw new RuntimeException("Slot yang ingin dikosongkan tidak ditemukan.");

            // 3. Update data lokasi
            transaction.update(locationRef, "parking_areas", areasFromDB);

            // 4. Hapus data dari koleksi booking
            transaction.delete(bookingRef);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Sesi parkir selesai.", Toast.LENGTH_SHORT).show();
            // Muat ulang data riwayat untuk memperbarui UI
            loadHistory();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal menyelesaikan sesi: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}