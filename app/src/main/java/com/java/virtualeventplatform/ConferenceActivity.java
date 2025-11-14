package com.java.virtualeventplatform;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceConfig;
import com.zegocloud.uikit.prebuilt.videoconference.ZegoUIKitPrebuiltVideoConferenceFragment;

import java.util.Random;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoScenario;
import im.zego.zegoexpress.entity.ZegoAudioConfig;
import im.zego.zegoexpress.constants.ZegoAudioConfigPreset;
import im.zego.zegoexpress.constants.ZegoAudioChannel;
import im.zego.zegoexpress.constants.ZegoAudioCodecID;
import im.zego.zegoexpress.constants.ZegoAECMode;

public class ConferenceActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    // Replace with your AppID & full AppSign (128-character) from ZEGOCLOUD console
    private static final long APP_ID = 1336130428L;
    private static final String APP_SIGN = "0d0c5ec3e99d06a5728984507937f1e77eaa248ab30b49011fd48de81f9a1efa";

    private ZegoExpressEngine engine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conference);

        // ask permissions then init
        checkAndRequestPermissions();
    }

    private void initZegoEngineAndAudio() {
        // create singleton engine (multiple calls return same instance)
        engine = ZegoExpressEngine.createEngine(
                APP_ID,
                APP_SIGN,
                true,                     // isTestEnv (set false for production)
                ZegoScenario.COMMUNICATION,
                getApplication(),
                null
        );

        // Configure audio using the correct API and enums.
        // You can choose a preset (BASIC/STANDARD/HIGH and the STEREO variants)
        ZegoAudioConfig audioConfig = new ZegoAudioConfig(ZegoAudioConfigPreset.STANDARD_QUALITY);

        // Tune channel/codec/bitrate if you want:
        audioConfig.channel = ZegoAudioChannel.MONO;           // or STEREO
        audioConfig.codecID = ZegoAudioCodecID.DEFAULT;       // DEFAULT / NORMAL / LOW / ...
        audioConfig.bitrate = 48000 / 1000; // example (note: SDK expects kbps-ish values; choose per preset)

        engine.setAudioConfig(audioConfig);    // apply the audio config
        engine.enableAEC(true);                // echo cancellation
        engine.setAECMode(ZegoAECMode.MEDIUM); // set AEC mode (AGGRESSIVE / MEDIUM / SOFT)
        engine.enableAGC(true);                // automatic gain control
        engine.enableANS(true);                // noise suppression
    }

    private void addFragment() {
        String conferenceID = "test_conference_id";
        String userID = generateUserID();
        String userName = userID + "_Name";

        ZegoUIKitPrebuiltVideoConferenceConfig config = new ZegoUIKitPrebuiltVideoConferenceConfig();

        ZegoUIKitPrebuiltVideoConferenceFragment fragment =
                ZegoUIKitPrebuiltVideoConferenceFragment.newInstance(
                        APP_ID, APP_SIGN, userID, userName, conferenceID, config);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }

    private boolean checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
        };

        boolean allGranted = true;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            // init engine and audio BEFORE adding the fragment so settings take effect
            initZegoEngineAndAudio();
            addFragment();
            return true;
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean granted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                initZegoEngineAndAudio();
                addFragment();
            } else {
                // inform user
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // destroy engine properly when your app no longer needs it
        if (engine != null) {
            // Use Zego's destroy API (async) if available, or call destroy() per SDK doc
            ZegoExpressEngine.destroyEngine(null);
            engine = null;
        }
    }

    private String generateUserID() {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        while (builder.length() < 5) {
            int nextInt = random.nextInt(10);
            if (builder.length() == 0 && nextInt == 0) continue;
            builder.append(nextInt);
        }
        return builder.toString();
    }
}