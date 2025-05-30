// RegisterActivity.java
package com.example.parkir; // Sesuaikan dengan nama package Anda

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameEditText, emailEditText, passwordEditText;
    private CheckBox termsAndConditionCheckbox;
    private Button createAccountButton;
    private TextView termsAndConditionLink;
    private ImageView backArrow;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Menggunakan activity_register.xml

        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi Views dari XML (sesuai ID di activity_register.xml)
        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        termsAndConditionCheckbox = findViewById(R.id.termsAndConditionCheckbox);
        createAccountButton = findViewById(R.id.createAccountButton);
        termsAndConditionLink = findViewById(R.id.termsAndConditionLink);
        backArrow = findViewById(R.id.backArrow);

        // Listener untuk tombol kembali
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> onBackPressed());
        }

        // Listener untuk link Syarat & Ketentuan
        termsAndConditionLink.setOnClickListener(v -> {
            Toast.makeText(RegisterActivity.this, "Membuka Syarat & Ketentuan", Toast.LENGTH_SHORT).show();
            // Implementasi untuk membuka URL/halaman syarat & ketentuan
        });

        // Listener untuk tombol Buat Akun
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String name = nameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(name)) {
            nameEditText.setError("Nama tidak boleh kosong.");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email tidak boleh kosong.");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password tidak boleh kosong.");
            passwordEditText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password minimal 6 karakter.");
            passwordEditText.requestFocus();
            return;
        }

        if (!termsAndConditionCheckbox.isChecked()) {
            Toast.makeText(this, "Anda harus menyetujui Syarat & Ketentuan.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proses pembuatan akun dengan Firebase Email/Password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registrasi berhasil
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(RegisterActivity.this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show();

                            // Opsional: Simpan nama pengguna ke Firestore/Realtime Database jika perlu
                            // Anda perlu menambahkan dependencies yang sesuai di Gradle
                            // FirebaseFirestore db = FirebaseFirestore.getInstance();
                            // Map<String, Object> userData = new HashMap<>();
                            // userData.put("name", name);
                            // userData.put("email", email);
                            // db.collection("users").document(user.getUid()).set(userData);

                            // Arahkan ke Activity Login setelah registrasi berhasil
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Tutup RegisterActivity
                        } else {
                            // Jika registrasi gagal
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, "Registrasi gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}