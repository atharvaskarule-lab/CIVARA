package com.example.civara;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_CODE = 100;
    private String API_KEY;

    private TextView tvCity, tvDate, tvTemperature, tvDescription, tvHumidity, tvWind, tvFeelsLike, tvMaxTemp, tvMinTemp;
    private ImageView ivWeatherIcon, btnRefresh;
    private RecyclerView rvForecast;
    private RelativeLayout loadingLayout;
    private FusedLocationProviderClient fusedLocationClient;

    private List<ForecastItem> forecastList;
    private ForecastAdapter forecastAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        // Initialize API Key
        API_KEY = getString(R.string.openweather_api_key);
        if (API_KEY.equals("YOUR_ACTUAL_API_KEY_HERE") || API_KEY.trim().isEmpty()) {
            Toast.makeText(this, "API Key is missing. Please add it to strings.xml", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Views
        tvCity = findViewById(R.id.tvCity);
        tvDate = findViewById(R.id.tvDate);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvDescription = findViewById(R.id.tvDescription);
        tvHumidity = findViewById(R.id.tvHumidity);
        tvWind = findViewById(R.id.tvWind);
        tvFeelsLike = findViewById(R.id.tvFeelsLike);
        tvMaxTemp = findViewById(R.id.tvMaxTemp);
        tvMinTemp = findViewById(R.id.tvMinTemp);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);
        btnRefresh = findViewById(R.id.btnRefresh);
        rvForecast = findViewById(R.id.rvForecast);
        loadingLayout = findViewById(R.id.loadingLayout);

        // Set current date
        setCurrentDate();

        // Setup RecyclerView
        forecastList = new ArrayList<>();
        forecastAdapter = new ForecastAdapter(forecastList);
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setAdapter(forecastAdapter);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Refresh button click
        btnRefresh.setOnClickListener(v -> {
            // Rotate animation for refresh button
            v.animate().rotation(v.getRotation() + 360f).setDuration(500).start();
            checkLocationPermission();
        });

        // Start the process
        checkLocationPermission();
    }

    private void setCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMM dd", Locale.getDefault());
        tvDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationAndFetchWeather();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        }
    }

    private void getLocationAndFetchWeather() {
        loadingLayout.setVisibility(View.VISIBLE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                fetchWeatherData(latitude, longitude);
                fetchForecastData(latitude, longitude);
            } else {
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(this, "Unable to get location. Please enable GPS.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            loadingLayout.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to get location: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }

    private void fetchWeatherData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + latitude
                + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    loadingLayout.setVisibility(View.GONE);
                    try {
                        // Parse weather data
                        String cityName = response.getString("name");
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        double feelsLike = main.getDouble("feels_like");
                        double tempMax = main.getDouble("temp_max");
                        double tempMin = main.getDouble("temp_min");
                        int humidity = main.getInt("humidity");

                        JSONObject wind = response.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");

                        JSONArray weatherArray = response.getJSONArray("weather");
                        JSONObject weather = weatherArray.getJSONObject(0);
                        String description = weather.getString("description");
                        String icon = weather.getString("icon");

                        // Get country if available
                        String location = cityName;
                        if (response.has("sys")) {
                            JSONObject sys = response.getJSONObject("sys");
                            String country = sys.optString("country", "");
                            if (!country.isEmpty()) {
                                location = cityName + ", " + country;
                            }
                        }

                        // Update UI with smooth animations
                        tvCity.setText(cityName);
                        tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°", temp));
                        tvDescription.setText(capitalizeWords(description));
                        tvHumidity.setText(humidity + "%");
                        tvWind.setText(String.format(Locale.getDefault(), "%.0f m/s", windSpeed));
                        tvFeelsLike.setText(String.format(Locale.getDefault(), "%.0f°", feelsLike));
                        tvMaxTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempMax));
                        tvMinTemp.setText(String.format(Locale.getDefault(), "%.0f°", tempMin));

                        // Load weather icon with higher resolution
                        String iconUrl = "https://openweathermap.org/img/wn/" + icon + "@4x.png";
                        Glide.with(this)
                                .load(iconUrl)
                                .placeholder(android.R.drawable.ic_menu_gallery)
                                .into(ivWeatherIcon);

                        // Animate views
                        animateViews();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing weather data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    loadingLayout.setVisibility(View.GONE);
                    String errorMsg = "Failed to fetch weather data";
                    if (error.networkResponse != null) {
                        errorMsg += " (Error " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
                });

        queue.add(request);
    }

    private void fetchForecastData(double latitude, double longitude) {
        String url = "https://api.openweathermap.org/data/2.5/forecast?lat=" + latitude
                + "&lon=" + longitude + "&appid=" + API_KEY + "&units=metric";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        forecastList.clear();
                        JSONArray list = response.getJSONArray("list");

                        // Get one forecast per day (every 8th item = 24 hours)
                        for (int i = 0; i < list.length() && i < 40; i += 8) {
                            JSONObject forecast = list.getJSONObject(i);

                            long dt = forecast.getLong("dt");
                            Date date = new Date(dt * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());
                            String dateString = sdf.format(date);

                            JSONObject main = forecast.getJSONObject("main");
                            double temp = main.getDouble("temp");

                            JSONObject weather = forecast.getJSONArray("weather").getJSONObject(0);
                            String icon = weather.getString("icon");

                            forecastList.add(new ForecastItem(
                                    dateString,
                                    icon,
                                    String.format(Locale.getDefault(), "%.0f°", temp)
                            ));
                        }

                        forecastAdapter.notifyDataSetChanged();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing forecast data", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    String errorMsg = "Failed to fetch forecast";
                    if (error.networkResponse != null) {
                        errorMsg += " (Error " + error.networkResponse.statusCode + ")";
                    }
                    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
                });

        queue.add(request);
    }

    private void animateViews() {
        // Fade in animation for main content
        tvTemperature.setAlpha(0f);
        tvDescription.setAlpha(0f);
        ivWeatherIcon.setAlpha(0f);

        tvTemperature.animate().alpha(1f).setDuration(600).start();
        tvDescription.animate().alpha(1f).setDuration(600).setStartDelay(100).start();
        ivWeatherIcon.animate().alpha(1f).setDuration(600).setStartDelay(200).start();
    }

    private String capitalizeWords(String text) {
        if (text == null || text.isEmpty()) return text;

        String[] words = text.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                loadingLayout.setVisibility(View.GONE);
                Toast.makeText(this, "Location permission is required to show weather", Toast.LENGTH_LONG).show();
            }
        }
    }
}