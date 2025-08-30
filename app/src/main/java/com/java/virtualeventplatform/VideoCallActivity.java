package com.java.virtualeventplatform;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class VideoCallActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId, currentUserId, eventPassword, hostId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Missing Event ID for video room", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Load event details (password + hostId)
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        eventPassword = doc.getString("password");
                        hostId = doc.getString("hostId");

                        if (currentUserId != null && currentUserId.equals(hostId)) {
                            // ✅ Host: start video directly
                            startVideo();
                        } else {
                            // ✅ Participant: ask for password
                            askForPassword();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void askForPassword() {
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter event password");
        passwordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);

        new AlertDialog.Builder(this)
                .setTitle("Event Password")
                .setMessage("Please enter the event password to join.")
                .setView(passwordInput)
                .setPositiveButton("Join", (dialog, which) -> {
                    String entered = passwordInput.getText().toString().trim();
                    if (entered.isEmpty()) {
                        Toast.makeText(this, "Password cannot be empty!", Toast.LENGTH_SHORT).show();
                        askForPassword(); // show again
                        return;
                    }

                    if (entered.equals(eventPassword)) {
                        startVideo();
                    } else {
                        Toast.makeText(this, "Incorrect Password!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", (d, w) -> finish())
                .show();
    }


    private void startVideo() {
        try {
            URL serverURL = new URL("https://meet.jit.si");

            JitsiMeetConferenceOptions options = new JitsiMeetConferenceOptions.Builder()
                    .setServerURL(serverURL)
                    .setRoom(eventId)   // Room name = eventId
                    .setSubject("Event Video Room")
                    .setFeatureFlag("invite.enabled", false)
                    .setFeatureFlag("chat.enabled", true)
                    .setFeatureFlag("pip.enabled", true)
                    .build();

            JitsiMeetActivity.launch(VideoCallActivity.this, options);
            finish();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
