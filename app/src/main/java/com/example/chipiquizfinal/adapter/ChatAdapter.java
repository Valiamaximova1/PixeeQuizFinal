package com.example.chipiquizfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.models.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String currentUserId;
    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage msg = messages.get(position);
        if (msg.getSenderId().equals(currentUserId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_sent, parent, false);
            return new SentMessageHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            ((SentMessageHolder) holder).bind(message);
        } else {
            ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ViewHolder for sent messages
    static class SentMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        ImageView avatar;

        SentMessageHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.textMessage);
            txtTime = itemView.findViewById(R.id.textTime);
            avatar = itemView.findViewById(R.id.imgAvatar);
        }

        void bind(ChatMessage msg) {
            txtMessage.setText(msg.getMessage());
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(msg.getTimestamp()));
            txtTime.setText(time);
            // hide avatar for sent messages or show user avatar if available
//            if (msg.getAvatarUrl() != null) {
//                avatar.setVisibility(View.VISIBLE);
//                Glide.with(itemView.getContext())
//                        .load(msg.getAvatarUrl())
//                        .circleCrop()
//                        .into(avatar);
//            } else {
//                avatar.setVisibility(View.GONE);
//            }
        }
    }

    // ViewHolder for received messages
    static class ReceivedMessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        ImageView avatar;

        ReceivedMessageHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.textMessage);
            txtTime = itemView.findViewById(R.id.textTime);
            avatar = itemView.findViewById(R.id.imgAvatar);
        }

        void bind(ChatMessage msg) {
            txtMessage.setText(msg.getMessage());
            String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(new Date(msg.getTimestamp()));
            txtTime.setText(time);
            // show friend's avatar
//            if (msg.getAvatarUrl() != null) {
//                avatar.setVisibility(View.VISIBLE);
//                Glide.with(itemView.getContext())
//                        .load(msg.getAvatarUrl())
//                        .circleCrop()
//                        .into(avatar);
//            } else {
//                avatar.setVisibility(View.GONE);
//            }
        }
    }
}

//
//public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
//
//    private List<ChatMessage> messages;
//    private String currentUserId;
//
//    public ChatAdapter(List<ChatMessage> messages, String currentUserId) {
//        this.messages = messages;
//        this.currentUserId = currentUserId;
//    }
//
//    @NonNull
//    @Override
//    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
//        return new MessageViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
//        ChatMessage message = messages.get(position);
//        holder.bind(message, message.getSenderId().equals(currentUserId));
//    }
//
//    @Override
//    public int getItemCount() {
//        return messages.size();
//    }
//
//    static class MessageViewHolder extends RecyclerView.ViewHolder {
//        private TextView textViewMessage;
//        private TextView textViewTimestamp;
//
//        public MessageViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textViewMessage = itemView.findViewById(R.id.textViewMessage);
//            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
//        }
//
//        public void bind(ChatMessage message, boolean isCurrentUser) {
//            textViewMessage.setText(message.getMessage());
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            String time = sdf.format(new Date(message.getTimestamp()));
//            textViewTimestamp.setText(time);
//            if (isCurrentUser) {
//                textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
//            } else {
//                textViewMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
//            }
//        }
//    }
//
//
//
//}
