package com.java.virtualeventplatform;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.java.virtualeventplatform.adapters.EventAdapter;
import com.java.virtualeventplatform.models.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class UpcomingEventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<Event> eventList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming_events, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewUpcomingEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        eventAdapter = new EventAdapter(eventList, event -> {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), EventDetailsActivity.class);
                intent.putExtra("eventId", event.getEventId());
                intent.putExtra("fromJoined", false);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(eventAdapter);
        loadUpcomingEvents();
        return view;


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
                                // Parse event date (assuming "yyyy-MM-dd")
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                long eventTime = sdf.parse(event.getDate()).getTime();

                                if (eventTime < now) {
                                    // ❌ Delete expired event
                                    FirebaseFirestore.getInstance()
                                            .collection("Events")
                                            .document(event.getEventId())
                                            .delete();
                                } else {
                                    // ✅ Only add upcoming events that are not hosted by current user
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
                    Toast.makeText(getContext(), "Loaded " + eventList.size() + " events", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}