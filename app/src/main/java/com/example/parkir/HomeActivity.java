
package com.example.parkir; // Sesuaikan dengan nama package Anda



import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextView welcomeText;

    // CardViews untuk menu utama
    private CardView findParkingCard;
    private CardView historyCard;
    private CardView profileCard;
    private CardView settingsCard;
    private Button logoutButton; // Deklarasikan tombol logout

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Menggunakan activity_home.xml

        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi Views dari XML (sesuai ID di activity_home.xml)
        welcomeText = findViewById(R.id.welcomeText); // ID di XML adalah welcomeText

        findParkingCard = findViewById(R.id.findParkingCard);
        historyCard = findViewById(R.id.historyCard);
        profileCard = findViewById(R.id.profileCard);
        settingsCard = findViewById(R.id.settingsCard);
        logoutButton = findViewById(R.id.logoutButton); // Inisialisasi tombol logout

        // Cek status login saat Activity dibuat
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // Jika tidak ada user yang login, arahkan kembali ke LoginActivity
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Tutup HomeActivity
        } else {
            // Jika ada user, tampilkan pesan selamat datang dengan email pengguna
            welcomeText.setText("Selamat Datang, " + currentUser.getEmail() + "!\nTemukan parkir terdekat");
        }

        // --- Listener untuk CardViews ---
        findParkingCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Membuka Cari Parkir", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, FindParkingActivity.class);
                startActivity(intent);
            }
        });

        historyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Membuka Riwayat Parkir", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });

        profileCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Membuka Profile", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        settingsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomeActivity.this, "Membuka Pengaturan", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });


        // --- Listener untuk Tombol Logout ---
        logoutButton.setOnClickListener(v -> {
            mAuth.signOut(); // Logout dari Firebase
            Toast.makeText(HomeActivity.this, "Anda telah logout.", Toast.LENGTH_SHORT).show();
            // Arahkan kembali ke LoginActivity setelah logout
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Tutup HomeActivity
        });
    }
}