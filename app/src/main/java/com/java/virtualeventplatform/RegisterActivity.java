package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEt, passwordEt, otpEt;
    private Button sendOtpBtn, verifyOtpBtn, googleSignInBtn;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String generatedOtp = "";
    private static final int RC_SIGN_IN = 100;

    private GoogleSignInClient mGoogleSignInClient;

    // 🔑 Replace with your SendGrid API Key
    private static final String SENDGRID_API_KEY = "Your SendGrid APIKey";
    private static final String FROM_EMAIL = "Your Sending Email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        otpEt = findViewById(R.id.otpEt);
        sendOtpBtn = findViewById(R.id.sendOtpBtn);
        verifyOtpBtn = findViewById(R.id.verifyOtpBtn);
        googleSignInBtn = findViewById(R.id.googleSignInBtn);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Google SignIn setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Send OTP Button
        sendOtpBtn.setOnClickListener(v -> sendOtp());

        // Verify OTP Button
        verifyOtpBtn.setOnClickListener(v -> verifyOtp());

        // Google SignIn
        googleSignInBtn.setOnClickListener(v -> signInWithGoogle());

        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
            finish();
            return;
        }
    }

    private void sendOtp() {
        String email = emailEt.getText().toString();
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate OTP
        generatedOtp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Save OTP in Firestore with expiry
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", generatedOtp);
        otpData.put("timestamp", System.currentTimeMillis());

        db.collection("otps").document(email).set(otpData)
                .addOnSuccessListener(aVoid -> {
                    sendOtpEmail(email, generatedOtp);
                })
                .addOnFailureListener(e -> Log.e("Register", "Error saving OTP", e));
    }

    private void sendOtpEmail(String toEmail, String otp) {
        AsyncTask.execute(() -> {
            try {
                URL url = new URL("https://api.sendgrid.com/v3/mail/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + SENDGRID_API_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject emailData = new JSONObject();
                emailData.put("personalizations", new org.json.JSONArray()
                        .put(new JSONObject().put("to", new org.json.JSONArray()
                                .put(new JSONObject().put("email", toEmail)))));

                emailData.put("from", new JSONObject().put("email", FROM_EMAIL));
                emailData.put("subject", "Your OTP Code");
                emailData.put("content", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("type", "text/plain")
                                .put("value", "Your OTP is: " + otp)));

                OutputStream os = conn.getOutputStream();
                os.write(emailData.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d("SendGrid", "Response Code: " + responseCode);

                runOnUiThread(() -> {
                    if (responseCode == 202) {
                        Toast.makeText(this, "OTP sent to email", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e("SendGrid", "Error sending email", e);
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void verifyOtp() {
        String enteredOtp = otpEt.getText().toString();
        String email = emailEt.getText().toString();
        String password = passwordEt.getText().toString();

        if (enteredOtp.equals(generatedOtp)) {
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                saveUserToFirestore(user);
                            }
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Auth Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show();
        }
    }

    // Google Sign-In
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("GoogleSignIn", "Google sign in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }

                        Toast.makeText(this, "Google SignIn Successful", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Google SignIn Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void saveUserToFirestore(FirebaseUser user) {
        String userId = user.getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName() != null ? user.getDisplayName() : "");
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("Users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "User saved"))
                .addOnFailureListener(e -> Log.e("Firestore", "Error saving user", e));
    }



}
