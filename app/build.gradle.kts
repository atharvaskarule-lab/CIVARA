plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.civara"
    compileSdk = 35

    buildFeatures {
        buildConfig = true   // 🔥 THIS IS MUST
    }

    defaultConfig {
        applicationId = "com.example.civara"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue(
            "string",
            "GEMINI_API_KEY",
            "AIzaSyCW4ng-9o9brPDmtaZT-L8dagL4vzwZXcY"
        )


        buildConfigField(
            "String",
            "OPENWEATHER_API_KEY",
            "\"${project.findProperty("OPENWEATHER_API_KEY") ?: ""}\""
        )

        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("GEMINI_API_KEY") ?: ""}\""
        )

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

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // AndroidX & UI
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    // Networking (Gemini REST API साठी)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")

    // Location & Images
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.constraintlayout)

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
