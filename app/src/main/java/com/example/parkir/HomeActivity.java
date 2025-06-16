package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkir.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Menggunakan View Binding untuk mengakses komponen layout
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Memeriksa apakah user sudah login
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Jika belum, kembali ke LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Mengatur nama pengguna di header
        setupWelcomeMessage(currentUser);


        // --- INI BAGIAN YANG DIPERBAIKI ---
        // Menambahkan OnClickListener ke komponen dengan ID baru

        // Listener untuk ikon profil di pojok kanan atas
        binding.ivProfileIcon.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Listener untuk kartu "Cari Parkir"
        binding.findParkingCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FindParkingActivity.class));
        });

        // Listener untuk kartu "Riwayat"
        binding.historyCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, HistoryActivity.class));
        });

        // Listener untuk kartu "Profil"
        binding.profileCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, ProfileActivity.class));
        });

        // Listener untuk kartu "Pengaturan"
        binding.settingsCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

        // Listener untuk tombol Logout


        // Listener untuk search bar (opsional, jika ingin ada aksi saat di-klik)
        binding.searchCard.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, FindParkingActivity.class));
        });
    }

    private void setupWelcomeMessage(FirebaseUser user) {
        // Ambil nama pengguna dari Firestore
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.welcomeText.setText("Hai, " + name + "!");
                        } else {
                            binding.welcomeText.setText("Selamat Datang!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    binding.welcomeText.setText("Selamat Datang!");
                });
    }


}