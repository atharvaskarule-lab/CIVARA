package com.example.civara;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private ImageButton btnSendMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Ensure this name matches your XML filename exactly!
        setContentView(R.layout.activity_chatbot);

        // Make sure you don't call findViewById for views that don't exist yet
        initViews();
    }

    private void initViews() {
        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        messageList = new ArrayList<>();
    }

    private void setupChat() {
        chatAdapter = new ChatAdapter(messageList);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        // Initial Welcome Message
        addMessage("Hello! I am your Civara Assistant. How can I help you today?", false);
    }

    private void sendMessage(String userMsg) {
        addMessage(userMsg, true);
        etMessage.setText("");

        // Simulate Bot Logic / API Call
        rvChat.postDelayed(() -> {
            String response = getBotResponse(userMsg.toLowerCase());
            addMessage(response, false);
        }, 1000);
    }

    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }

    // This is where you define the "Desire function" logic
    private String getBotResponse(String input) {
        if (input.contains("pothole") || input.contains("complaint")) {
            return "You can report issues in the 'Complaint Portal'. Would you like me to open it for you?";
        } else if (input.contains("sos") || input.contains("danger")) {
            return "If you are in danger, please tap the SOS button on the dashboard immediately!";
        } else if (input.contains("event")) {
            return "You can check the latest city events in the 'Event Calendar' section.";
        } else {
            return "I'm not sure about that. Try asking about complaints, events, or safety.";
        }
    }
}