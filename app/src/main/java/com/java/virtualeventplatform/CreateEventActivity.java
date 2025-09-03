package com.java.virtualeventplatform;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;public class CreateEventActivity extends AppCompatActivity {
    private EditText titleEt, descEt, dateEt;
    private Button createBtn;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        // init Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // bind views
        titleEt = findViewById(R.id.titleEt);
        descEt = findViewById(R.id.descEt);
        dateEt = findViewById(R.id.dateEt);
        createBtn = findViewById(R.id.createBtn);

        // 🚀 Date picker listener should be here
        dateEt.setFocusable(false); // disable keyboard
        dateEt.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    CreateEventActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String selectedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        dateEt.setText(selectedDate);
                    }, year, month, day);

            datePickerDialog.show();
        });

        // button click listener
        createBtn.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        String title = titleEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String date = dateEt.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String hostId = mAuth.getCurrentUser().getUid();
        String eventId = db.collection("Events").document().getId();

        // generate random password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        String eventPassword = sb.toString();

        // store event data
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventId);
        event.put("title", title);
        event.put("description", desc);
        event.put("date", date);
        event.put("hostId", hostId);
        event.put("imageUrl", "");
        event.put("password", eventPassword);
        event.put("status", "upcoming");

        db.collection("Events").document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Event Created!")
                            .setMessage("Share this password with participants:\n\n" + eventPassword)
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

