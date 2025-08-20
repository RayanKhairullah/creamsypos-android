package com.example.creamsyapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.creamsyapp.R;
import com.example.creamsyapp.supabase.SupabaseHelper;

public class AuthActivity extends AppCompatActivity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private SupabaseHelper supabaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        supabaseHelper = SupabaseHelper.getInstance();
        supabaseHelper.init(getApplicationContext());

        // Coba pulihkan/refresh sesi; jika berhasil langsung masuk ke Main
        supabaseHelper.initializeSession(this, new SupabaseHelper.SessionInitCallback() {
            @Override
            public void onReady() { runOnUiThread(() -> navigateToMainActivity()); }

            @Override
            public void onRequireLogin() { /* stay on this screen */ }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> Toast.makeText(AuthActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password tidak boleh kosong");
            return;
        }

        supabaseHelper.signIn(email, password, new SupabaseHelper.AuthCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(AuthActivity.this, "Login berhasil", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log.e("AuthActivity", "Login error details: " + error);
                    String friendly;
                    String lower = error != null ? error.toLowerCase() : "";
                    if (lower.contains("invalid") || lower.contains("invalid login") || lower.contains("invalid_grant") || lower.contains("credentials")) {
                        friendly = "Email atau password salah.";
                    } else if (lower.contains("network") || lower.contains("timeout") || lower.contains("failed to connect")) {
                        friendly = "Koneksi internet bermasalah. Coba lagi.";
                    } else {
                        friendly = "Login gagal. Periksa email/password dan coba lagi.";
                    }
                    Toast.makeText(AuthActivity.this, friendly, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}