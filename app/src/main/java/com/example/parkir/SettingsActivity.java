package com.example.parkir;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.parkir.databinding.ActivitySettingsBinding;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the layout using View Binding
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- Fungsionalitas Tombol Kembali ---
        // Menambahkan listener klik pada ikon panah kembali
        binding.ivBackSettings.setOnClickListener(v -> {
            // Menutup activity saat ini dan kembali ke layar sebelumnya.
            onBackPressed();
        });

        // Anda bisa menambahkan logika lain untuk pengaturan di sini
        // Contoh:
        // binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
        //     // Simpan preferensi notifikasi
        // });
    }
}
