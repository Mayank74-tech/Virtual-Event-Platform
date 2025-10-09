package com.java.virtualeventplatform;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private CircleImageView ivProfileAvatar;
    private ImageView ivProfileHeader;
    private EditText tvUserHandle;
    private TextView tvUserName;
    private FloatingActionButton fabEditProfile;
    private MaterialButton btnSignOut;

    private Uri imageUri;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            ivProfileAvatar.setImageURI(imageUri);
                            uploadProfileImage();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        ivProfileAvatar = findViewById(R.id.ivProfileAvatar);
        ivProfileHeader = findViewById(R.id.ivProfileHeader);
        tvUserHandle = findViewById(R.id.tvUserHandle);
        tvUserName = findViewById(R.id.tvUserName);
        fabEditProfile = findViewById(R.id.fabEditProfile);
        btnSignOut = findViewById(R.id.btnSignOut);

        // Load user data
        loadUserData();

        fabEditProfile.setOnClickListener(v -> {
            if (tvUserHandle.isEnabled()) {
                saveProfile();
            } else {
                enableEditing(true);
            }
        });

        ivProfileAvatar.setOnClickListener(v -> openImagePicker());

        btnSignOut.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(this, "Signed out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        DocumentReference userRef = db.collection("Users").document(user.getUid());
        userRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                tvUserName.setText(doc.getString("name"));
                tvUserHandle.setText(doc.getString("bio"));

                String photoUrl = doc.getString("photoUrl");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Picasso.get().load(photoUrl).into(ivProfileAvatar);
                }
            }
        });
    }

    private void enableEditing(boolean enable) {
        tvUserHandle.setEnabled(enable);
        fabEditProfile.setImageResource(enable ? R.drawable.ic_check : R.drawable.ic_edit);
    }

    private void saveProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String newBio = tvUserHandle.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("bio", newBio);

        db.collection("Users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
                    enableEditing(false);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void uploadProfileImage() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || imageUri == null) return;

        StorageReference ref = storage.getReference("profile_pics/" + user.getUid() + ".jpg");
        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    db.collection("Users").document(user.getUid())
                            .update("photoUrl", uri.toString())
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile photo updated!", Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
