package com.example.chipiquizfinal.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.dao.UserDao;
import com.example.chipiquizfinal.entity.User;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private ListView listViewMessages;
    private EditText messageInput;
    private Button sendButton;

    private FirebaseDatabase database;
    private DatabaseReference chatRef;

    private ArrayList<String> messages = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    private int myUserId;
    private int otherUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        listViewMessages = findViewById(R.id.listViewMessages);
        messageInput     = findViewById(R.id.messageInput);
        sendButton       = findViewById(R.id.sendButton);

        // Определяме двамата потребители
        UserDao userDao = MyApplication.getDatabase().userDao();
        String myEmail = MyApplication.getLoggedEmail();
        User me = userDao.getUserByEmail(myEmail);
        if (me == null) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        myUserId = me.getId();
        otherUserId = getIntent().getIntExtra("chatWithUserId", -1);
        if (otherUserId < 0) {
            Toast.makeText(this, "Invalid chat partner!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Създаваме стабилен chatId (по-малкоID_по-голямоID)
        if (myUserId < otherUserId) {
            chatId = myUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + myUserId;
        }

        // Настройваме Firebase reference
        database = FirebaseDatabase.getInstance();
        chatRef = database.getReference("chats").child(chatId);

        // Настройваме адаптер за ListView
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, messages);
        listViewMessages.setAdapter(adapter);

        // Слушаме нови съобщения
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snap, String prevKey) {
                Map<String,Object> map = (Map<String,Object>) snap.getValue();
                if (map == null) return;
                Long senderId = (Long) map.get("senderId");
                String text  = (String) map.get("text");
                String display = (senderId != null && senderId == myUserId)
                        ? "Аз: " + text
                        : "Той/Тя: " + text;
                messages.add(display);
                adapter.notifyDataSetChanged();
                listViewMessages.smoothScrollToPosition(messages.size() - 1);
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

        // Изпращане на съобщение
        sendButton.setOnClickListener(v -> {
            String txt = messageInput.getText().toString().trim();
            if (TextUtils.isEmpty(txt)) return;
            Map<String,Object> msg = new HashMap<>();
            msg.put("senderId", myUserId);
            msg.put("text", txt);
            chatRef.push().setValue(msg)
                    .addOnSuccessListener(a -> messageInput.setText(""))
                    .addOnFailureListener(e ->
                            Toast.makeText(ChatActivity.this,
                                    "Send failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        });
    }
}
