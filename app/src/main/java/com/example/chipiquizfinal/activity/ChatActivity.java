package com.example.chipiquizfinal.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chipiquizfinal.R;
import com.example.chipiquizfinal.adapter.ChatAdapter;
import com.example.chipiquizfinal.AppDatabase;
import com.example.chipiquizfinal.entity.User;
import com.example.chipiquizfinal.models.ChatMessage;
import com.example.chipiquizfinal.MyApplication;
import com.example.chipiquizfinal.dao.UserDao;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private RecyclerView chatRecyclerView;
    private EditText editTextMessage;
    private Button buttonSend;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private DatabaseReference databaseReference;

    private User currentUser;
    private String currentUserId; // Ще бъде зададен от currentUser.getId()

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        FirebaseDatabase.getInstance().getReference("test")
                .setValue("Hello, Test!")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("Test", "Записът в 'test' е успешен");
                    } else {
                        Log.e("Test", "Грешка при запис в 'test': " + task.getException());
                    }
                });



        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Log.d("ChatActivity", "FirebaseDatabase instance: " + database);

        // Извличане на текущия потребител от Room базата
        UserDao userDao = MyApplication.getDatabase().userDao();
        // Увери се, че преди това вече е зададен логнат имейл чрез MyApplication.setLoggedEmail(…) (например при логин)
        currentUser = userDao.getUserByEmail(MyApplication.getLoggedEmail());
        if (currentUser != null) {
            currentUserId = String.valueOf(currentUser.getId());
        } else {
            currentUserId = "unknown";
            Log.e(TAG, "Текущият потребител не е намерен! Проверете MyApplication.getLoggedEmail()");
        }


        // Инициализация на UI елементите
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Инициализация на списъка със съобщения и адаптера
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Инициализация на Firebase Database референтата към "chatMessages"
        databaseReference = FirebaseDatabase.getInstance().getReference("chatMessages");
        Log.d("ChatActivity", "databaseReference: " + databaseReference);

        // Зареждане на съобщения от Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    ChatMessage message = messageSnapshot.getValue(ChatMessage.class);
                    if (message != null) {
                        chatMessages.add(message);
                    }
                }
                chatAdapter.notifyDataSetChanged();
                chatRecyclerView.smoothScrollToPosition(chatMessages.size());
                Log.d(TAG, "Съобщения обновени: " + chatMessages.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Грешка при зареждане на съобщения: " + error.getMessage());
            }
        });


        // Слушател за бутона за изпращане
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    /**
     * Функция за изпращане на съобщение.
     * Изпраща съобщението към Firebase и показва Toast при успех.
     */
    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            Toast.makeText(this, "Въведи съобщение", Toast.LENGTH_SHORT).show();
            return;
        }
        // Създаване на ChatMessage обект
        ChatMessage message = new ChatMessage(currentUserId, messageText, System.currentTimeMillis());

        // Изпращане на съобщението към Firebase
        databaseReference.push().setValue(message)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Съобщението е изпратено успешно", Toast.LENGTH_SHORT).show();
                        Log.d("ChatActivity", "Съобщението е изпратено успешно");
                    } else {
                        Toast.makeText(ChatActivity.this, "Грешка при изпращане", Toast.LENGTH_SHORT).show();
                        Log.e("ChatActivity", "Грешка при изпращане: " + task.getException());
                    }
                });
        editTextMessage.setText(""); // Изчистваме полето
    }

}
