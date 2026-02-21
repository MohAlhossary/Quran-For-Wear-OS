import com.android.build.api.dsl.ApplicationExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    // In AGP 9.0, the Compose Compiler is now a plugin
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
}

// 1. Android-specific configuration
extensions.configure<ApplicationExtension> {
    namespace = "app.quran4wearos"
    // THIS FIXES YOUR ERROR: Explicitly defined inside the extension
    compileSdk = 36

    defaultConfig {
        applicationId = "app.quran4wearos"
        minSdk = 30
        targetSdk = 36
        versionCode = 5
        versionName = "1.1.1"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    // Note: composeOptions { kotlinCompilerExtensionVersion } is REMOVED in Kotlin 2.0+
}

// 2. Kotlin-specific configuration (FIXES 'compilerOptions' error)
kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Wear OS specific
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.appcompat)
    implementation(libs.preference)
    implementation(libs.material)


    // Wear Compose
    implementation(libs.compose.material.v130)
    implementation(libs.compose.foundation) // Check for the latest version

    // Horologist Compose Layout
    implementation(libs.horologist.compose.layout)
    implementation(libs.foundation.layout)

    // Core Compose via BOM
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")
    implementation(composeBom)
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)

    // Testing
    debugImplementation(libs.ui.tooling)

    implementation(libs.compose.material.v131)

    // For Compose Material Slider
    implementation(libs.androidx.material)

    // Make sure you have this for Wear Compose foundation
    implementation(libs.androidx.compose.foundation.v131)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // For lifecycle and viewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose.v270)
    implementation(libs.androidx.lifecycle.runtime.ktx)

}