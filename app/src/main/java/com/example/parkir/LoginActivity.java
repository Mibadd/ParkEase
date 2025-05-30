// LoginActivity.java
package com.example.parkir; // Sesuaikan dengan nama package Anda

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailEditText, passwordLoginEditText; // Menggunakan emailEditText agar lebih jelas
    private Button loginButton;
    private TextView noAccountText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Menggunakan activity_login.xml

        mAuth = FirebaseAuth.getInstance();

        // Inisialisasi Views dari XML (sesuai ID di activity_login.xml)
        emailEditText = findViewById(R.id.phoneEditText); // ID di XML adalah phoneEditText, kita gunakan untuk email
        passwordLoginEditText = findViewById(R.id.passwordLoginEditText);
        loginButton = findViewById(R.id.loginButton);
        noAccountText = findViewById(R.id.noAccountText);

        // Listener untuk tombol Login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithEmailAndPassword();
            }
        });

        // Listener untuk teks "I don't have account"
        noAccountText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginWithEmailAndPassword() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordLoginEditText.getText().toString().trim();

        // Validasi input
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email tidak boleh kosong.");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLoginEditText.setError("Password tidak boleh kosong.");
            passwordLoginEditText.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login berhasil
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login berhasil!", Toast.LENGTH_SHORT).show();
                            // Arahkan ke Activity utama aplikasi Anda
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish(); // Tutup LoginActivity
                        } else {
                            // Login gagal
                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(LoginActivity.this, "Login gagal: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}