package app.quran4wearos

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuranRepository(private val context: Context) {
    private var suraNames: Map<Int, SuraName>? = null
    private var entryPoints: List<QuranListEntry>? = null
    private var metadata: Map<Int, QuranListEntry>? = null
    private var textData: Map<Int, Array<String>>? = null

    private suspend fun ensureLoaded() = withContext(Dispatchers.IO) {
        val parser = QuranFileParser(context)
        if (suraNames == null) suraNames = parser.loadSuraNames()
        if (metadata == null) metadata = parser.loadMetadata()
        if (textData == null) textData = parser.loadTextData()
        if (entryPoints == null) {
            val list = parser.loadEntryPoints()
            list.forEach { entry ->
                val name = suraNames?.get(entry.suraNo)
                entry.suraNameEn = name?.nameEn ?: ""
                entry.suraNameAr = name?.nameAr ?: ""
            }
            entryPoints = list
        }
    }

    suspend fun getPage(pageNo: Int): List<QuranEntry> = withContext(Dispatchers.IO) {
        ensureLoaded()
        // Filter metadata by page, then attach text and sura names
        metadata!!.values.filter { it.page == pageNo }.sortedBy { it.id }.map { meta ->
            val texts = textData?.get(meta.id) ?: arrayOf("", "")
            val sName = suraNames?.get(meta.suraNo)?.nameAr ?: ""
            QuranEntry(meta.id, sName, meta.ayaNo, meta.page, texts[0], texts[1])
        }
    }

    suspend fun getMetadataById(id: Int): QuranListEntry? = withContext(Dispatchers.IO) {
        ensureLoaded()
        metadata!![id]
    }
    fun getMetadataByIdNotSafe(id: Int): QuranListEntry? {
        return metadata!![id]
    }

    suspend fun getNavigationList(): List<QuranListEntry> = withContext(Dispatchers.IO) {
        ensureLoaded()
        entryPoints ?: emptyList()
    }
}