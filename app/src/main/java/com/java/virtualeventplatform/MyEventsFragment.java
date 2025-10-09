package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.java.virtualeventplatform.adapters.EventAdapter;
import com.java.virtualeventplatform.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMyEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        eventAdapter = new EventAdapter(eventList, event -> {
            Intent intent = new Intent(getContext(), HostEventActivity.class);
            intent.putExtra("eventId", event.getEventId()); // pass event ID
            startActivity(intent);
        });

        recyclerView.setAdapter(eventAdapter);


        // ✅ TEMP: load all events (ignore filter)
        loadMyEvents();
        onResume();

        return view;
    }

    private void loadMyEvents() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String currentUserId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("Events")
                .whereEqualTo("hostId", currentUserId) // ✅ Only listen to current user's events
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value == null || value.isEmpty()) {
                        Toast.makeText(getContext(), "No events created by you", Toast.LENGTH_SHORT).show();
                        eventList.clear();
                        eventAdapter.updateEvents(eventList);
                        return;
                    }

                    eventList.clear();
                    for (DocumentSnapshot doc : value) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            eventList.add(event);
                        }
                    }
                    eventAdapter.updateEvents(eventList);
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyEvents(); // ✅ Only user’s events
    }
}
