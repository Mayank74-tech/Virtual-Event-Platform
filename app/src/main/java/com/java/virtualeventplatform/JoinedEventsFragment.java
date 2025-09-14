package com.java.virtualeventplatform;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
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
        adapter = new EventAdapter(joinedEvents, this); // ✅ correct listener
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        loadJoinedEvents();

        return view;
    }

    private void loadJoinedEvents() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("JoinedEvents")
                .whereEqualTo("userId", currentUserId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    joinedEvents.clear();

                    if (querySnapshot.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No joined events found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    // Temporary list to accumulate events before notifying adapter
                    List<Event> tempList = new ArrayList<>();

                    int total = querySnapshot.size();
                    int[] processedCount = {0};

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String eventId = doc.getString("eventId");

                        if (eventId != null) {
                            db.collection("Events").document(eventId)
                                    .get()
                                    .addOnSuccessListener(eventSnapshot -> {
                                        processedCount[0]++;
                                        if (eventSnapshot.exists()) {
                                            Event event = eventSnapshot.toObject(Event.class);
                                            if (event != null) {
                                                event.setEventId(eventId);
                                                tempList.add(event);
                                            }
                                        }

                                        // Only update adapter after all events are processed
                                        if (processedCount[0] == total) {
                                            joinedEvents.addAll(tempList);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        processedCount[0]++;
                                        if (processedCount[0] == total) {
                                            joinedEvents.addAll(tempList);
                                            adapter.notifyDataSetChanged();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        } else {
                            processedCount[0]++;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onItemClick(Event event) {
        // Open EventDetailsActivity with fromJoined flag
        Intent intent = new Intent(getContext(), EventDetailsActivity.class);
        intent.putExtra("eventId", event.getEventId());
        intent.putExtra("fromJoined", true);
        startActivity(intent);
    }
}
