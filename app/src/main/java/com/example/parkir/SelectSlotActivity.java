package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.parkir.adapter.ViewPagerAdapter;
import com.example.parkir.fragment.ParkingAreaFragment;
import com.example.parkir.model.ParkingArea;
import com.example.parkir.model.ParkingSlot;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Transaction;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SelectSlotActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private Button btnConfirm;
    private TextView tvSlotTitle;
    private ViewPagerAdapter viewPagerAdapter;
    private FirebaseFirestore db;
    private String locationName; // Tambahkan variabel ini
    private String documentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_slot);
        db = FirebaseFirestore.getInstance();
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        btnConfirm = findViewById(R.id.btnConfirmSlot);
        tvSlotTitle = findViewById(R.id.tvSlotTitle);

        documentId = getIntent().getStringExtra("documentId");

        setupViewPager();
        loadParkingSlots();

        btnConfirm.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                // Arahkan ke LoginActivity
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            int currentItem = viewPager.getCurrentItem();
            if (viewPagerAdapter.getItemCount() > 0) {
                ParkingAreaFragment currentFragment = (ParkingAreaFragment) viewPagerAdapter.getFragment(currentItem);
                ParkingSlot selectedSlot = currentFragment.getSelectedSlot();
                ParkingArea selectedArea = currentFragment.getParkingArea(); // Perlu method helper ini

                if (selectedSlot != null && selectedArea != null) {
                    bookParkingSlot(documentId, selectedArea, selectedSlot, currentUser.getUid());
                } else {
                    Toast.makeText(this, "Pilih slot terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void bookParkingSlot(String docId, ParkingArea area, ParkingSlot slot, String userId) {
        final DocumentReference locationRef = db.collection("parking_locations").document(docId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            // 1. Baca data terbaru dari dokumen
            List<Map<String, Object>> areasFromDB = (List<Map<String, Object>>) transaction.get(locationRef).get("parking_areas");
            if (areasFromDB == null) {
                throw new RuntimeException("Area parkir tidak ditemukan.");
            }

            // 2. Cari area dan slot yang sesuai
            boolean slotFound = false;
            for (Map<String, Object> areaMap : areasFromDB) {
                if (Objects.equals(areaMap.get("area_name"), area.getArea_name())) {
                    // Update counter area
                    long occupiedCount = (long) areaMap.get("occupied_spots_count");
                    long availableCount = (long) areaMap.get("available_spots_count");
                    areaMap.put("occupied_spots_count", occupiedCount + 1);
                    areaMap.put("available_spots_count", availableCount - 1);

                    List<Map<String, Object>> slotsFromDB = (List<Map<String, Object>>) areaMap.get("slots");
                    if (slotsFromDB != null) {
                        for (Map<String, Object> slotMap : slotsFromDB) {
                            if (Objects.equals(slotMap.get("slot_number"), slot.getSlot_number())) {
                                // 3. Update status slot
                                slotMap.put("status", "occupied");
                                slotMap.put("occupied_by_user_id", userId);
                                slotFound = true;
                                break;
                            }
                        }
                    }
                }
                if (slotFound) break;
            }

            if (!slotFound) {
                throw new RuntimeException("Slot tidak ditemukan atau sudah dipesan.");
            }

            // 4. Tulis kembali seluruh array 'parking_areas'
            transaction.update(locationRef, "parking_areas", areasFromDB);

            // 5. Tambahkan ke koleksi history pengguna (opsional, tapi sangat direkomendasikan)
            Map<String, Object> bookingData = Map.of(
                    "userId", userId,
                    "locationId", docId,
                    "locationName", this.locationName,
                    "areaName", area.getArea_name(),
                    "slotNumber", slot.getSlot_number(),
                    "bookingTime", new Date()
            );
            db.collection("bookings").add(bookingData);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Slot " + slot.getSlot_number() + " berhasil dipesan", Toast.LENGTH_SHORT).show();
            // Pindah ke halaman sukses
            Intent intent = new Intent(SelectSlotActivity.this, BookingSuccessActivity.class);
            intent.putExtra("locationName", this.locationName);
            intent.putExtra("areaName", area.getArea_name());
            intent.putExtra("slotNumber", slot.getSlot_number());
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Gagal memesan slot: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(viewPagerAdapter.getPageTitle(position))
        ).attach();
    }

    // Helper untuk konversi Long ke int dengan aman
    private int getIntValue(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0; // Nilai default jika tipe tidak dikenali atau null
    }

    private void loadParkingSlots() {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "ID Dokumen tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("parking_locations")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Simpan nama lokasi
                        this.locationName = documentSnapshot.getString("name");
                        String locationName = documentSnapshot.getString("name");
                        tvSlotTitle.setText("Pilih Slot Parkir di " + locationName);

                        List<Map<String, Object>> areasData = (List<Map<String, Object>>) documentSnapshot.get("parking_areas");
                        if (areasData != null) {
                            viewPagerAdapter.clear();

                            for (Map<String, Object> areaMap : areasData) {
                                String areaName = (String) areaMap.get("area_name");
                                String vehicleType = (String) areaMap.get("vehicle_type");

                                // Ambil semua field numerik dengan aman
                                int totalSpots = getIntValue(areaMap.get("total_spots"));
                                int occupiedSpots = getIntValue(areaMap.get("occupied_spots_count"));
                                int availableSpots = getIntValue(areaMap.get("available_spots_count"));
                                int price = getIntValue(areaMap.get("price_per_hour"));
                                int floor = getIntValue(areaMap.get("floor_level"));

                                List<Map<String, Object>> slotsMap = (List<Map<String, Object>>) areaMap.get("slots");

                                List<ParkingSlot> slotList = new ArrayList<>();
                                if(slotsMap != null) {
                                    for(Map<String, Object> slotMap : slotsMap){
                                        ParkingSlot slot = new ParkingSlot((String) slotMap.get("slot_number"), (String) slotMap.get("status"));
                                        slotList.add(slot);
                                    }
                                }

                                ParkingArea parkingArea = new ParkingArea(areaName, vehicleType, slotList, totalSpots, occupiedSpots, availableSpots, price, floor);
                                viewPagerAdapter.addFragment(ParkingAreaFragment.newInstance(parkingArea), areaName);
                            }
                            viewPagerAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}