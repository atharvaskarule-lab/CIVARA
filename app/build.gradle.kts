plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    // ✅ CORRECTED: Added a newline between these two properties
    namespace = "com.example.civara"
    compileSdk =36

    defaultConfig {
        applicationId = "com.example.civara"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX & UI
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // Only one Firebase BOM is declared to manage versions.
    implementation(platform("com.google.firebase:firebase-bom:34.7.0"))

    // Firebase libraries (versions are now managed by the BOM above)
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")

    // ✅ CORRECTED: Reverted to the hardcoded string because the 'libs' alias was incorrect.
    implementation("com.google.android.gms:play-services-location:21.0.1")
}
