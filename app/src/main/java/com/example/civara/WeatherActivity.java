package com.example.civara;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;import android.widget.ImageView;
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
    private String API_KEY; // Will be initialized in onCreate

    private TextView tvCity, tvTemperature, tvDescription, tvHumidity, tvWind;
    private ImageView ivWeatherIcon;
    private RecyclerView rvForecast;
    private ProgressBar progressBar;
    private FusedLocationProviderClient fusedLocationClient;

    private List<ForecastItem> forecastList;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize API Key securely from resources
        API_KEY = getString(R.string.openweather_api_key);
        if (API_KEY.equals("YOUR_ACTUAL_API_KEY_HERE") || API_KEY.trim().isEmpty()) {
            Toast.makeText(this, "API Key is missing. Please add it to strings.xml", Toast.LENGTH_LONG).show();
            finish(); // Close activity if key is missing
            return;
        }

        // Initialize Views
        tvCity = findViewById(R.id.tvCity);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        rvForecast = findViewById(R.id.rvForecast);
        progressBar = findViewById(R.id.progressBar);

        // Setup RecyclerView
        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Start the process by checking permissions
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, get location
            getLocationAndFetchWeather();
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void getLocationAndFetchWeather() {
        progressBar.setVisibility(View.VISIBLE);

        // This check is required by the IDE, even though we just checked for permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // ✅ IMPROVED: Fetch weather directly using coordinates
                fetchWeatherData(latitude, longitude);
                fetchForecastData(latitude, longitude);

            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Location not available. Please turn on GPS and try again.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    // ✅ CHANGED: Method now accepts latitude and longitude
    private void fetchWeatherData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        // Parse current weather data
                        String cityName = response.getString("name");
                        JSONObject main = response.getJSONObject("main");
                        String temperature = String.format(Locale.getDefault(), "%.0f", main.getDouble("temp"));
                        String humidity = main.getString("humidity");
                        JSONObject wind = response.getJSONObject("wind");
                        String windSpeed = wind.getString("speed");
                        JSONArray weatherArray = response.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String description = weather.getString("description");
                        String icon = weather.getString("icon");

                        // Update UI
                        tvCity.setText(cityName);
                        tvTemperature.setText(temperature + "°C");
                        tvDescription.setText(description.toUpperCase());
                        tvHumidity.setText("Humidity: " + humidity + "%");
                        tvWind.setText("Wind: " + windSpeed + " m/s");
                        Glide.with(this).load("https://openweathermap.org/img/wn/" + icon + "@2x.png").into(ivWeatherIcon);

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing weather data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Weather fetch failed";
                    if (error.networkResponse != null) {
                        errorMessage += " (Error: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });

        queue.add(jsonObjectRequest);
    }

    // ✅ CHANGED: Method now accepts latitude and longitude
    private void fetchForecastData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        forecastList.clear();
                        JSONArray list = response.getJSONArray("list");
                        for (int i = 0; i < list.length(); i += 8) { // += 8 to get one forecast per day (every 24 hours)
                            JSONObject forecast = list.getJSONObject(i);
                            long dt = forecast.getLong("dt");
                            Date date = new Date(dt * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                            String dateString = sdf.format(date);

                            JSONObject main = forecast.getJSONObject("main");
                            String temp = String.format(Locale.getDefault(), "%.0f", main.getDouble("temp"));

                            JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
                            String icon = weather.getString("icon");

                            forecastList.add(new ForecastItem(dateString, icon, temp));
                        }
                        forecastAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing forecast data.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMessage = "Forecast fetch failed";
                    if (error.networkResponse != null) {
                        errorMessage += " (Error: " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                });
        queue.add(jsonObjectRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, get location
                getLocationAndFetchWeather();
            } else {
                // Permission denied
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Location permission is required to show weather.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
