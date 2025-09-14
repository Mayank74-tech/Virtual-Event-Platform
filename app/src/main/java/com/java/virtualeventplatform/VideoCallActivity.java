package com.java.virtualeventplatform;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;

public class VideoCallActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String eventId, currentUserId, eventPassword, hostId;

    private RtcEngine rtcEngine;
    private static final String APP_ID = "aee0d08cdbf34d7596f451eed8007c6a"; // copy from console

    private FrameLayout localContainer, remoteContainer;

    // Agora event handler
    private final IRtcEngineEventHandler mHandler = new IRtcEngineEventHandler() {
        @Override
        public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }

        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            runOnUiThread(() ->
                    Toast.makeText(VideoCallActivity.this,
                            "Joined channel: " + channel, Toast.LENGTH_SHORT).show());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        // find UI containers
        localContainer = findViewById(R.id.local_video_container);
        remoteContainer = findViewById(R.id.remote_video_container);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        eventId = getIntent().getStringExtra("eventId");

        if (eventId == null) {
            Toast.makeText(this, "Missing Event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load event details from Firestore
        db.collection("Events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        eventPassword = doc.getString("password");
                        hostId = doc.getString("hostId");

                        if (currentUserId != null && currentUserId.equals(hostId)) {
                            startVideo(true);
                        } else {
                            askForPassword();
                        }
                    } else {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void askForPassword() {
        EditText passwordInput = new EditText(this);
        passwordInput.setHint("Enter event password");

        new AlertDialog.Builder(this)
                .setTitle("Event Password")
                .setView(passwordInput)
                .setPositiveButton("Join", (dialog, which) -> {
                    String entered = passwordInput.getText().toString().trim();
                    if (entered.equals(eventPassword)) {
                        startVideo(false);
                    } else {
                        Toast.makeText(this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", (d, w) -> finish())
                .show();
    }

    private void startVideo(boolean isHost) {
        try {
            rtcEngine = RtcEngine.create(getBaseContext(), APP_ID, mHandler);

            // enable video
            rtcEngine.enableVideo();

            // setup local video
            SurfaceView localView = RtcEngine.CreateRendererView(getBaseContext());
            localView.setZOrderMediaOverlay(true);
            localContainer.addView(localView);
            rtcEngine.setupLocalVideo(new VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0));

            // join channel
            ChannelMediaOptions options = new ChannelMediaOptions();
            options.clientRoleType = isHost ? 1 : 2; // 1 = host, 2 = audience
            rtcEngine.joinChannel(null, eventId, 0, options);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Agora init failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupRemoteVideo(int uid) {
        if (remoteContainer.getChildCount() > 0) return;

        SurfaceView remoteView = RtcEngine.CreateRendererView(getBaseContext());
        remoteContainer.addView(remoteView);
        rtcEngine.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtcEngine != null) {
            rtcEngine.leaveChannel();
            RtcEngine.destroy();
            rtcEngine = null;
        }
    }
}
