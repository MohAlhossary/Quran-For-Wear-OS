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
        versionCode = 3
        versionName = "1.1.0"
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
    implementation("androidx.wear.compose:compose-material:1.5.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Core Compose via BOM
    val composeBom = platform("androidx.compose:compose-bom:2026.01.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Testing
    debugImplementation("androidx.compose.ui:ui-tooling")
}