// QuranApplication.kt
package app.quran4wearos

import android.app.Application
import android.content.Context

class QuranApplication : Application() {
    companion object {
        lateinit var instance: QuranApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}