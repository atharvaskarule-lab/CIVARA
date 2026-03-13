package com.example.civara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class GarbageAdapter extends RecyclerView.Adapter<GarbageAdapter.ViewHolder> {
    private List<IssueModel> issueList;
    private OnItemClickListener listener;
    private boolean isAdmin; // Flag to check if current user is admin

    public interface OnItemClickListener {
        void onItemClick(IssueModel issue, int position);
        void onVoteClick(IssueModel issue, int position);
    }

    public GarbageAdapter(List<IssueModel> issueList, OnItemClickListener listener) {
        this.issueList = issueList;
        this.listener = listener;
        this.isAdmin = false; // Default to false
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IssueModel issue = issueList.get(position);

        holder.tvTitle.setText(issue.getTitle());
        holder.tvDescription.setText(issue.getDescription());
        holder.tvStatus.setText(issue.getStatus());

        if (issue.getEmail() != null && !issue.getEmail().isEmpty()) {
            holder.tvSubmittedBy.setText("Submitted by: " + issue.getEmail());
        } else if (issue.getName() != null && !issue.getName().isEmpty()) {
            holder.tvSubmittedBy.setText("Submitted by: " + issue.getName());
        } else {
            holder.tvSubmittedBy.setText("Submitted by: Anonymous");
        }

        // VOTE UI
        holder.tvVoteCount.setText(issue.getVoteCount() + " votes");
        holder.tvVoteCount.setVisibility(View.VISIBLE); // Always visible
        
        if (isAdmin) {
            holder.btnVote.setVisibility(View.GONE); // Admin doesn't need to vote
        } else {
            holder.btnVote.setVisibility(View.VISIBLE);
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid != null && issue.getVoterIds() != null && issue.getVoterIds().contains(currentUid)) {
                holder.btnVote.setText("Voted");
                holder.btnVote.setEnabled(false);
                holder.btnVote.setAlpha(0.6f);
            } else {
                holder.btnVote.setText("Vote");
                holder.btnVote.setEnabled(true);
                holder.btnVote.setAlpha(1.0f);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(issue, position));
        holder.btnVote.setOnClickListener(v -> listener.onVoteClick(issue, position));
    }

    @Override
    public int getItemCount() {
        return issueList != null ? issueList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvSubmittedBy, tvVoteCount;
        MaterialButton btnVote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDescription = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
            tvSubmittedBy = itemView.findViewById(R.id.tvSubmittedBy);
            tvVoteCount = itemView.findViewById(R.id.tvVoteCount);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}