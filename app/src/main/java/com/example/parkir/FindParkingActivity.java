package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.adapter.ParkingAdapter;
import com.example.parkir.model.ParkingLocation;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FindParkingActivity extends AppCompatActivity implements ParkingAdapter.OnItemClickListener {

    private static final String TAG = "FindParkingActivity";

    private EditText etSearch;
    private ImageView ivBack;
    private RecyclerView parkingRecyclerView;
    private ParkingAdapter parkingAdapter;
    private List<ParkingLocation> fullParkingList; // Untuk menyimpan data asli
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        etSearch = findViewById(R.id.etSearch);
        ivBack = findViewById(R.id.ivBack);
        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);

        db = FirebaseFirestore.getInstance();
        fullParkingList = new ArrayList<>();

        setupRecyclerView();
        setupSearchListener();

        ivBack.setOnClickListener(v -> onBackPressed());

        loadAllParkingLocations();
    }

    private void setupRecyclerView() {
        // Kirim list kosong pada awalnya, akan diisi nanti
        parkingAdapter = new ParkingAdapter(new ArrayList<>(), this);
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parkingRecyclerView.setAdapter(parkingAdapter);
    }

    @Override
    public void onItemClick(ParkingLocation location) {
        Intent intent = new Intent(FindParkingActivity.this, ParkingDetailActivity.class);
        intent.putExtra("DOCUMENT_ID", location.getDocumentId());
        startActivity(intent);
    }

    private void setupSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String text) {
        List<ParkingLocation> filteredList = new ArrayList<>();
        for (ParkingLocation item : fullParkingList) {
            // Filter berdasarkan nama lokasi (tidak case-sensitive)
            if (item.getName().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }
        parkingAdapter.updateData(filteredList);
    }

    private void loadAllParkingLocations() {
        db.collection("parking_locations").orderBy("name").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        fullParkingList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkingLocation location = document.toObject(ParkingLocation.class);
                            location.setDocumentId(document.getId());
                            fullParkingList.add(location);
                        }
                        // Tampilkan semua data pada awalnya
                        parkingAdapter.updateData(new ArrayList<>(fullParkingList));
                    } else {
                        Log.e(TAG, "Error getting parking locations: ", task.getException());
                        Toast.makeText(FindParkingActivity.this, "Gagal memuat data parkir.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}