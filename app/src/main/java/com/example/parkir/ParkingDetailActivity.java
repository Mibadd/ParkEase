package com.example.parkir;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkir.adapter.ParkingAreaAdapter;
import com.example.parkir.model.ParkingArea;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ParkingDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String documentId;
    private GeoPoint locationCoordinates;

    private ImageView ivLocationImage;
    private TextView tvLocationName, tvLocationAddress;
    private LinearLayout layoutPriceInfo;
    private RecyclerView rvParkingAreas;
    private Button btnSelectSlot, btnViewMap;
    private ParkingAreaAdapter parkingAreaAdapter;
    private List<ParkingArea> parkingAreaList = new ArrayList<>();
    private CollapsingToolbarLayout collapsingToolbar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_detail);

        documentId = getIntent().getStringExtra("DOCUMENT_ID");
        if (documentId == null) {
            Toast.makeText(this, "ID Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupToolbar();

        loadParkingDetails();

        btnSelectSlot.setOnClickListener(v -> {
            Intent intent = new Intent(ParkingDetailActivity.this, SelectSlotActivity.class);
            intent.putExtra("documentId", documentId);
            startActivity(intent);
        });

        btnViewMap.setOnClickListener(v -> openMap());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        ivLocationImage = findViewById(R.id.ivLocationImage);
        tvLocationName = findViewById(R.id.tvLocationName);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        layoutPriceInfo = findViewById(R.id.layoutPriceInfo);
        rvParkingAreas = findViewById(R.id.rvParkingAreas);
        btnSelectSlot = findViewById(R.id.btnSelectSlot);
        btnViewMap = findViewById(R.id.btnViewMap);

        rvParkingAreas.setLayoutManager(new LinearLayoutManager(this));
        parkingAreaAdapter = new ParkingAreaAdapter(parkingAreaList);
        rvParkingAreas.setAdapter(parkingAreaAdapter);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(" ");
        }
    }

    private void loadParkingDetails() {
        db.collection("parking_locations").document(documentId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        populateUi(documentSnapshot);
                    } else {
                        Toast.makeText(this, "Detail lokasi tidak ditemukan", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void populateUi(DocumentSnapshot doc) {
        String name = doc.getString("name");
        String address = doc.getString("address");
        locationCoordinates = doc.getGeoPoint("coordinates");

        collapsingToolbar.setTitle(name);
        tvLocationName.setText(name);
        tvLocationAddress.setText(address);

        List<Map<String, Object>> areasData = (List<Map<String, Object>>) doc.get("parking_areas");
        if (areasData != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<ParkingArea>>() {}.getType();
            parkingAreaList.clear();
            parkingAreaList.addAll(gson.fromJson(gson.toJson(areasData), type));
            parkingAreaAdapter.notifyDataSetChanged();

            // Populate price info
            populatePriceInfo(parkingAreaList);
        }
    }

    private void populatePriceInfo(List<ParkingArea> areas) {
        layoutPriceInfo.removeAllViews();
        Locale localeID = new Locale("in", "ID");
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(localeID);

        for (ParkingArea area : areas) {
            View priceView = LayoutInflater.from(this).inflate(R.layout.item_price_info, layoutPriceInfo, false);
            TextView tvVehicleType = priceView.findViewById(R.id.tvVehicleType);
            TextView tvPrice = priceView.findViewById(R.id.tvPrice);

            String vehicleType = area.getVehicle_type();
            if ("car".equalsIgnoreCase(vehicleType)) {
                tvVehicleType.setText("Mobil");
            } else if ("motorcycle".equalsIgnoreCase(vehicleType)) {
                tvVehicleType.setText("Motor");
            } else {
                tvVehicleType.setText(vehicleType);
            }

            String formattedPrice = currencyFormat.format(area.getPrice_per_hour()) + "/jam";
            tvPrice.setText(formattedPrice);

            layoutPriceInfo.addView(priceView);
        }
    }

    private void openMap() {
        if (locationCoordinates != null) {
            String uri = String.format(Locale.ENGLISH, "geo:%f,%f?q=%f,%f(%s)",
                    locationCoordinates.getLatitude(), locationCoordinates.getLongitude(),
                    locationCoordinates.getLatitude(), locationCoordinates.getLongitude(),
                    tvLocationName.getText().toString());
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        } else {
            Toast.makeText(this, "Koordinat lokasi tidak tersedia.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}