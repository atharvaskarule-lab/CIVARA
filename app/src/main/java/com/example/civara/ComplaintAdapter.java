package com.example.civara;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> implements Filterable {

    private List<Complaint> complaintList;
    private List<Complaint> complaintListFull;
    private OnComplaintClickListener listener;

    public interface OnComplaintClickListener {
        void onComplaintClick(Complaint complaint);
    }

    public ComplaintAdapter(List<Complaint> complaintList, OnComplaintClickListener listener) {
        this.complaintList = complaintList;
        this.complaintListFull = new ArrayList<>(complaintList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Complaint complaint = complaintList.get(position);

        // Use fallbacks for null values to prevent crashes
        holder.tvType.setText(complaint.getType() != null ? complaint.getType() : "N/A");
        holder.tvDescription.setText(complaint.getDescription() != null ? complaint.getDescription() : "");
        holder.tvDate.setText(complaint.getDate() != null ? complaint.getDate() : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onComplaintClick(complaint);
            }
        });

        String status = complaint.getStatus() != null ? complaint.getStatus() : "Pending";
        holder.tvStatusBadge.setText(status.toUpperCase());

        // Dynamic Badge Styling
        switch (status.toLowerCase()) {
            case "solved":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_solved);
                holder.tvStatusBadge.setTextColor(Color.parseColor("#166534"));
                break;
            case "in progress":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_progress);
                holder.tvStatusBadge.setTextColor(Color.parseColor("#1E40AF"));
                break;
            default:
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_status_pending);
                holder.tvStatusBadge.setTextColor(Color.parseColor("#92400E"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return complaintList != null ? complaintList.size() : 0;
    }

    public void updateList(List<Complaint> newList) {
        this.complaintList = new ArrayList<>(newList);
        this.complaintListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return complaintFilter;
    }

    private final Filter complaintFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Complaint> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(complaintListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Complaint item : complaintListFull) {
                    if ((item.getType() != null && item.getType().toLowerCase().contains(filterPattern)) ||
                            (item.getDescription() != null && item.getDescription().toLowerCase().contains(filterPattern))) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence constraint, FilterResults results) {
            complaintList.clear();
            if (results.values != null) {
                complaintList.addAll((List<Complaint>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDescription, tvStatusBadge, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}