package com.example.civara;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatbotActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private FloatingActionButton btnSendMessage;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(chatAdapter);

        addMessage("Hello! I am Civara Assistant 🤖", false);

        btnSendMessage.setOnClickListener(v -> {
            String userMsg = etMessage.getText().toString().trim();
            if (!userMsg.isEmpty()) {
                askGemini(userMsg);
            }
        });
    }

    private void askGemini(String userMsg) {

        addMessage(userMsg, true);
        etMessage.setText("");

        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject textPart = new JSONObject();
            textPart.put("text", userMsg);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contents);


            RequestBody requestBody =
                    RequestBody.create(body.toString(), JSON);

            String apiKey = getString(R.string.GEMINI_API_KEY);

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            addMessage("❌ Network error", false));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    String res = response.body() != null ? response.body().string() : "";
                    Log.d("GEMINI_RESPONSE", res);

                    try {
                        JSONObject json = new JSONObject(res);

                        JSONArray candidates = json.getJSONArray("candidates");

                        JSONObject content =
                                candidates.getJSONObject(0)
                                        .getJSONObject("content");

                        JSONArray parts =
                                content.getJSONArray("parts");

                        String reply =
                                parts.getJSONObject(0)
                                        .getString("text");

                        runOnUiThread(() ->
                                addMessage(reply, false));

                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() ->
                                addMessage("⚠️ Gemini response error", false));
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void addMessage(String text, boolean isUser) {
        messageList.add(new ChatMessage(text, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rvChat.scrollToPosition(messageList.size() - 1);
    }
}
