package com.example.civara; // Use your actual package name

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Optional: Make the splash screen full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        // Delay for 3 seconds (3000 milliseconds)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Intent to move from SplashActivity to MainActivity
                Intent intent = new Intent(SplashActivity.this, HomepageActivity.class);
                startActivity(intent);

                // Close this activity so the user can't go back to it
                finish();
            }
        }, 3000);
    }
}