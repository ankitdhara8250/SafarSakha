plugins {
    alias(libs.plugins.android.application)// Android app plugin
    alias(libs.plugins.kotlin.android)// Kotlin for Android plugin
    alias(libs.plugins.kotlin.compose) // Compose compiler plugin
}

android {
    namespace = "com.safarsakha.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.safarsakha.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Connects shared composeApp module to Android app
    implementation(project(":composeApp"))
    // Android core Kotlin APIs
    implementation(libs.androidx.core.ktx)
    // Needed for setContent { } in MainActivity
    implementation(libs.androidx.activity.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}