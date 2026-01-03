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
        holder.tvDate.setText("Submitted: " + (c.getDate() != null ? c.getDate() : "N/A"));

        // Modern Color Coding for Admin
        String status = c.getStatus() != null ? c.getStatus() : "Pending";
        switch (status) {
            case "Resolved":
                holder.tvStatus.setTextColor(Color.parseColor("#16A34A")); // Success Green
                break;
            case "In Progress":
                holder.tvStatus.setTextColor(Color.parseColor("#2563EB")); // Primary Blue
                break;
            case "Rejected":
                holder.tvStatus.setTextColor(Color.parseColor("#DC2626")); // Error Red
                break;
            default: // Pending
                holder.tvStatus.setTextColor(Color.parseColor("#EA580C")); // Warning Orange
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onComplaintClick(c);
        });
    }

    @Override
    public int getItemCount() { return complaintList.size(); }

    @Override
    public Filter getFilter() { return complaintFilter; }

    private Filter complaintFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Complaint> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(complaintListFull);
            } else {
                String pattern = constraint.toString().toLowerCase().trim();
                for (Complaint item : complaintListFull) {
                    if (item.getTitle().toLowerCase().contains(pattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults res = new FilterResults();
            res.values = filteredList;
            return res;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            complaintList.clear();
            complaintList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvStatus, tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDesc = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
            tvDate = itemView.findViewById(R.id.tvComplaintDate);
        }
    }
}