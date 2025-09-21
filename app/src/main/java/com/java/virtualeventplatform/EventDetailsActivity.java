package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.java.virtualeventplatform.models.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class EventDetailsActivity extends AppCompatActivity {
    private ImageView eventImage;
    private TextView eventTitle, eventDate, eventDescription;
    private MaterialButton joinChatButton, joinVideoButton, joinEventButton;

    private FirebaseFirestore db;
    private String eventId;
    private String currentUserId;
    private String hostId;
    private String status = "upcoming";

    private boolean fromJoined = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventImage = findViewById(R.id.eventImageDetail);
        eventTitle = findViewById(R.id.eventTitleDetail);
        eventDate = findViewById(R.id.eventDateDetail);
        eventDescription = findViewById(R.id.eventDescriptionDetail);
        joinChatButton = findViewById(R.id.joinChatButton);
        joinVideoButton = findViewById(R.id.joinVideoButton);
        joinEventButton = findViewById(R.id.joinEventButton);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        eventId = getIntent().getStringExtra("eventId");
        fromJoined = getIntent().getBooleanExtra("fromJoined", false);

        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEventDetails(eventId);

        // Join Event
        joinEventButton.setOnClickListener(v -> joinEvent());

        // Join Chat
        joinChatButton.setOnClickListener(v -> {
            if ("live".equals(status)) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Chat available only when event is LIVE", Toast.LENGTH_SHORT).show();
            }
        });

        // Join Video
        joinVideoButton.setOnClickListener(v -> {
            if ("live".equals(status)) {
                Intent intent = new Intent(this, ConferenceActivity.class);
                intent.putExtra("eventId", eventId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Video available only when event is LIVE", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadEventDetails(String eventId) {
        db.collection("Events").document(eventId)
                .addSnapshotListener((documentSnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            hostId = event.getHostId();
                            status = documentSnapshot.getString("status");

                            eventTitle.setText(event.getTitle());
                            eventDate.setText(event.getDate());
                            eventDescription.setText(event.getDescription());
                            Glide.with(this).load(event.getImageUrl()).into(eventImage);

                            updateUIForUser();

                            // Check if already joined (for upcoming case)
                            if (!fromJoined && currentUserId != null) {
                                checkIfUserJoined();
                            }
                        }
                    }
                });
    }

    private void updateUIForUser() {
        if (currentUserId != null && currentUserId.equals(hostId)) {
            // Host
            Toast.makeText(this, "You are the host", Toast.LENGTH_SHORT).show();
            joinEventButton.setVisibility(android.view.View.GONE);
            joinChatButton.setVisibility(android.view.View.GONE);
            joinVideoButton.setVisibility(android.view.View.GONE);
        } else if (fromJoined) {
            // Joined Event
            joinEventButton.setVisibility(android.view.View.GONE);
            joinChatButton.setVisibility(android.view.View.VISIBLE);
            joinVideoButton.setVisibility(android.view.View.VISIBLE);
        } else {
            // Upcoming Event
            joinEventButton.setVisibility(android.view.View.VISIBLE);
            joinChatButton.setVisibility(android.view.View.GONE);
            joinVideoButton.setVisibility(android.view.View.GONE);
        }
    }

    private void joinEvent() {
        if (currentUserId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch event password from Firestore
        db.collection("Events").document(eventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String eventPassword = documentSnapshot.getString("password");
                        showPasswordDialog(eventPassword);
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showPasswordDialog(String correctPassword) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Enter Event Password");

        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Password");
        builder.setView(input);

        builder.setPositiveButton("Join", (dialog, which) -> {
            String enteredPassword = input.getText().toString().trim();
            if (enteredPassword.equals(correctPassword)) {
                addUserToJoinedEvents();
            } else {
                Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
    private void addUserToJoinedEvents() {
        if (currentUserId == null) return;

        String docId = eventId + "_" + currentUserId;
        FirebaseAuth auth = FirebaseAuth.getInstance();

        String userEmail = auth.getCurrentUser() != null && auth.getCurrentUser().getEmail() != null
                ? auth.getCurrentUser().getEmail() : "No Email";
        String userName = auth.getCurrentUser() != null && auth.getCurrentUser().getDisplayName() != null
                ? auth.getCurrentUser().getDisplayName() : "Anonymous";

        Map<String, Object> joinedEvent = new HashMap<>();
        joinedEvent.put("eventId", eventId);
        joinedEvent.put("userId", currentUserId);
        joinedEvent.put("userEmail", userEmail);
        joinedEvent.put("userName", userName);
        joinedEvent.put("joinedAt", System.currentTimeMillis());

        db.collection("JoinedEvents")
                .document(docId)
                .set(joinedEvent)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "You joined this event!", Toast.LENGTH_SHORT).show();
                    joinEventButton.setVisibility(View.GONE);
                    joinChatButton.setVisibility(View.VISIBLE);
                    joinVideoButton.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void checkIfUserJoined() {
        String docId = eventId + "_" + currentUserId;

        db.collection("JoinedEvents").document(docId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        joinEventButton.setText("Joined");
                        joinEventButton.setEnabled(false);
                    }
                });
    }
}
