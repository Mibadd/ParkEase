package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set layout untuk Splash Screen
        setContentView(R.layout.activity_main);

        // Handler untuk menunda layar Splash selama 3 detik
        new Handler().postDelayed(() -> {
            // Pindah ke Activity Utama (HomeActivity)
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish(); // Menutup Splash Screen agar tidak kembali saat menekan tombol Back
        }, 3000); // 3000ms = 3 detik
    }
}
