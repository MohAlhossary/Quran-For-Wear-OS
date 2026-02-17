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
import kotlin.collections.filter

class QuranViewModel(val repository: QuranRepository) : ViewModel() {
    private val _displayList = MutableStateFlow<List<QuranEntry>>(emptyList())
    val displayList = _displayList.asStateFlow()

    private val pageCache = mutableMapOf<Int, List<QuranEntry>>()

    var currentlyViewedPage = -1

    fun loadFromAyaId(ayaId: Int) {
        viewModelScope.launch {
            val entry = repository.getMetadataById(ayaId)
            entry?.let {
                val targetPage = entry.id
                val currentPageData = pageCache[targetPage] ?: repository.getPage(targetPage)
                pageCache[targetPage] = currentPageData
                println("currentPageData size is ${currentPageData.size}")

                _displayList.value = currentPageData

                val window = ((targetPage - 1)..(targetPage + 1)).filter { it in 1..604 }
                window.forEach { p ->
                    if (!pageCache.containsKey(p)) pageCache[p] = repository.getPage(p)
                }
                _displayList.value =
                    pageCache.filterKeys { it in window }.values.flatten().sortedBy { it.id }
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

    fun updateAdjacentPages(currentAyaId: Int) {
        viewModelScope.launch {
            val meta = repository.getMetadataById(currentAyaId) ?: return@launch
            val currentPage = meta.page

            if (currentPage != currentlyViewedPage) {
                currentlyViewedPage = currentPage

                viewModelScope.launch(Dispatchers.IO) {
                    val range = (currentPage - 1)..(currentPage + 1)
                    val validatedRange = range.filter { it in 1..604 }

                    val newEntries = mutableListOf<QuranEntry>()
                    for (p in validatedRange) {
                        newEntries.addAll(repository.getPage(p))
                    }

                    // Use distinctBy and sortedBy to ensure the list is clean and ordered
                    val finalSortedList = newEntries
                        .distinctBy { it.id }
                        .sortedBy { it.id }

                    _displayList.value = finalSortedList
                }
            }
        }
    }
}