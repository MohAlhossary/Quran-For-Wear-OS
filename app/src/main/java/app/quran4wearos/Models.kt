package app.quran4wearos

data class QuranListEntry(
    val id: Int,
    val jozz: Int,
    val page: Int,
    val suraNo: Int,
    val suraName: String,
    val suraNameAr: String,
    val ayaNo: Int,
    val type: String,
    val text: String? = null,
    val textEmlaey: String? = null
)

data class QuranEntry(
    val id: Int,
    val suraNo: Int,
    val suraName: String,
    val suraNameAr: String,
    val ayaNo: Int,
    val page: Int,
    val text: String,
    val textEmlaey: String
)