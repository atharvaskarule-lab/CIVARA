package com.example.civara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class GarbageAdapter extends RecyclerView.Adapter<GarbageAdapter.ViewHolder> {
    private List<IssueModel> issueList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(IssueModel issue, int position);
    }

    public GarbageAdapter(List<IssueModel> issueList, OnItemClickListener listener) {
        this.issueList = issueList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Aapki request ke mutabik item_complaint inflate kiya gaya hai
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssueModel issue = issueList.get(position);
        holder.tvTitle.setText(issue.getTitle());
        holder.tvDescription.setText(issue.getDescription());
        holder.tvStatus.setText("Status: " + issue.getStatus());

        holder.itemView.setOnClickListener(v -> listener.onItemClick(issue, position));
    }

    @Override
    public int getItemCount() {
        // List ka actual size return karein
        return issueList != null ? issueList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs layout file ke mutabik
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDescription = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
        }
    }
}