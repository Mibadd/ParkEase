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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

    // --- UBAH: Tambahkan variabel untuk menyimpan nama & alamat ---
    private String locationName;
    private String address;
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

        // --- UBAH: Logika tombol konfirmasi diubah untuk membuka PaymentActivity ---
        btnConfirm.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            int currentItem = viewPager.getCurrentItem();
            if (viewPagerAdapter.getItemCount() > 0) {
                ParkingAreaFragment currentFragment = (ParkingAreaFragment) viewPagerAdapter.getFragment(currentItem);

                if (currentFragment == null) {
                    Toast.makeText(this, "Terjadi kesalahan, silakan pilih area lagi.", Toast.LENGTH_SHORT).show();
                    return;
                }

                ParkingSlot selectedSlot = currentFragment.getSelectedSlot();
                ParkingArea selectedArea = currentFragment.getParkingArea();

                if (selectedSlot != null && selectedArea != null) {
                    // Membuat intent untuk membuka PaymentActivity
                    Intent intent = new Intent(SelectSlotActivity.this, PaymentActivity.class);

                    // Menyisipkan semua data yang dibutuhkan oleh PaymentActivity
                    intent.putExtra("locationName", this.locationName);
                    intent.putExtra("address", this.address);
                    intent.putExtra("areaName", selectedArea.getArea_name());
                    intent.putExtra("slotNumber", selectedSlot.getSlot_number());
                    intent.putExtra("pricePerHour", selectedArea.getPrice_per_hour());
                    intent.putExtra("floorLevel", selectedArea.getFloor_level());
                    intent.putExtra("documentId", documentId);
                    intent.putExtra("userId", currentUser.getUid());

                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Pilih slot terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(viewPagerAdapter.getPageTitle(position))
        ).attach();
    }

    private int getIntValue(Object value) {
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        }
        return 0;
    }

    private void loadParkingSlots() {
        if (documentId == null || documentId.isEmpty()) {
            Toast.makeText(this, "ID Dokumen tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("parking_locations")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // --- BARU: Ambil nama dan alamat dari Firestore ---
                        this.locationName = documentSnapshot.getString("name");
                        this.address = documentSnapshot.getString("address");

                        tvSlotTitle.setText("Pilih Slot Parkir di " + this.locationName);

                        List<Map<String, Object>> areasData = (List<Map<String, Object>>) documentSnapshot.get("parking_areas");
                        if (areasData != null) {
                            viewPagerAdapter.clear();
                            for (Map<String, Object> areaMap : areasData) {
                                String areaName = (String) areaMap.get("area_name");
                                String vehicleType = (String) areaMap.get("vehicle_type");
                                int totalSpots = getIntValue(areaMap.get("total_spots"));
                                int occupiedSpots = getIntValue(areaMap.get("occupied_spots_count"));
                                int availableSpots = getIntValue(areaMap.get("available_spots_count"));
                                int price = getIntValue(areaMap.get("price_per_hour"));
                                int floor = getIntValue(areaMap.get("floor_level"));

                                List<Map<String, Object>> slotsMap = (List<Map<String, Object>>) areaMap.get("slots");
                                List<ParkingSlot> slotList = new ArrayList<>();
                                if (slotsMap != null) {
                                    for (Map<String, Object> slotMap : slotsMap) {
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