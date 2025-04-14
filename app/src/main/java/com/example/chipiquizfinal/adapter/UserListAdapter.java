package com.example.chipiquizfinal.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.entity.User;

import java.util.List;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

    private List<User> users;



    public UserListAdapter(List<User> users) {
        this.users = users;

    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_card, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);

        holder.usernameText.setText(user.getUsername());
        holder.emailText.setText(user.getEmail());
        holder.roleText.setText("Role: " + user.getRole());

        holder.viewEditBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
            View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.dialog_user_details, null);
            builder.setView(dialogView);

            EditText emailField = dialogView.findViewById(R.id.emailField);
            EditText usernameField = dialogView.findViewById(R.id.usernameField);
            EditText languageField = dialogView.findViewById(R.id.languageField);
            EditText passwordField = dialogView.findViewById(R.id.passwordField);
            TextView statsView = dialogView.findViewById(R.id.statsView);

            emailField.setText(user.getEmail());
            usernameField.setText(user.getUsername());
            languageField.setText(user.getLanguage());
            passwordField.setText(user.getPassword());

            statsView.setText("Points: " + user.getPoints() +
                    "\nLives: " + user.getLives() +
                    "\nLevel: " + user.getLevel() +
                    "\nConsecutive days: " + user.getConsecutiveDays());

            builder.setPositiveButton("Save", (dialog, which) -> {
                user.setPassword(passwordField.getText().toString());
                MyApplication.getDatabase().userDao().update(user);
                Toast.makeText(v.getContext(), "Password updated", Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText, emailText, roleText;
        Button viewEditBtn;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.usernameText);
            emailText = itemView.findViewById(R.id.emailText);
            roleText = itemView.findViewById(R.id.roleText);
            viewEditBtn = itemView.findViewById(R.id.viewEditBtn);
        }
    }
}
