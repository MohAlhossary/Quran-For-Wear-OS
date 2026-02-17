package app.quran4wearos
data class SuraName(
    val no: Int,
    val nameEn: String,
    val nameAr: String
)

data class QuranListEntry(
    val id: Int,
    val jozz: Int,
    val suraNo: Int,
    val page: Int,
    val lineStart: Int, // Added
    val lineEnd: Int,   // Added
    val ayaNo: Int,
    val type: String,
    val text: String,
    var suraNameEn: String = "",
    var suraNameAr: String = ""
)

data class QuranEntry(
    val id: Int,
    val suraNameAr: String,
    val ayaNo: Int,
    val page: Int,
    val text: String,
    val textEmlaey: String
)