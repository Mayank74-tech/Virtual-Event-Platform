package com.java.virtualeventplatform;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreateEventActivity extends AppCompatActivity {
    private EditText titleEt, descEt, dateEt, timeEt;
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
        timeEt = findViewById(R.id.timeEt);
        createBtn = findViewById(R.id.createBtn);

        // 🚀 Date picker
        dateEt.setFocusable(false);
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

        // ⏰ Time picker
        timeEt.setFocusable(false);
        timeEt.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    CreateEventActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        // Convert to 12-hour format
                        String amPm = (selectedHour >= 12) ? "PM" : "AM";
                        int hour12 = (selectedHour % 12 == 0) ? 12 : selectedHour % 12;

                        String selectedTime = String.format(Locale.getDefault(),
                                "%02d:%02d %s", hour12, selectedMinute, amPm);
                        timeEt.setText(selectedTime);
                    }, hour, minute, false // false = 12-hour format with AM/PM
            );

            timePickerDialog.show();
        });

        // button click listener
        createBtn.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        String title = titleEt.getText().toString().trim();
        String desc = descEt.getText().toString().trim();
        String date = dateEt.getText().toString().trim();
        String time = timeEt.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty() || date.isEmpty() || time.isEmpty()) {
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
        event.put("time", time); // ✅ store time
        event.put("hostId", hostId);
        event.put("imageUrl", "");
        event.put("password", eventPassword);
        event.put("status", "upcoming");

        db.collection("Events").document(eventId).set(event)
                .addOnSuccessListener(aVoid -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Event Created!")
                            .setMessage("Event Password: " + eventPassword +
                                    "\nDate: " + date +
                                    "\nTime: " + time)
                            .setPositiveButton("OK", (dialog, which) -> finish())
                            .show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
