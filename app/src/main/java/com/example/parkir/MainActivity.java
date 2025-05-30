
package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private static final long SPLASH_SCREEN_DELAY = 2000; // Delay splash screen 2 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Pastikan ini menunjuk ke layout splash screen Anda

        mAuth = FirebaseAuth.getInstance();

        // Menggunakan Handler untuk menunda pemeriksaan login
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser == null) {
                    // Jika tidak ada user yang login, arahkan ke LoginActivity
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Jika ada user, arahkan ke HomeActivity
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                finish(); // Tutup MainActivity setelah pengarahan
            }
        }, SPLASH_SCREEN_DELAY);
    }
}