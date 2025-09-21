package com.java.virtualeventplatform;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class HostEventActivity extends AppCompatActivity {

    private MaterialButton startChatBtn, startVideoBtn, endEventBtn;
    private FirebaseFirestore db;
    private String eventId;
    private String currentUserId;
    private TextView eventTitle, eventDescription, eventDate,eventTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_event);

        // Init Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Event ID
        eventId = getIntent().getStringExtra("eventId");

        eventTitle = findViewById(R.id.eventTitleHost);
        eventDescription = findViewById(R.id.eventDescriptionHost);
        eventDate = findViewById(R.id.eventDateHost);
        eventTime = findViewById(R.id.eventTime);

        // Init Buttons
        startChatBtn = findViewById(R.id.startChatBtn);
        startVideoBtn = findViewById(R.id.startVideoBtn);
        endEventBtn = findViewById(R.id.endEventBtn);


        loadEventDetails();
        // Start Chat
        startChatBtn.setOnClickListener(v -> {
            db.collection("Events").document(eventId)
                    .update("status", "live")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Chat started!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ChatActivity.class);
                        intent.putExtra("eventId", eventId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // Start Video
        startVideoBtn.setOnClickListener(v -> {
            db.collection("Events").document(eventId)
                    .update("status", "live")
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Video started!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, ConferenceActivity.class);
                        intent.putExtra("eventId", eventId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });

        // End Event
        // End Event
        endEventBtn.setOnClickListener(v -> {
            new AlertDialog.Builder(HostEventActivity.this)
                    .setTitle("End Event")
                    .setMessage("Are you sure you want to end and delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.collection("Events").document(eventId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Event ended!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this, HomeActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }
    private void loadEventDetails() {
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("time");

                        eventTitle.setText(title != null ? title : "No Title");
                        eventDescription.setText(description != null ? description : "No Description");
                        eventDate.setText(date != null ? date : "No Date");
                        eventTime.setText(time!=null?time:"No mentioned");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}

