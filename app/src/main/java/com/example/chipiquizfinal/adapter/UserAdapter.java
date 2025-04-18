package com.example.chipiquizfinal.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.entity.Friendship;
import com.example.chipiquizfinal.entity.User;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    private List<User> items;
    private final int currentUserId;
    private final FriendshipDao friendshipDao;

    public UserAdapter(List<User> items, int currentUserId, FriendshipDao friendshipDao) {
        this.items = items;
        this.currentUserId = currentUserId;
        this.friendshipDao = friendshipDao;
    }

    public void setItems(List<User> users) {
        this.items = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User user = items.get(position);

        // Username
        holder.username.setText(user.getUsername());

        // Avatar
        String path = user.getProfileImagePath();
        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Glide.with(holder.avatar.getContext())
                        .load(imgFile)
                        .circleCrop()
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_profile_placeholder)
                        .into(holder.avatar);
            } else {
                holder.avatar.setImageResource(R.drawable.ic_profile_placeholder);
            }
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile_placeholder);
        }

        // Friendship status
        List<Friendship> outgoing = friendshipDao.getOutgoing(currentUserId);
        List<Friendship> incoming = friendshipDao.getIncomingRequests(currentUserId);

        Friendship outgoingRel = null;
        for (Friendship f : outgoing) {
            if (f.getFriendId() == user.getId()) {
                outgoingRel = f;
                break;
            }
        }

        Friendship incomingRel = incoming.stream().filter(f -> f.getUserId() == user.getId()).findFirst().orElse(null);

        // Configure button
        if (outgoingRel != null && "ACCEPTED".equals(outgoingRel.status)) {
            holder.btn.setText("Приятели");
            holder.btn.setEnabled(false);

        } else if (incomingRel != null && "PENDING".equals(incomingRel.status)) {
            holder.btn.setText("Одобри");
            holder.btn.setEnabled(true);
            holder.btn.setOnClickListener(v -> {
                incomingRel.status = "ACCEPTED";
                friendshipDao.update(incomingRel);
                notifyItemChanged(position);
            });

        } else {
            holder.btn.setText("Добави приятел");
            holder.btn.setEnabled(true);
            holder.btn.setOnClickListener(v -> {
                Friendship f = new Friendship();
                f.setUserId(currentUserId);
                f.setFriendId(user.getId());
                f.status = "PENDING";
                friendshipDao.insert(f);
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        Button btn;

        VH(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.itemAvatar);
            username = itemView.findViewById(R.id.itemUsername);
            btn = itemView.findViewById(R.id.itemBtn);
        }
    }
}
