// Top-level build file
plugins {
    // Ensure the version matches your Android Studio version
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}