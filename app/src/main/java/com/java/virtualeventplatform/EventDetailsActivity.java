package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.java.virtualeventplatform.models.Event;

public class EventDetailsActivity extends AppCompatActivity {
    private ImageView eventImage;
    private TextView eventTitle, eventDate, eventDescription;
    private MaterialButton joinChatButton, joinVideoButton;

    private FirebaseFirestore db;
    private String eventId;
    private String currentUserId;
    private String hostId;
    private String status = "upcoming"; // default

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

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        // Get eventId passed from HomeActivity
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Missing event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Real-time updates
        loadEventDetails(eventId);

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
                Intent intent = new Intent(this, VideoCallActivity.class);
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

                            // ✅ Host UI
                            if (currentUserId != null && currentUserId.equals(hostId)) {
                                // Host → Go to HostEventActivity
                                Intent intent = new Intent(this, HostEventActivity.class);
                                intent.putExtra("eventId", eventId);
                                startActivity(intent);
                                finish(); // close details screen
                            } else {
                                // Participant → Stay here
                                joinChatButton.setVisibility(View.VISIBLE);
                                joinVideoButton.setVisibility(View.VISIBLE);
                            }

                        }
                        }

                });
    }
}
