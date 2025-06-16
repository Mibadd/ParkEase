package com.example.parkir;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvParkingLocationName, tvParkingLocationAddress, tvDateTime,
            tvParkingSlotArea, tvParkingSlotNumber, tvPricePerHour, tvTotalPrice, tvSelectPayment;
    private EditText etDuration;
    private Button btnPayNow;
    private ImageView backButton;
    private LinearLayout paymentMethodContainer; // Tambahkan ini

    // Variabel untuk data booking
    private String locationName, address, areaName, slotNumber, documentId, userId;
    private int pricePerHour, floorLevel;
    private String selectedPaymentMethod = "GoPay"; // Default payment method

    // Inisialisasi Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        tvParkingLocationName = findViewById(R.id.tvParkingLocationName);
        tvParkingLocationAddress = findViewById(R.id.tvParkingLocationAddress);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvParkingSlotArea = findViewById(R.id.tvParkingSlotArea);
        tvParkingSlotNumber = findViewById(R.id.tvParkingSlotNumber);
        tvPricePerHour = findViewById(R.id.tvPricePerHour);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        etDuration = findViewById(R.id.etDuration);
        btnPayNow = findViewById(R.id.btnPayNow);
        backButton = findViewById(R.id.backButton);
        paymentMethodContainer = findViewById(R.id.paymentMethodContainer); // Inisialisasi container
        tvSelectPayment = findViewById(R.id.tvSelectPayment); // Inisialisasi text view pembayaran

        // Set default payment method text
        tvSelectPayment.setText(selectedPaymentMethod);

        // Get data dari intent
        Intent intent = getIntent();
        locationName = intent.getStringExtra("locationName");
        address = intent.getStringExtra("address");
        areaName = intent.getStringExtra("areaName");
        slotNumber = intent.getStringExtra("slotNumber");
        pricePerHour = intent.getIntExtra("pricePerHour", 0);
        floorLevel = intent.getIntExtra("floorLevel", 0);
        documentId = intent.getStringExtra("documentId");
        userId = intent.getStringExtra("userId");

        // Set data to views
        tvParkingLocationName.setText(locationName);
        tvParkingLocationAddress.setText(address);
        tvParkingSlotArea.setText(String.format(Locale.getDefault(), "%s, Lt. %d", areaName, floorLevel));
        tvParkingSlotNumber.setText(slotNumber);

        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yy, HH:mm", Locale.getDefault());
        tvDateTime.setText(sdf.format(new Date()));

        String priceText = formatPrice(pricePerHour) + " / hour";
        tvPricePerHour.setText(priceText);

        updateTotalPrice();

        etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateTotalPrice();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        backButton.setOnClickListener(v -> finish());

        // --- BARU: Listener untuk menampilkan dialog pilihan pembayaran ---
        paymentMethodContainer.setOnClickListener(v -> showPaymentMethodDialog());

        btnPayNow.setOnClickListener(v -> {
            if (etDuration.getText().toString().isEmpty() || Integer.parseInt(etDuration.getText().toString()) <= 0) {
                Toast.makeText(this, "Durasi harus diisi dengan benar", Toast.LENGTH_SHORT).show();
                return;
            }
            processBooking();
        });
    }

    // --- BARU: Method untuk menampilkan dialog popup pilihan pembayaran ---
    private void showPaymentMethodDialog() {
        final String[] paymentMethods = {"GoPay", "OVO", "DANA", "Credit/Debit Card"};
        int checkedItem = -1; // Default tidak ada yang terpilih
        // Cari indeks dari metode yang sedang dipilih
        for (int i = 0; i < paymentMethods.length; i++) {
            if (paymentMethods[i].equals(selectedPaymentMethod)) {
                checkedItem = i;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Payment Method");
        builder.setSingleChoiceItems(paymentMethods, checkedItem, (dialog, which) -> {
            // Ketika pengguna memilih item, update variabel dan teks
            selectedPaymentMethod = paymentMethods[which];
            tvSelectPayment.setText(selectedPaymentMethod);
            dialog.dismiss(); // Tutup dialog setelah memilih
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTotalPrice() {
        String durationStr = etDuration.getText().toString();
        if (durationStr.isEmpty()) {
            tvTotalPrice.setText(formatPrice(0));
            return;
        }
        try {
            int duration = Integer.parseInt(durationStr);
            long totalPrice = (long) duration * pricePerHour;
            tvTotalPrice.setText(formatPrice(totalPrice));
        } catch (NumberFormatException e) {
            tvTotalPrice.setText(formatPrice(0));
        }
    }

    private String formatPrice(long price) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        format.setMaximumFractionDigits(0);
        return format.format(price);
    }

    private void processBooking() {
        btnPayNow.setEnabled(false);
        Toast.makeText(this, "Memproses pesanan Anda...", Toast.LENGTH_SHORT).show();

        final DocumentReference locationRef = db.collection("parking_locations").document(documentId);
        final int duration = Integer.parseInt(etDuration.getText().toString());
        final long totalPrice = (long) duration * pricePerHour;

        db.runTransaction((Transaction.Function<Void>) transaction -> {
            List<Map<String, Object>> areasFromDB = (List<Map<String, Object>>) transaction.get(locationRef).get("parking_areas");
            if (areasFromDB == null) {
                throw new FirebaseFirestoreException("Area parkir tidak ditemukan.", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            boolean slotFoundAndUpdate = false;
            for (Map<String, Object> areaMap : areasFromDB) {
                if (Objects.equals(areaMap.get("area_name"), areaName)) {
                    List<Map<String, Object>> slotsFromDB = (List<Map<String, Object>>) areaMap.get("slots");
                    if (slotsFromDB != null) {
                        for (Map<String, Object> slotMap : slotsFromDB) {
                            if (Objects.equals(slotMap.get("slot_number"), slotNumber)) {
                                if (Objects.equals(slotMap.get("status"), "occupied")) {
                                    throw new FirebaseFirestoreException("Maaf, slot ini baru saja dipesan orang lain.", FirebaseFirestoreException.Code.ABORTED);
                                }
                                slotMap.put("status", "occupied");
                                slotMap.put("occupied_by_user_id", userId);
                                slotFoundAndUpdate = true;
                                break;
                            }
                        }
                    }

                    if (slotFoundAndUpdate) {
                        long occupiedCount = (long) areaMap.get("occupied_spots_count");
                        long availableCount = (long) areaMap.get("available_spots_count");
                        areaMap.put("occupied_spots_count", occupiedCount + 1);
                        areaMap.put("available_spots_count", availableCount - 1);
                        break;
                    }
                }
            }

            if (!slotFoundAndUpdate) {
                throw new FirebaseFirestoreException("Slot tidak ditemukan.", FirebaseFirestoreException.Code.NOT_FOUND);
            }

            transaction.update(locationRef, "parking_areas", areasFromDB);

            DocumentReference bookingRef = db.collection("bookings").document();
            Map<String, Object> bookingData = new HashMap<>();
            bookingData.put("userId", userId);
            bookingData.put("locationId", documentId);
            bookingData.put("locationName", locationName);
            bookingData.put("areaName", areaName);
            bookingData.put("slotNumber", slotNumber);
            bookingData.put("bookingTime", new Date());
            bookingData.put("durationInHours", duration);
            bookingData.put("totalPrice", totalPrice);
            bookingData.put("status", "active");
            // --- BARU: Simpan metode pembayaran yang dipilih ---
            bookingData.put("paymentMethod", selectedPaymentMethod);

            transaction.set(bookingRef, bookingData);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Intent successIntent = new Intent(PaymentActivity.this, BookingSuccessActivity.class);
            successIntent.putExtra("locationName", locationName);
            successIntent.putExtra("areaName", areaName);
            successIntent.putExtra("slotNumber", slotNumber);
            startActivity(successIntent);
            finishAffinity();
        }).addOnFailureListener(e -> {
            Toast.makeText(PaymentActivity.this, "Booking Gagal: " + e.getMessage(), Toast.LENGTH_LONG).show();
            btnPayNow.setEnabled(true);
        });
    }
}