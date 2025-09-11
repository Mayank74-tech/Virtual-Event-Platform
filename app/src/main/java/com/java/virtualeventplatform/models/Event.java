package com.java.virtualeventplatform.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
    private String eventId;   // Firestore doc ID
    private String title;
    private String description;
    private String date;
    private String time;
    private String hostId;
    private String imageUrl;
    private String password;   // ✅ New field
    private String status;
    // ✅ upcoming | live | ended

    // Empty constructor (required for Firestore)
    public Event() {}

    public Event(String eventId, String title, String description, String date,
                 String hostId, String imageUrl, String password, String status) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.hostId = hostId;
        this.imageUrl = imageUrl;
        this.password = password;
        this.status = status;
    }

    // --- Getters & Setters ---
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // --- Parcelable implementation ---
    protected Event(Parcel in) {
        eventId = in.readString();
        title = in.readString();
        description = in.readString();
        date = in.readString();
        time=in.readString();
        hostId = in.readString();
        imageUrl = in.readString();
        password = in.readString();
        status = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(eventId);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(date);
        parcel.writeString(time);
        parcel.writeString(hostId);
        parcel.writeString(imageUrl);
        parcel.writeString(password);
        parcel.writeString(status);
    }


}
