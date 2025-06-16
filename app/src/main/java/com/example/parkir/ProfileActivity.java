package com.example.parkir;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Impor Glide
import com.example.parkir.databinding.ActivityProfileBinding;
import com.google.firebase.auth.FirebaseAuth; // Impor Firebase Auth
import com.google.firebase.auth.FirebaseUser; // Impor Firebase User
import com.google.firebase.firestore.DocumentReference; // Impor Firestore
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;
    // Deklarasi variabel Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Cek jika ada pengguna yang login atau tidak
        if (currentUser == null) {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_SHORT).show();
            finish(); // Tutup activity ini
            return; // Hentikan eksekusi lebih lanjut
        }

        // Panggil metode untuk mengisi data dan mengatur listener
        loadUserData();
        setupActionListeners();
    }
    @Override
    protected void onResume() {
        super.onResume();
        // Panggil kembali loadUserData setiap kali activity ini kembali ditampilkan
        // Ini akan memastikan data yang ditampilkan selalu yang terbaru dari Firestore
        if (mAuth.getCurrentUser() != null) {
            loadUserData();
        }
    }

    /**
     * Metode untuk memuat data pengguna dari Cloud Firestore.
     */
    private void loadUserData() {
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Dokumen ditemukan, ambil datanya
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String profileImageUrl = documentSnapshot.getString("profileImageUrl");

                // Set data ke tampilan UI
                binding.tvUserName.setText(name);
                binding.tvUserEmail.setText(email);

            } else {
                Toast.makeText(this, "Data profil tidak ditemukan di database.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            // Gagal mengambil data
            Toast.makeText(this, "Gagal memuat data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ProfileActivity", "Error getting user data", e);
        });
    }

    /**
     * Metode untuk mengatur semua listener untuk elemen yang dapat diklik.
     */
    private void setupActionListeners() {
        // Tombol kembali
        binding.ivBackProfile.setOnClickListener(v -> {
            finish(); // Menutup activity saat ini
        });

        // Tombol Edit Profil
        binding.btnEditProfile.setOnClickListener(v -> {
            // Ambil data saat ini dari TextViews
            String currentName = binding.tvUserName.getText().toString();
            String currentEmail = binding.tvUserEmail.getText().toString();

            // Buat Intent untuk membuka EditProfileActivity
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);

            // Kirim data saat ini ke activity berikutnya menggunakan putExtra
            intent.putExtra("CURRENT_NAME", currentName);
            intent.putExtra("CURRENT_EMAIL", currentEmail);

            startActivity(intent);
        });

        // Opsi Pengaturan
        binding.layoutSettings.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Opsi Logout
        binding.layoutLogout.setOnClickListener(v -> {
            showLogoutConfirmationDialog();
        });
    }

    /**
     * Menampilkan dialog konfirmasi sebelum melakukan logout.
     */
    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Logout")
                .setMessage("Apakah Anda yakin ingin keluar dari akun Anda?")
                .setPositiveButton("Ya, Logout", (dialog, which) -> {
                    mAuth.signOut(); // Logout dari Firebase
                    Toast.makeText(this, "Anda telah logout", Toast.LENGTH_SHORT).show();
                    // Arahkan pengguna ke halaman login dan bersihkan activity stack
                    // Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    // startActivity(intent);
                    finish();
                })
                .setNegativeButton("Batal", (dialog, which) -> {
                    dialog.dismiss();
                })
                .show();
    }
}