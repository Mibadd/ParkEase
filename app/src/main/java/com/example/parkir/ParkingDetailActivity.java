package com.example.parkir;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.adapter.ParkingSlotAdapter;
import com.example.parkir.model.ParkingArea;
import com.example.parkir.model.ParkingLocation;
import com.example.parkir.model.ParkingSlot;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ParkingDetailActivity extends AppCompatActivity {

    private static final String TAG = "ParkingDetailActivity";

    private Toolbar toolbar;
    private TextView tvLocationName, tvSelectedAreaInfo;
    private RadioGroup rgVehicleType;
    private Spinner spinnerParkingArea;
    private RecyclerView rvParkingSlots;
    private Button btnConfirmParking;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private String locationId, locationName;
    private ParkingLocation currentParkingLocation;
    private List<ParkingArea> allParkingAreasForLocation;
    private List<ParkingArea> currentlyFilteredParkingAreas;
    private ArrayAdapter<String> parkingAreaSpinnerAdapter;
    private ParkingSlotAdapter parkingSlotAdapter;
    private List<ParkingSlot> slotsForSelectedArea;

    private ParkingArea currentlySelectedParkingAreaObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_detail);

        locationId = getIntent().getStringExtra("locationId");
        locationName = getIntent().getStringExtra("locationName");

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        allParkingAreasForLocation = new ArrayList<>();
        currentlyFilteredParkingAreas = new ArrayList<>();
        slotsForSelectedArea = new ArrayList<>();

        initViews();
        setupToolbar();

        if (locationId == null || locationId.isEmpty()) {
            Toast.makeText(this, "ID Lokasi tidak valid.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (currentUser == null) {
            Toast.makeText(this, "Harap login terlebih dahulu.", Toast.LENGTH_LONG).show();
            // Pertimbangkan untuk mengarahkan ke halaman login
            finish();
            return;
        }

        tvLocationName.setText(locationName != null ? locationName : "Detail Parkir");
        setupRecyclerView();
        setupListeners();
        loadParkingLocationDetails();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarParkingDetail);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvSelectedAreaInfo = findViewById(R.id.tvSelectedAreaInfo);
        rgVehicleType = findViewById(R.id.rgVehicleType);
        spinnerParkingArea = findViewById(R.id.spinnerParkingArea);
        rvParkingSlots = findViewById(R.id.rvParkingSlots);
        btnConfirmParking = findViewById(R.id.btnConfirmParking);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(locationName != null ? locationName : "Pilih Slot Parkir");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        int numColumns = 4; // Jumlah slot per baris, sesuaikan jika perlu
        rvParkingSlots.setLayoutManager(new GridLayoutManager(this, numColumns));
        parkingSlotAdapter = new ParkingSlotAdapter(this, slotsForSelectedArea, currentUser.getUid(), (slot, position) -> {
            parkingSlotAdapter.setSelectedPosition(position);
            btnConfirmParking.setEnabled(true);
            btnConfirmParking.setText("PARKIR DI " + slot.getSlot_number());
        });
        rvParkingSlots.setAdapter(parkingSlotAdapter);
    }

    private void setupListeners() {
        rgVehicleType.setOnCheckedChangeListener((group, checkedId) -> {
            filterAndDisplayParkingAreas();
            resetSlotSelectionAndButton();
        });

        spinnerParkingArea.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!currentlyFilteredParkingAreas.isEmpty() && position < currentlyFilteredParkingAreas.size()) {
                    currentlySelectedParkingAreaObject = currentlyFilteredParkingAreas.get(position);
                    Log.d(TAG, "Area dipilih: " + currentlySelectedParkingAreaObject.getArea_name()); // LOG

                    if (currentlySelectedParkingAreaObject.getSlots() != null) {
                        Log.d(TAG, "Jumlah slot di area ini: " + currentlySelectedParkingAreaObject.getSlots().size()); // LOG
                        slotsForSelectedArea.clear(); // Pindahkan clear ke sini agar lebih aman
                        slotsForSelectedArea.addAll(currentlySelectedParkingAreaObject.getSlots());
                    } else {
                        Log.d(TAG, "Array 'slots' untuk area " + currentlySelectedParkingAreaObject.getArea_name() + " adalah null."); // LOG
                        slotsForSelectedArea.clear();
                    }
                    parkingSlotAdapter.updateSlots(slotsForSelectedArea); // Panggil updateSlots
                    Log.d(TAG, "Adapter getItemCount setelah update: " + parkingSlotAdapter.getItemCount()); // LOG
                    updateSelectedAreaSummaryInfo();
                } else {
                    currentlySelectedParkingAreaObject = null;
                    slotsForSelectedArea.clear();
                    parkingSlotAdapter.notifyDataSetChanged();
                    tvSelectedAreaInfo.setText("Pilih area parkir.");
                }
                resetSlotSelectionAndButton();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                currentlySelectedParkingAreaObject = null;
                slotsForSelectedArea.clear();
                parkingSlotAdapter.notifyDataSetChanged();
                tvSelectedAreaInfo.setText("Pilih area parkir.");
                resetSlotSelectionAndButton();
            }
        });

        btnConfirmParking.setOnClickListener(v -> handleParkingConfirmation());
    }

    private void resetSlotSelectionAndButton() {
        parkingSlotAdapter.setSelectedPosition(-1); // Hapus seleksi slot
        btnConfirmParking.setEnabled(false);
        btnConfirmParking.setText("PILIH SLOT DAHULU");
    }

    private void loadParkingLocationDetails() {
        DocumentReference locationRef = db.collection("parking_locations").document(locationId);
        locationRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                currentParkingLocation = documentSnapshot.toObject(ParkingLocation.class);
                if (currentParkingLocation != null) {
                    currentParkingLocation.setId(documentSnapshot.getId()); // Simpan ID dokumen
                    allParkingAreasForLocation.clear();
                    if (currentParkingLocation.getParking_areas() != null) {
                        allParkingAreasForLocation.addAll(currentParkingLocation.getParking_areas());
                    }
                    filterAndDisplayParkingAreas(); // Filter awal berdasarkan jenis kendaraan default
                } else {
                    Toast.makeText(this, "Format data lokasi tidak sesuai.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Lokasi parkir tidak ditemukan.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Gagal memuat detail lokasi: ", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void filterAndDisplayParkingAreas() {
        if (allParkingAreasForLocation.isEmpty()) {
            spinnerParkingArea.setAdapter(null);
            slotsForSelectedArea.clear();
            parkingSlotAdapter.notifyDataSetChanged();
            tvSelectedAreaInfo.setText("Tidak ada area parkir di lokasi ini.");
            return;
        }

        String selectedVehicle = rgVehicleType.getCheckedRadioButtonId() == R.id.rbCar ? "car" : "motorcycle";
        currentlyFilteredParkingAreas.clear();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            currentlyFilteredParkingAreas.addAll(
                    allParkingAreasForLocation.stream()
                            .filter(area -> selectedVehicle.equalsIgnoreCase(area.getVehicle_type()))
                            .collect(Collectors.toList())
            );
        } else {
            for (ParkingArea area : allParkingAreasForLocation) {
                if (selectedVehicle.equalsIgnoreCase(area.getVehicle_type())) {
                    currentlyFilteredParkingAreas.add(area);
                }
            }
        }

        List<String> areaNames = new ArrayList<>();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            areaNames = currentlyFilteredParkingAreas.stream().map(ParkingArea::getArea_name).collect(Collectors.toList());
        } else {
            for(ParkingArea pa : currentlyFilteredParkingAreas) {
                areaNames.add(pa.getArea_name());
            }
        }


        parkingAreaSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, areaNames);
        parkingAreaSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerParkingArea.setAdapter(parkingAreaSpinnerAdapter);

        if (currentlyFilteredParkingAreas.isEmpty()) {
            slotsForSelectedArea.clear();
            parkingSlotAdapter.notifyDataSetChanged();
            tvSelectedAreaInfo.setText("Tidak ada area untuk " + (selectedVehicle.equals("car")? "mobil." : "motor."));
        }
        // Pemilihan item pertama di spinner akan otomatis memanggil onItemSelected listener
    }

    private void displaySlotsForSelectedArea() {
        if (currentlySelectedParkingAreaObject != null && currentlySelectedParkingAreaObject.getSlots() != null) {
            slotsForSelectedArea.clear();
            slotsForSelectedArea.addAll(currentlySelectedParkingAreaObject.getSlots());
        } else {
            slotsForSelectedArea.clear();
        }
        parkingSlotAdapter.updateSlots(slotsForSelectedArea); // Gunakan method updateSlots di adapter
    }

    private void updateSelectedAreaSummaryInfo() {
        if (currentlySelectedParkingAreaObject == null) {
            tvSelectedAreaInfo.setText("Pilih area.");
            return;
        }
        int occupiedCount = currentlySelectedParkingAreaObject.getOccupied_spots_count();
        int availableCount = currentlySelectedParkingAreaObject.getAvailable_spots_count();
        tvSelectedAreaInfo.setText(String.format(Locale.getDefault(),
                "%s: %d kosong / %d terisi (Total: %d)",
                currentlySelectedParkingAreaObject.getArea_name(),
                availableCount,
                occupiedCount,
                currentlySelectedParkingAreaObject.getTotal_spots()));
    }

    private void handleParkingConfirmation() {
        ParkingSlot selectedSlot = parkingSlotAdapter.getSelectedSlot();
        if (selectedSlot == null || currentlySelectedParkingAreaObject == null) {
            Toast.makeText(this, "Harap pilih slot parkir terlebih dahulu.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!"empty".equalsIgnoreCase(selectedSlot.getStatus())) {
            Toast.makeText(this, "Slot ini sudah terisi atau tidak valid.", Toast.LENGTH_SHORT).show();
            return; // Seharusnya tidak terjadi karena validasi di adapter
        }

        btnConfirmParking.setEnabled(false);
        btnConfirmParking.setText("MEMPROSES...");

        DocumentReference locationDocRef = db.collection("parking_locations").document(locationId);

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            DocumentSnapshot snapshot = transaction.get(locationDocRef);
            ParkingLocation freshLocation = snapshot.toObject(ParkingLocation.class);

            if (freshLocation == null || freshLocation.getParking_areas() == null) {
                throw new FirebaseFirestoreException("Data lokasi tidak ditemukan atau rusak.", FirebaseFirestoreException.Code.ABORTED);
            }

            // Cari ParkingArea dan ParkingSlot yang sesuai di data terbaru dari Firestore
            int targetAreaIndex = -1;
            int targetSlotIndex = -1;

            for (int i = 0; i < freshLocation.getParking_areas().size(); i++) {
                ParkingArea area = freshLocation.getParking_areas().get(i);
                if (area.getArea_name().equals(currentlySelectedParkingAreaObject.getArea_name()) &&
                        area.getVehicle_type().equals(currentlySelectedParkingAreaObject.getVehicle_type())) {
                    targetAreaIndex = i;
                    if (area.getSlots() != null) {
                        for (int j = 0; j < area.getSlots().size(); j++) {
                            if (area.getSlots().get(j).getSlot_number().equals(selectedSlot.getSlot_number())) {
                                targetSlotIndex = j;
                                break;
                            }
                        }
                    }
                    break;
                }
            }

            if (targetAreaIndex == -1 || targetSlotIndex == -1) {
                throw new FirebaseFirestoreException("Area atau slot tidak ditemukan di data terbaru.", FirebaseFirestoreException.Code.ABORTED);
            }

            ParkingSlot slotToUpdate = freshLocation.getParking_areas().get(targetAreaIndex).getSlots().get(targetSlotIndex);

            if (!"empty".equalsIgnoreCase(slotToUpdate.getStatus())) {
                throw new FirebaseFirestoreException("Slot sudah terisi oleh pengguna lain!", FirebaseFirestoreException.Code.ABORTED);
            }

            // --- Logika untuk memastikan satu pengguna hanya bisa parkir di satu tempat di lokasi ini ---
            // (Opsional, bisa ditambahkan jika diperlukan)
            // Iterasi semua slot di freshLocation, jika currentUser.getUid() sudah ada di occupied_by_user_id lain,
            // maka throw exception atau minta pengguna membatalkan parkir sebelumnya.
            // Untuk saat ini, kita lewati logika ini demi kesederhanaan.

            slotToUpdate.setStatus("occupied");
            slotToUpdate.setOccupied_by_user_id(currentUser.getUid());
            // Update field last_updated juga
            freshLocation.setLast_updated(null); // Agar server timestamp yang mengisinya

            // Update seluruh array parking_areas
            transaction.update(locationDocRef, "parking_areas", freshLocation.getParking_areas());
            transaction.update(locationDocRef, "last_updated", FieldValue.serverTimestamp());


            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(ParkingDetailActivity.this, "Anda berhasil parkir di slot " + selectedSlot.getSlot_number(), Toast.LENGTH_LONG).show();

            // Update UI secara lokal untuk responsivitas
            selectedSlot.setStatus("occupied");
            selectedSlot.setOccupied_by_user_id(currentUser.getUid());
            parkingSlotAdapter.notifyItemChanged(parkingSlotAdapter.getSelectedPosition()); // Update slot yang dipilih
            resetSlotSelectionAndButton();
            updateSelectedAreaSummaryInfo(); // Update info ketersediaan

            // Mungkin delay lalu finish() atau navigasi ke halaman "Tiket Parkir"
            // finish();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Gagal konfirmasi parkir: ", e);
            Toast.makeText(ParkingDetailActivity.this, "Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnConfirmParking.setEnabled(true);
            btnConfirmParking.setText("PARKIR DI " + (selectedSlot != null ? selectedSlot.getSlot_number() : "SLOT"));
            // Muat ulang data untuk konsistensi jika transaksi gagal
            loadParkingLocationDetails();
        });
    }
}