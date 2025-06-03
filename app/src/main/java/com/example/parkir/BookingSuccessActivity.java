package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookingSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_success);

        TextView tvLocationName = findViewById(R.id.tvLocationName);
        TextView tvSlotDetails = findViewById(R.id.tvSlotDetails);
        TextView tvTimestamp = findViewById(R.id.tvTimestamp);
        Button btnViewHistory = findViewById(R.id.btnViewHistory);

        // Ambil data dari Intent
        String location = getIntent().getStringExtra("locationName");
        String area = getIntent().getStringExtra("areaName");
        String slot = getIntent().getStringExtra("slotNumber");

        tvLocationName.setText("di " + location);
        tvSlotDetails.setText("Slot " + area + ", Nomor " + slot);

        // Tampilkan waktu saat ini
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy, HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        tvTimestamp.setText(currentTime);

        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(BookingSuccessActivity.this, HistoryActivity.class);
            // Membersihkan activity stack sebelumnya agar tidak bisa kembali ke halaman slot
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Mencegah pengguna kembali ke halaman pemilihan slot
        super.onBackPressed();
        Intent intent = new Intent(BookingSuccessActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}