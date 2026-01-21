package com.example.civara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage msg = messageList.get(position);
        holder.tvMessage.setText(msg.getMessage());

        holder.tvMessage.setTextAlignment(
                msg.isUser()
                        ? View.TEXT_ALIGNMENT_VIEW_END
                        : View.TEXT_ALIGNMENT_VIEW_START
        );
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(android.R.id.text1);
        }
    }
}
