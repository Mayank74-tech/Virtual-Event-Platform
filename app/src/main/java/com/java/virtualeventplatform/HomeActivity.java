package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.java.virtualeventplatform.adapters.EventAdapter;
import com.java.virtualeventplatform.models.Event;

import java.util.ArrayList;
import java.util.List;
public class HomeActivity extends AppCompatActivity {
    private RecyclerView eventsRecyclerView;
    private FloatingActionButton addEventFab;
    private FirebaseFirestore db;
    private List<Event> eventList;
    private EventAdapter adapter;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        addEventFab = findViewById(R.id.addEventFab);
        db = FirebaseFirestore.getInstance();

        eventList = new ArrayList<>();
        adapter = new EventAdapter(eventList, clickedEvent -> {
            Intent intent = new Intent(HomeActivity.this, EventDetailsActivity.class);
            intent.putExtra("eventId", clickedEvent.getEventId()); // send ID
            startActivity(intent);
        });

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(adapter);

        // Load events from Firestore
        loadEvents();

        // Go to CreateEventActivity
        addEventFab.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CreateEventActivity.class);
            startActivity(intent);

        });
    }

    private void loadEvents() {
        db.collection("Events").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(this, "Error loading events", Toast.LENGTH_SHORT).show();
                return;
            }
            eventList.clear();
            for (QueryDocumentSnapshot doc : snapshots) {
                Event event = doc.toObject(Event.class);
                event.setEventId(doc.getId());
                eventList.add(event);
            }
            adapter.notifyDataSetChanged();
        });
    }
}
