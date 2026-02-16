package app.quran4wearos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {
    private var textMaps: QuranFileParser.QuranDataMaps? = null
    private var metadataMap: Map<Int, QuranListEntry>? = null

    private suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        val parser = QuranFileParser(context)
        if (textMaps == null) textMaps = parser.loadAllTextData("quran_data.csv")
        if (metadataMap == null) metadataMap = parser.loadMetadata("quran_metadata.csv")
    }

    suspend fun getPage(pageNo: Int): List<QuranEntry> = withContext(Dispatchers.IO) {
        ensureLoaded()
        metadataMap!!.values.filter { it.page == pageNo }.map { meta ->
            QuranEntry(
                meta.id, meta.suraNo, meta.suraName, meta.suraNameAr, meta.ayaNo, meta.page,
                textMaps?.textMap?.get(meta.id) ?: "",
                textMaps?.emlaeyMap?.get(meta.id) ?: ""
            )
        }
    }

    suspend fun getMetadataById(id: Int): QuranListEntry? = withContext(Dispatchers.IO) {
        ensureLoaded()
        metadataMap?.get(id)
    }

    suspend fun getNavigationList(): List<QuranListEntry> = withContext(Dispatchers.IO) {
        ensureLoaded()
        metadataMap!!.values.filter { it.type == "S" || it.type == "J" }.sortedBy { it.id }
    }
}