package com.example.civara;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> implements Filterable {

    private List<Complaint> complaintList;
    private List<Complaint> complaintListFull;
    private OnComplaintClickListener listener;

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
        void onVoteClick(Complaint complaint);
    }

    public ComplaintAdapter(List<Complaint> complaintList, OnComplaintClickListener listener) {
        this.complaintList = complaintList;
        this.complaintListFull = new ArrayList<>(complaintList);
        this.listener = listener;
    }

    public void updateList(List<Complaint> newList) {
        this.complaintList = new ArrayList<>(newList);
        this.complaintListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint c = complaintList.get(position);

        holder.tvTitle.setText(c.getTitle());
        holder.tvDesc.setText(c.getDescription());
        holder.tvStatus.setText(c.getStatus() != null ? c.getStatus() : "Pending");
        holder.tvDate.setText("Date: " + (c.getDate() != null ? c.getDate() : "N/A"));
        holder.tvSubmittedBy.setText("Submitted by: " + (c.getName() != null ? c.getName() : "Anonymous"));
        
        // VOTE UI
        holder.tvVoteCount.setText(c.getVoteCount() + " votes");
        
        String currentUid = FirebaseAuth.getInstance().getUid();
        if (currentUid != null && c.getVoterIds() != null && c.getVoterIds().contains(currentUid)) {
            holder.btnVote.setText("Voted");
            holder.btnVote.setEnabled(false);
            holder.btnVote.setAlpha(0.6f);
        } else {
            holder.btnVote.setText("Vote");
            holder.btnVote.setEnabled(true);
            holder.btnVote.setAlpha(1.0f);
        }

        // DECODE BASE64 IMAGE
        if (c.getImageUrl() != null && !c.getImageUrl().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(c.getImageUrl(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                holder.ivComplaintImg.setImageBitmap(decodedByte);
                holder.ivComplaintImg.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                holder.ivComplaintImg.setVisibility(View.GONE);
            }
        } else {
            holder.ivComplaintImg.setVisibility(View.GONE);
        }

        // Status Colors
        String status = c.getStatus() != null ? c.getStatus() : "Pending";
        switch (status) {
            case "Resolved": holder.tvStatus.setTextColor(Color.parseColor("#16A34A")); break;
            case "In Progress": holder.tvStatus.setTextColor(Color.parseColor("#2563EB")); break;
            case "Rejected": holder.tvStatus.setTextColor(Color.parseColor("#DC2626")); break;
            default: holder.tvStatus.setTextColor(Color.parseColor("#EA580C")); break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onComplaintClick(c);
        });

        holder.btnVote.setOnClickListener(v -> {
            if (listener != null) listener.onVoteClick(c);
        });
    }

    @Override
    public int getItemCount() { return complaintList.size(); }

    @Override
    public Filter getFilter() { return complaintFilter; }

    private final Filter complaintFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Complaint> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(complaintListFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Complaint item : complaintListFull) {
                    if ((item.getTitle() != null && item.getTitle().toLowerCase().contains(pattern)) ||
                            (item.getName() != null && item.getName().toLowerCase().contains(pattern))) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            complaintList.clear();
            if (results.values != null) {
                complaintList.addAll((List<Complaint>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvStatus, tvDate, tvSubmittedBy, tvVoteCount;
        ImageView ivComplaintImg;
        MaterialButton btnVote;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDesc = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
            tvDate = itemView.findViewById(R.id.tvComplaintDate);
            tvSubmittedBy = itemView.findViewById(R.id.tvSubmittedBy);
            ivComplaintImg = itemView.findViewById(R.id.ivComplaintImage);
            tvVoteCount = itemView.findViewById(R.id.tvVoteCount);
            btnVote = itemView.findViewById(R.id.btnVote);
        }
    }
}