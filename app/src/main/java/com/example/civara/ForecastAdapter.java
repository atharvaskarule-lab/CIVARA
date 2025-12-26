package com.example.civara;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    private List<ForecastItem> forecastList;

    public ForecastAdapter(List<ForecastItem> forecastList) {
        this.forecastList = forecastList;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // We need to create a layout file for the forecast item row.
        // Let's assume it's named 'item_forecast.xml'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_forecast, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastItem item = forecastList.get(position);

        holder.tvDate.setText(item.getDate());
        holder.tvTemp.setText(item.getTemperature() + "°C");

        // Use Glide to load the weather icon
        Glide.with(holder.itemView.getContext())
                .load("https://openweathermap.org/img/wn/" + item.getIcon() + "@2x.png")
                .into(holder.ivIcon);
    }

    @Override
    public int getItemCount() {
        return forecastList.size();
    }

    public static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp;
        ImageView ivIcon;

        public ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvForecastDate);
            tvTemp = itemView.findViewById(R.id.tvForecastTemp);
            ivIcon = itemView.findViewById(R.id.ivForecastIcon);
        }
    }
}
