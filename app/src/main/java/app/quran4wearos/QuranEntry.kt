package app.quran4wearos

data class QuranEntry(
    val id: Int,
    val jozz: Int,
    val suraNo: Int,
    val suraName: String,
    val ayaNo: Int,
    val text: String,
    val type: String // "S" for Sura, "J" for Juzz
)