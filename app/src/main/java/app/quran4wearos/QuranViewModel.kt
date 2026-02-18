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

class QuranViewModel(val repository: QuranRepository) : ViewModel() {
    private val _displayList = MutableStateFlow<List<QuranEntry>>(emptyList())
    val displayList = _displayList.asStateFlow()

    private val _targetInitialIndex = MutableStateFlow(-1)
    val targetInitialIndex = _targetInitialIndex.asStateFlow()

    private val pageCache = mutableMapOf<Int, List<QuranEntry>>()

    var currentlyViewedPage by mutableStateOf(-1)
    var useEmlaey by mutableStateOf(false)
        private set

    fun loadFromAyaId(ayaId: Int) {
        viewModelScope.launch {
            val entry = repository.getMetadataById(ayaId)
            entry?.let {
                loadPageWindow(entry.page)

                // Find and store the index of the target aya
                val targetIndex = _displayList.value.indexOfFirst { it.id == ayaId }
                if (targetIndex != -1) {
                    _targetInitialIndex.value = targetIndex
                }
            }
        }
    }

    fun updateAdjacentPages(currentAyaId: Int) {
        viewModelScope.launch {
            val meta = repository.getMetadataById(currentAyaId) ?: return@launch
            val currentPage = meta.page

            if (currentPage != currentlyViewedPage) {
                currentlyViewedPage = currentPage
                loadPageWindow(currentPage)
            }
        }
    }

    private suspend fun loadPageWindow(centerPage: Int) {
        val range = (centerPage - 1)..(centerPage + 1)
        val validatedRange = range.filter { it in 1..604 }

        val newEntries = mutableListOf<QuranEntry>()
        for (page in validatedRange) {
            // Use cached data if available, otherwise load
            val pageData = pageCache.getOrPut(page) { repository.getPage(page) }
            newEntries.addAll(pageData)
        }

        // Ensure the list is clean and ordered
        _displayList.value = newEntries
            .distinctBy { it.id }
            .sortedBy { it.id }
    }

    fun toggleScript() {
        println("TOGGLED!!!")
        useEmlaey = !useEmlaey
    }

    fun clearTargetIndex() {
        _targetInitialIndex.value = -1
    }

    fun clearCache() {
        pageCache.clear()
        currentlyViewedPage = -1
    }
}