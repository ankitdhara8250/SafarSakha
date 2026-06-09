import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlin.multiplatform)// Enables KMP plugin
    alias(libs.plugins.kotlin.compose)// Compose compiler plugin
    alias(libs.plugins.compose.multiplatform)// Enables CMP
    alias(libs.plugins.android.kotlin.multiplatform.library)// Android target for KMP library
    alias(libs.plugins.kotlin.serialization)// Kotlin serialization support
}

kotlin {
    // Android target for KMP library
    androidLibrary {
        namespace = "com.safarsakha.composeapp"
        compileSdk = 36
        minSdk = 24
    }

    iosX64()// iOS simulator Intel target
    iosArm64()// iPhone/iPad target
    iosSimulatorArm64()// iOS simulator Apple Silicon target

    jvm()// Desktop JVM target

    jvmToolchain(17)

    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    jvm {
        mainRun {
            // Runs composeApp as desktop JVM application
            mainClass.set("com.safarsakha.MainKt")
        }
    }

    sourceSets {
        commonMain.dependencies {
            // Compose runtime
            implementation("org.jetbrains.compose.runtime:runtime:1.8.2")
            // Compose layouts
            implementation("org.jetbrains.compose.foundation:foundation:1.8.2")
            // Material3 UI
            implementation("org.jetbrains.compose.material3:material3:1.8.2")
            // Compose UI core
            implementation("org.jetbrains.compose.ui:ui:1.8.2")
            // CMP resources
            implementation("org.jetbrains.compose.components:components-resources:1.8.2")
            // JSON serialization
            implementation(libs.kotlinx.serialization.json)
            // Navigation3 for CMP
            implementation("org.jetbrains.androidx.navigation3:navigation3-ui:1.0.0-alpha05")

            // ViewModel
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

            // Firebase GitLive
            implementation("dev.gitlive:firebase-common:2.1.0")
            implementation("dev.gitlive:firebase-auth:2.1.0")
            implementation("dev.gitlive:firebase-firestore:2.1.0")
            implementation("dev.gitlive:firebase-storage:2.1.0")

            // FileKit Image Picker
            implementation(libs.filekit.core)
            implementation(libs.filekit.compose)

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

            // Datetime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // Coil 3
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.runtime.ktx)

            // Ktor HTTP client for Android (OkHttp)
            implementation(libs.ktor.client.okhttp)

            // Firebase Android SDKs
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.auth)
            implementation(libs.firebase.firestore)
            implementation(libs.firebase.storage)
            implementation(libs.firebase.common.ktx)
        }

        jvmMain.dependencies {
            implementation("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:1.8.2")
            
            // Ktor HTTP client for JVM (Java)
            implementation(libs.ktor.client.java)
        }

        iosMain.dependencies {
            // Ktor HTTP client for iOS (Darwin)
            implementation(libs.ktor.client.darwin)
        }
    }
}
