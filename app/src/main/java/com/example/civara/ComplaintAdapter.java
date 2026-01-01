package com.example.civara;

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
    private List<Complaint> complaintListFull; // For filtering
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
        this.complaintList = newList;
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
        holder.tvStatus.setText("Status: " + c.getStatus());
        holder.itemView.setOnClickListener(v -> listener.onComplaintClick(c));
    }

    @Override
    public int getItemCount() { return complaintList.size(); }

    @Override
    public Filter getFilter() {
        return complaintFilter;
    }

    private Filter complaintFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Complaint> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(complaintListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (Complaint item : complaintListFull) {
                    if (item.getTitle().toLowerCase().contains(filterPattern)) {
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
            complaintList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDesc, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvComplaintTitle);
            tvDesc = itemView.findViewById(R.id.tvComplaintDesc);
            tvStatus = itemView.findViewById(R.id.tvComplaintStatus);
        }
    }
}