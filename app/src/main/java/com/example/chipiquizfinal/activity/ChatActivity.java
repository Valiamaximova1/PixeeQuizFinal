package com.example.chipiquizfinal.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.ChatAdapter;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.models.ChatMessage;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerChat;
    private EditText messageInput;
    private com.google.android.material.button.MaterialButton sendButton;

    private DatabaseReference chatRef;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private int myUserId;
    private int otherUserId;
    private String chatId;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // 1) UI
        recyclerChat   = findViewById(R.id.recyclerChat);
        messageInput   = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);


        // 2) Разтоваряме ListView и го сменяме с RecyclerView
        recyclerChat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages, String.valueOf(getMyUserId()));
        recyclerChat.setAdapter(chatAdapter);

        // 3) Подготовка на chatId (myId_otherId или обратното)
        myUserId    = getMyUserId();
        otherUserId = getIntent().getIntExtra("chatWithUserId", -1);
        if (otherUserId < 0) {
            Toast.makeText(this, "Несъществуващ приятел", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        chatId = myUserId < otherUserId
                ? myUserId + "_" + otherUserId
                : otherUserId + "_" + myUserId;

        // 4) Настройваме Firebase RTDB
        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(chatId);

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snap, String prevKey) {
                Map<String,Object> map = (Map<String,Object>) snap.getValue();
                if (map == null) return;

                // прочитаме полетата
                long senderIdL = (long) map.get("senderId");
                String text    = (String) map.get("text");
                long ts        = map.containsKey("timestamp")
                        ? (long) map.get("timestamp")
                        : System.currentTimeMillis();

                ChatMessage msg = new ChatMessage();
                msg.setSenderId(String.valueOf(senderIdL));
                msg.setMessage(text);
                msg.setTimestamp(ts);

                messages.add(msg);
                chatAdapter.notifyItemInserted(messages.size() - 1);
                recyclerChat.scrollToPosition(messages.size() - 1);
            }
            @Override public void onChildChanged(DataSnapshot ds, String s) {}
            @Override public void onChildRemoved(DataSnapshot ds) {}
            @Override public void onChildMoved(DataSnapshot ds, String s) {}
            @Override public void onCancelled(DatabaseError err) {
                Toast.makeText(ChatActivity.this,
                        "Chat load failed: " + err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        // 5) Изпращане на съобщения
        sendButton.setOnClickListener(v -> {
            String txt = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(txt)) return;

            Map<String,Object> msg = new HashMap<>();
            msg.put("senderId", myUserId);
            msg.put("text", txt);
            msg.put("timestamp", System.currentTimeMillis());

            chatRef.push().setValue(msg)
                    .addOnSuccessListener(a -> messageInput.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this,
                                    "Send failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private int getMyUserId() {
        // Вземаме логнатия потребител от Room
        UserDao userDao = MyApplication.getDatabase().userDao();
        String email = MyApplication.getLoggedEmail();
        User me = userDao.getUserByEmail(email);
        return me != null ? me.getId() : -1;
    }
}









//package com.example.chipiquizfinal.activity;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.*;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.example.chipiquizfinal.MyApplication;
//import com.example.chipiquizfinal.R;
//import com.example.chipiquizfinal.dao.UserDao;
//import com.example.chipiquizfinal.entity.User;
//import com.google.firebase.database.*;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Map;
//
//public class ChatActivity extends AppCompatActivity {
//    private ListView listViewMessages;
//    private EditText messageInput;
//    private Button sendButton;
//
//    private FirebaseDatabase database;
//    private DatabaseReference chatRef;
//
//    private ArrayList<String> messages = new ArrayList<>();
//    private ArrayAdapter<String> adapter;
//
//    private int myUserId;
//    private int otherUserId;
//    private String chatId;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_chat);
//
//        listViewMessages = findViewById(R.id.listViewMessages);
//        messageInput     = findViewById(R.id.messageInput);
//        sendButton       = findViewById(R.id.sendButton);
//        UserDao userDao = MyApplication.getDatabase().userDao();
//        String myEmail = MyApplication.getLoggedEmail();
//        User me = userDao.getUserByEmail(myEmail);
//        if (me == null) {
//            Toast.makeText(this, "Потребителя не е намерен!", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        myUserId = me.getId();
//        otherUserId = getIntent().getIntExtra("chatWithUserId", -1);
//        if (otherUserId < 0) {
//            Toast.makeText(this, "Несъществуващ приятел", Toast.LENGTH_SHORT).show();
//            finish();
//            return;
//        }
//        if (myUserId < otherUserId) {
//            chatId = myUserId + "_" + otherUserId;
//        } else {
//            chatId = otherUserId + "_" + myUserId;
//        }
//        database = FirebaseDatabase.getInstance();
//        chatRef = database.getReference("chats").child(chatId);
//        adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_list_item_1, messages);
//        listViewMessages.setAdapter(adapter);
//        chatRef.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot snap, String prevKey) {
//                Map<String,Object> map = (Map<String,Object>) snap.getValue();
//                if (map == null) return;
//                Long senderId = (Long) map.get("senderId");
//                String text  = (String) map.get("text");
//                String display = (senderId != null && senderId == myUserId)
//                        ? "Аз: " + text
//                        : "Той/Тя: " + text;
//                messages.add(display);
//                adapter.notifyDataSetChanged();
//                listViewMessages.smoothScrollToPosition(messages.size() - 1);
//            }
//            @Override public void onChildChanged(DataSnapshot ds, String s) {}
//            @Override public void onChildRemoved(DataSnapshot ds) {}
//            @Override public void onChildMoved(DataSnapshot ds, String s) {}
//            @Override public void onCancelled(DatabaseError err) {
//                Toast.makeText(ChatActivity.this,
//                        "Chat load failed: " + err.getMessage(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//        sendButton.setOnClickListener(v -> {
//            String txt = messageInput.getText().toString().trim();
//            if (TextUtils.isEmpty(txt)) return;
//            Map<String,Object> msg = new HashMap<>();
//            msg.put("senderId", myUserId);
//            msg.put("text", txt);
//            chatRef.push().setValue(msg)
//                    .addOnSuccessListener(a -> messageInput.setText(""))
//                    .addOnFailureListener(e ->
//                            Toast.makeText(ChatActivity.this,
//                                    "Send failed: " + e.getMessage(),
//                                    Toast.LENGTH_SHORT).show()
//                    );
//        });
//    }
//}
