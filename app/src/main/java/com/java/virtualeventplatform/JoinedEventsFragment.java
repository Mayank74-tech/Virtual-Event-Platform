package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.java.virtualeventplatform.adapters.EventAdapter;
import com.java.virtualeventplatform.models.Event;

import java.util.ArrayList;
import java.util.List;

public class JoinedEventsFragment extends Fragment implements EventAdapter.OnItemClickListener {

    private ProgressBar progressBar;
    private EventAdapter adapter;
    private final List<Event> joinedEvents = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration listenerRegistration;

    public JoinedEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_joined_events, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewJoinedEvents);
        progressBar = view.findViewById(R.id.progressBarJoinedEvents);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventAdapter(joinedEvents, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        if (currentUserId != null) {
            listenToJoinedEvents();
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void listenToJoinedEvents() {
        progressBar.setVisibility(View.VISIBLE);

        listenerRegistration = db.collection("JoinedEvents")
                .whereEqualTo("userId", currentUserId)
                .addSnapshotListener((joinedSnapshot, e) -> {
                    if (e != null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (joinedSnapshot == null || joinedSnapshot.isEmpty()) {
                        joinedEvents.clear();
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No joined events found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Collect all eventIds
                    List<String> eventIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : joinedSnapshot) {
                        String eventId = doc.getString("eventId");
                        if (eventId != null) {
                            eventIds.add(eventId);
                        }
                    }

                    if (eventIds.isEmpty()) {
                        joinedEvents.clear();
                        adapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    // Now listen to events in real-time
                    db.collection("Events")
                            .whereIn("eventId", eventIds) // Make sure your Events docs have "eventId" field
                            .addSnapshotListener((eventSnapshot, err) -> {
                                progressBar.setVisibility(View.GONE);

                                if (err != null) {
                                    Toast.makeText(getContext(), "Error: " + err.getMessage(), Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (eventSnapshot != null && !eventSnapshot.isEmpty()) {
                                    joinedEvents.clear();
                                    for (DocumentSnapshot eventDoc : eventSnapshot) {
                                        Event event = eventDoc.toObject(Event.class);
                                        if (event != null) {
                                            event.setEventId(eventDoc.getId()); // Use docId as fallback
                                            joinedEvents.add(event);
                                        }
                                    }
                                    adapter.notifyDataSetChanged();
                                } else {
                                    joinedEvents.clear();
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    @Override
    public void onItemClick(Event event) {
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("fromJoined", true);
        startActivity(intent);
    }
}
