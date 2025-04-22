package com.example.chipiquizfinal.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.ChatAdapter;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.models.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//
 public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText messageInput;
    private Button sendButton;

    private FirebaseDatabase database;
    private DatabaseReference chatRef;

    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private int myUserId, otherUserId;
    private String chatId;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerChat);
        messageInput = findViewById(R.id.messageInput);
        sendButton   = findViewById(R.id.sendButton);

        UserDao dao = MyApplication.getDatabase().userDao();
        ChatAdapter adapter = new ChatAdapter(messages, String.valueOf(myUserId), dao);
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        UserDao userDao = MyApplication.getDatabase().userDao();
        User me = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        myUserId = me.getId();
        otherUserId = getIntent().getIntExtra("chatWithUserId", -1);
        chatId = myUserId < otherUserId
                ? myUserId + "_" + otherUserId
                : otherUserId + "_" + myUserId;

        setupHeader(userDao.getUserById(otherUserId));

        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("chats").child(chatId);
        chatRef.addChildEventListener(new ChildEventListener() {


            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatActivity.this,
                        "Chat load failed: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildName) {

            }
            @Override
            public void onChildAdded(DataSnapshot snap, String prevKey) {
                @SuppressWarnings("unchecked")
                Map<String,Object> map = (Map<String,Object>) snap.getValue();
                if (map == null) return;

                Long senderIdL = (Long) map.get("senderId");
                String text    = (String) map.get("text");
                Long tsObj     = (Long) map.get("timestamp");
                long timestamp = tsObj != null ? tsObj : System.currentTimeMillis();

                boolean isMine = senderIdL != null && senderIdL == myUserId;
                ChatMessage cm = new ChatMessage(
                        String.valueOf(senderIdL),
                        text,
                        timestamp,
                        isMine
                );
                messages.add(cm);
                adapter.notifyItemInserted(messages.size() - 1);
                recyclerView.scrollToPosition(messages.size() - 1);
            }


        });

        sendButton.setOnClickListener(v -> {
            String txt = messageInput.getText().toString().trim();
            if (txt.isEmpty()) return;
            Map<String,Object> msg = new HashMap<>();
            msg.put("senderId", myUserId);
            msg.put("text", txt);
            msg.put("timestamp", System.currentTimeMillis());
            chatRef.push().setValue(msg)
                    .addOnSuccessListener(a -> messageInput.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Send failed: "+e.getMessage(),
                                    Toast.LENGTH_SHORT).show());



        });

    }

    private void setupHeader(User other) {
        ImageView headerAvatar = findViewById(R.id.chatHeaderAvatar);
        TextView headerName    = findViewById(R.id.chatHeaderName);

        if (other!=null) {
            headerName.setText(other.getUsername());
            String path = other.getProfileImagePath();
            if (path!=null && !path.isEmpty()) {
                Glide.with(this).load(new File(path))
                        .circleCrop()
                        .into(headerAvatar);
            }
        }
    }
}

