package com.java.virtualeventplatform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.java.virtualeventplatform.R;
import com.java.virtualeventplatform.models.Event;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;
    private OnItemClickListener listener;

    // ✅ Click listener interface
    public interface OnItemClickListener {
        void onItemClick(Event event);
    }

    // ✅ Constructor: data + listener
    public EventAdapter(List<Event> eventList, OnItemClickListener listener) {
        this.eventList = eventList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // ✅ Update dataset dynamically
    public void updateEvents(List<Event> newEvents) {
        this.eventList = newEvents;
        notifyDataSetChanged();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView title, date,time;
        ImageView image;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.eventDate);
            image = itemView.findViewById(R.id.eventImage);
            time = itemView.findViewById(R.id.eventTime);
        }

        public void bind(final Event event, final OnItemClickListener listener) {
            title.setText(event.getTitle());
            date.setText(event.getDate());
            time.setText(event.getTime());

            // ✅ Load image with Glide
            if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
                Glide.with(image.getContext())
                        .load(event.getImageUrl())
                        .placeholder(R.drawable.ic_event_placeholder)
                        .into(image);
            } else {
                image.setImageResource(R.drawable.ic_event_placeholder);
            }

            // ✅ Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(event);
            });
        }
    }
}
