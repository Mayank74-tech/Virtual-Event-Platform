package com.java.virtualeventplatform;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.java.virtualeventplatform.adapters.EventAdapter;
import com.java.virtualeventplatform.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UpcomingEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private final List<Event> eventList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upcoming_events);

        // Handle system insets (status/nav bars)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewUpcomingEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize Adapter
        eventAdapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("eventId", event.getEventId());
            intent.putExtra("fromJoined", false);
            startActivity(intent);
        });

        recyclerView.setAdapter(eventAdapter);

        // Load events
        loadUpcomingEvents();
    }

    private void loadUpcomingEvents() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("Events")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    eventList.clear();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Event event = doc.toObject(Event.class);

                        if (event != null) {
                            event.setEventId(doc.getId());

                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                long eventTime = sdf.parse(event.getDate()).getTime();

                                if (eventTime < now) {
                                    // ❌ Delete expired events
                                    FirebaseFirestore.getInstance()
                                            .collection("Events")
                                            .document(event.getEventId())
                                            .delete();
                                } else {
                                    // ✅ Add upcoming events not hosted by current user
                                    if (!event.getHostId().equals(currentUserId)) {
                                        eventList.add(event);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    eventAdapter.updateEvents(eventList);
                    Toast.makeText(this, "Loaded " + eventList.size() + " events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
