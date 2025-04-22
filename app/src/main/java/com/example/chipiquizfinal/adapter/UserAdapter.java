package com.example.chipiquizfinal.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.chipiquizfinal.activity.ProfileViewActivity;
import com.example.chipiquizfinal.dao.FriendshipDao;
import com.example.chipiquizfinal.entity.Friendship;
import com.example.chipiquizfinal.entity.User;

import java.io.File;
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

    @Override
    public @NonNull VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User user = items.get(position);
        Context ctx = holder.itemView.getContext();

         holder.username.setText(user.getUsername());
        String path = user.getProfileImagePath();
        if (path != null && new File(path).exists()) {
            Glide.with(ctx)
                    .load(new File(path))
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(holder.avatar);
        } else {
            holder.avatar.setImageResource(R.drawable.ic_profile_placeholder);
        }


        holder.profView.setVisibility(View.VISIBLE);
        holder.profView.setOnClickListener(v -> {
            Intent intent = new Intent(ctx, ProfileViewActivity.class);
            intent.putExtra("userId", user.getId());
            ctx.startActivity(intent);
        });


         Friendship fOut = friendshipDao.getFriendship(currentUserId, user.getId());
        Friendship fIn  = friendshipDao.getFriendship(user.getId(), currentUserId);

        if (fIn != null && "PENDING".equals(fIn.getStatus())) {
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setText("Одобри");
            holder.btn.setEnabled(true);
            holder.btn.setOnClickListener(v -> {
                fIn.setStatus("ACCEPTED");
                friendshipDao.update(fIn);
                notifyItemChanged(position);
            });

        } else if (fOut != null && "PENDING".equals(fOut.getStatus())) {
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setText("Изчаква...");
            holder.btn.setEnabled(false);

        } else if ((fIn != null && "ACCEPTED".equals(fIn.getStatus()))
                || (fOut != null && "ACCEPTED".equals(fOut.getStatus()))) {
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setText("Приятели");
            holder.btn.setEnabled(false);

        } else {
            holder.btn.setVisibility(View.VISIBLE);
            holder.btn.setText("Добави приятел");
            holder.btn.setEnabled(true);
            holder.btn.setOnClickListener(v -> {
                Friendship newF = new Friendship();
                newF.setUserId(currentUserId);
                newF.setFriendId(user.getId());
                newF.setStatus("PENDING");
                friendshipDao.insert(newF);
                notifyItemChanged(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<User> users) {
        this.items = users;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username, profView;
        Button btn;

        VH(@NonNull View iv) {
            super(iv);
            avatar   = iv.findViewById(R.id.itemAvatar);
            username = iv.findViewById(R.id.itemUsername);
            btn      = iv.findViewById(R.id.itemBtn);
            profView = iv.findViewById(R.id.viewProf);
        }
    }
}

