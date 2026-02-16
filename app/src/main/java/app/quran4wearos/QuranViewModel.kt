package app.quran4wearos

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {
    private val _displayList = MutableStateFlow<List<QuranEntry>>(emptyList())
    val displayList = _displayList.asStateFlow()

    private val pageCache = mutableMapOf<Int, List<QuranEntry>>()

    fun loadFromAyaId(ayaId: Int) {
        viewModelScope.launch {
            val entry = repository.getMetadataById(ayaId)
            entry?.let { loadPageSequentially(it.page) }
        }
    }

    fun loadPageSequentially(targetPage: Int) {
        viewModelScope.launch {
            val currentPageData = pageCache[targetPage] ?: repository.getPage(targetPage)
            pageCache[targetPage] = currentPageData
            println("currentPageData size is ${currentPageData.size}")

            _displayList.value = currentPageData

            launch(Dispatchers.IO) {
                val window = ((targetPage - 2)..(targetPage + 2)).filter { it in 1..604 }
                window.forEach { p ->
                    if (!pageCache.containsKey(p)) pageCache[p] = repository.getPage(p)
                }
                _displayList.value = pageCache.filterKeys { it in window }.values.flatten().sortedBy { it.id }
            }
        }
    }
    var useEmlaey by mutableStateOf(false)
        private set

    // ADD THIS FUNCTION:
    fun toggleScript() {
        println("TOGGLED!!!")
        useEmlaey = !useEmlaey
    }
}
