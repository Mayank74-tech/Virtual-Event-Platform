package com.java.virtualeventplatform;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.java.virtualeventplatform.adapters.MessageAdapter;
import com.java.virtualeventplatform.models.Message;

import java.util.ArrayList;
import java.util.List;


public class ChatActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;

    private MessageAdapter adapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private String eventId;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ✅ Get eventId from Intent
        eventId = getIntent().getStringExtra("eventId");
        if (eventId == null) {
            Toast.makeText(this, "Error: Event ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();

        recyclerView = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList, currentUserId);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ✅ Load messages in real-time
        loadMessages();

        // ✅ Send message
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    private void loadMessages() {
        CollectionReference messagesRef = db.collection("Events")
                .document(eventId)
                .collection("Messages");

        messagesRef.orderBy("timestamp").addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Toast.makeText(ChatActivity.this, "Error loading messages", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshots != null) {
                messageList.clear();
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    Message msg = dc.getDocument().toObject(Message.class);
                    messageList.add(msg);
                }
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void sendMessage(String text) {
        String senderName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (senderName == null || senderName.isEmpty()) {
            senderName = "Anonymous";
        }

        Message msg = new Message(
                currentUserId,
                senderName,
                text,
                System.currentTimeMillis()
        );

        db.collection("Events")
                .document(eventId)
                .collection("Messages")
                .add(msg)
                .addOnSuccessListener(docRef -> {
                    recyclerView.scrollToPosition(messageList.size() - 1);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChatActivity.this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

}
