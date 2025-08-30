package com.java.virtualeventplatform.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.java.virtualeventplatform.R;
import com.java.virtualeventplatform.models.Message;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messageList.get(position);
        if (msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.bind(msg, currentUserId);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText, senderName, timeText;

        public MessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            messageText = itemView.findViewById(R.id.tvMessage);
            timeText = itemView.findViewById(R.id.tvTime);

            if (viewType == VIEW_TYPE_RECEIVED) {
                senderName = itemView.findViewById(R.id.tvSender);
            }
        }

        public void bind(Message msg, String currentUserId) {
            messageText.setText(msg.getMessage());

            // Format timestamp
            if (msg.getTimestamp() != 0) {
                String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(msg.getTimestamp()));
                timeText.setText(time);
            }

            if (senderName != null) {
                senderName.setText(
                        msg.getSenderId().equals(currentUserId) ? "You" : msg.getSenderName()
                );
            }
        }
    }
}
