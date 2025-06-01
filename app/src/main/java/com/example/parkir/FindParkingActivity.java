package com.example.parkir;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.parkir.adapter.ParkingAdapter;
import com.example.parkir.model.ParkingLocation;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;

public class FindParkingActivity extends AppCompatActivity {

    private static final String TAG = "FindParkingActivity";

    private TextInputEditText locationInput;
    private Button searchButton;
    private RecyclerView parkingRecyclerView;
    private ParkingAdapter parkingAdapter;
    private List<ParkingLocation> parkingLocationList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_parking);

        locationInput = findViewById(R.id.locationInput);
        searchButton = findViewById(R.id.searchButton);
        parkingRecyclerView = findViewById(R.id.parkingRecyclerView);

        db = FirebaseFirestore.getInstance();
        parkingLocationList = new ArrayList<>();

        setupRecyclerView();

        searchButton.setOnClickListener(v -> {
            String queryText = locationInput.getText().toString().trim();
            // Implement search logic if needed, or filter client-side for simplicity.
            // For now, clicking search reloads all.
            loadParkingLocations(queryText);
        });

        // Initial load
        loadParkingLocations(null);
    }

    private void setupRecyclerView() {
        parkingAdapter = new ParkingAdapter(this, parkingLocationList, location -> {
            // Toast.makeText(FindParkingActivity.this, "Navigasi ke " + location.getName(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(FindParkingActivity.this, ParkingDetailActivity.class);
            intent.putExtra("locationId", location.getId()); // Kirim ID lokasi
            intent.putExtra("locationName", location.getName()); // Kirim nama lokasi untuk ditampilkan
            startActivity(intent);
        });
        parkingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parkingRecyclerView.setAdapter(parkingAdapter);
    }

    private void loadParkingLocations(String searchQuery) {
        Query query = db.collection("parking_locations");

        // Basic search: If you want to implement actual text search,
        // Firestore has limited capabilities. Consider a third-party service like Algolia
        // or structure your data for simpler "startsWith" queries if applicable.
        // For now, this example loads all and you could filter client-side or adjust query.
        // if (searchQuery != null && !searchQuery.isEmpty()) {
        //     query = query.whereGreaterThanOrEqualTo("name", searchQuery)
        //                  .whereLessThanOrEqualTo("name", searchQuery + "\uf8ff");
        // }

        query.orderBy("name") // Optional: order by name
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<ParkingLocation> tempList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            ParkingLocation location = document.toObject(ParkingLocation.class);
                            location.setId(document.getId()); // Important: Store document ID
                            tempList.add(location);
                        }
                        parkingAdapter.updateData(tempList); // Use adapter's update method
                        if (tempList.isEmpty()) {
                            Toast.makeText(FindParkingActivity.this, "Tidak ada lokasi parkir ditemukan.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error getting parking locations: ", task.getException());
                        Toast.makeText(FindParkingActivity.this, "Gagal memuat data parkir.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}