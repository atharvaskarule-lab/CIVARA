package com.example.civara;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;

    private String API_KEY;

    private FusedLocationProviderClient fusedLocationClient;

    private TextView tvCity, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private ProgressBar progressBar;

    private RecyclerView rvForecast;
    private List<ForecastItem> forecastList;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // ✅ INITIALIZE API KEY HERE (ONLY ONCE)
        API_KEY = getString(R.string.openweather_api_key);

        if (API_KEY == null || API_KEY.trim().isEmpty()) {
            Toast.makeText(this, "OpenWeather API key missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        progressBar = findViewById(R.id.progressBar);

        rvForecast = findViewById(R.id.rvForecast);
        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        progressBar.setVisibility(View.VISIBLE);
        getLocationAndWeather();
    }

    private void getLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        loadWeather(location);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Location unavailable. Turn on GPS.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadWeather(Location location) {
        fetchCurrentWeather(location.getLatitude(), location.getLongitude());
        fetchForecast(location.getLatitude(), location.getLongitude());
    }

    // 🌤 Current Weather
    private void fetchCurrentWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + API_KEY
                + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        tvCity.setText(response.getString("name"));

                        JSONObject main = response.getJSONObject("main");
                        tvTemperature.setText(main.getDouble("temp") + "°C");
                        tvHumidity.setText("Humidity: " + main.getInt("humidity") + "%");

                        JSONObject wind = response.getJSONObject("wind");
                        tvWind.setText("Wind: " + wind.getDouble("speed") + " m/s");

                        JSONObject weather = response.getJSONArray("weather").getJSONObject(0);
                        tvDescription.setText(weather.getString("description"));

                        Glide.with(this)
                                .load("https://openweathermap.org/img/wn/"
                                        + weather.getString("icon") + "@2x.png")
                                .into(ivWeatherIcon);

                        progressBar.setVisibility(View.GONE);

                    } catch (JSONException e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this,
                                "Weather parse error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Weather fetch failed",
                            Toast.LENGTH_SHORT).show();
                }
        );

        queue.add(request);
    }

    // 📅 5-Day Forecast
    private void fetchForecast(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/forecast"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&appid=" + API_KEY
                + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray list = response.getJSONArray("list");
                        forecastList.clear();

                        for (int i = 0; i < list.length(); i += 8) {
                            JSONObject obj = list.getJSONObject(i);
                            long time = obj.getLong("dt") * 1000L;

                            String date = new SimpleDateFormat(
                                    "EEE, MMM d", Locale.getDefault())
                                    .format(new Date(time));

                            String temp = String.valueOf(
                                    Math.round(obj.getJSONObject("main")
                                            .getDouble("temp"))
                            );

                            String icon = obj.getJSONArray("weather")
                                    .getJSONObject(0)
                                    .getString("icon");

                            forecastList.add(new ForecastItem(date, icon, temp));
                        }

                        forecastAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Toast.makeText(this,
                                "Forecast parse error",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this,
                        "Forecast fetch failed",
                        Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            progressBar.setVisibility(View.VISIBLE);
            getLocationAndWeather();
        } else {
            Toast.makeText(this,
                    "Location permission denied",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
