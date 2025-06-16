package com.example.parkir;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkir.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Terima data yang dikirim dari ProfileActivity
        receiveInitialData();

        // Atur listener untuk tombol
        setupActionListeners();
    }

    private void receiveInitialData() {
        // Ambil intent yang memulai activity ini
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String currentName = extras.getString("CURRENT_NAME");
            String currentEmail = extras.getString("CURRENT_EMAIL");

            // Set data ke field EditText
            binding.etName.setText(currentName);
            binding.etEmail.setText(currentEmail);
        }
    }

    private void setupActionListeners() {
        // Listener untuk tombol kembali di toolbar
        binding.toolbarEditProfile.setNavigationOnClickListener(v -> finish());

        // Listener untuk tombol simpan
        binding.btnSaveProfile.setOnClickListener(v -> {
            String newName = binding.etName.getText().toString().trim();

            // Validasi input
            if (newName.isEmpty()) {
                binding.etName.setError("Nama tidak boleh kosong");
                return;
            }

            // Panggil metode untuk update ke Firestore
            updateProfileInFirestore(newName);
        });
    }

    private void updateProfileInFirestore(String newName) {
        if (currentUser != null) {
            String uid = currentUser.getUid();
            DocumentReference userRef = db.collection("users").document(uid);

            // Gunakan .update() untuk mengubah field tertentu saja
            userRef.update("name", newName)
                    .addOnSuccessListener(aVoid -> {
                        // Sukses
                        Toast.makeText(EditProfileActivity.this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                        finish(); // Kembali ke halaman profil
                    })
                    .addOnFailureListener(e -> {
                        // Gagal
                        Toast.makeText(EditProfileActivity.this, "Gagal memperbarui profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}