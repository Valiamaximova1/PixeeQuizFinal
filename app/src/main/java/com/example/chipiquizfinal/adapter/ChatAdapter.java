package com.example.chipiquizfinal.adapter;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.models.ChatMessage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;
    private final String currentUserId;
    private final UserDao userDao;

    public ChatAdapter(List<ChatMessage> messages, String currentUserId, UserDao userDao) {
        this.messages      = messages;
        this.currentUserId = currentUserId;
        this.userDao       = userDao;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_bubble, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        boolean isMine = message.getSenderId().equals(currentUserId);
        User sender = userDao.getUserById(Integer.parseInt(message.getSenderId()));

        holder.textViewMessage.setText(message.getMessage());
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(new Date(message.getTimestamp()));
        holder.textViewTimestamp.setText(time);


        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) holder.bubbleCard.getLayoutParams();
        lp.gravity = isMine ? Gravity.END : Gravity.START;
        holder.bubbleCard.setLayoutParams(lp);

        // показваме аватар само за другите
        if (isMine) {
            holder.avatar.setVisibility(View.GONE);
            holder.bubbleCard.setCardBackgroundColor(
                    holder.itemView.getContext()
                            .getColor(R.color.purple_dark)  // твой цвят за мои съобщения
            );
        } else {
            holder.avatar.setVisibility(View.VISIBLE);
            holder.bubbleCard.setCardBackgroundColor(
                    holder.itemView.getContext()
                            .getColor(R.color.white)  // мой цвят за чужди
            );
            // зареждаме снимка, ако имаш път към локалния файл:
            String path = sender != null ? sender.getProfileImagePath() : null;
            if (path != null && !path.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                        .load(new File(path))
                        .circleCrop()
                        .into(holder.avatar);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        FrameLayout bubbleContainer;
        com.google.android.material.card.MaterialCardView bubbleCard;
        ImageView avatar;
        TextView textViewMessage, textViewTimestamp;

        public MessageViewHolder(@NonNull View iv) {
            super(iv);
            bubbleContainer = iv.findViewById(R.id.bubbleContainer);
            bubbleCard      = iv.findViewById(R.id.bubbleCard);
            avatar          = iv.findViewById(R.id.avatar);
            textViewMessage = iv.findViewById(R.id.textViewMessage);
            textViewTimestamp= iv.findViewById(R.id.textViewTimestamp);
        }
    }
}
