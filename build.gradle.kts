plugins {
    // Define the Android plugin version here so the 'app' module can find it
    id("com.android.application") version "9.0.1" apply false
    id("com.android.library") version "9.0.1" apply false

    // Use a valid Kotlin 2.x version
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false

    // The Compose compiler plugin version must match your Kotlin version
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}