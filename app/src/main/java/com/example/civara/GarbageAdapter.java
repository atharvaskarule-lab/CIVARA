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
        // item_complaint layout inflate ho raha hai
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssueModel issue = issueList.get(position);

        // Basic data binding
        holder.tvTitle.setText(issue.getTitle());
        holder.tvDescription.setText(issue.getDescription());
        holder.tvStatus.setText("Status: " + issue.getStatus());

        // Logic: Email -> Name -> Anonymous (Jo aapne manga tha)
        if (issue.getEmail() != null && !issue.getEmail().isEmpty()) {
            holder.tvSubmittedBy.setText("Submitted by: " + issue.getEmail());
        } else if (issue.getName() != null && !issue.getName().isEmpty()) {
            holder.tvSubmittedBy.setText("Submitted by: " + issue.getName());
        } else {
            holder.tvSubmittedBy.setText("Submitted by: Anonymous");
        }

        // Item click listener
        holder.itemView.setOnClickListener(v -> listener.onItemClick(issue, position));
    }

    @Override
    public int getItemCount() {
        return issueList != null ? issueList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvSubmittedBy;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // IDs layout file (XML) ke mutabik link ho rahi hain
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDescription = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
            tvSubmittedBy = itemView.findViewById(R.id.tvSubmittedBy);
        }
    }
}