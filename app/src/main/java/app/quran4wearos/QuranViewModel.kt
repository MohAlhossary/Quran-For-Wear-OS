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

    // Add this to track the target index for initial positioning
    private val _targetInitialIndex = MutableStateFlow(-1)
    val targetInitialIndex = _targetInitialIndex.asStateFlow()

    private val pageCache = mutableMapOf<Int, List<QuranEntry>>()

    var currentlyViewedPage = -1

    fun loadFromAyaId(ayaId: Int) {
        viewModelScope.launch {
            val entry = repository.getMetadataById(ayaId)
            entry?.let {
                val targetPage = entry.page

                // Load the 3-page window immediately
                val window = ((targetPage - 1)..(targetPage + 1)).filter { it in 1..604 }
                val allEntries = mutableListOf<QuranEntry>()

                for (p in window) {
                    val pageData = pageCache[p] ?: repository.getPage(p)
                    pageCache[p] = pageData
                    allEntries.addAll(pageData)
                }

                // Sort all entries
                val sortedEntries = allEntries.distinctBy { it.id }.sortedBy { it.id }
                _displayList.value = sortedEntries

                // Find and store the index of the target aya
                val targetIndex = sortedEntries.indexOfFirst { it.id == ayaId }
                if (targetIndex != -1) {
                    _targetInitialIndex.value = targetIndex
                }
            }
        }
    }

    var useEmlaey by mutableStateOf(false)
        private set

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

                val range = (currentPage - 1)..(currentPage + 1)
                val validatedRange = range.filter { it in 1..604 }

                val newEntries = mutableListOf<QuranEntry>()
                for (p in validatedRange) {
                    // Use cached data if available, otherwise load
                    val pageData = pageCache[p] ?: repository.getPage(p)
                    pageCache[p] = pageData
                    newEntries.addAll(pageData)
                }

                // Ensure the list is clean and ordered
                val finalSortedList = newEntries
                    .distinctBy { it.id }
                    .sortedBy { it.id }

                _displayList.value = finalSortedList
            }
        }
    }

    // Add this function to clear the target index after use
    fun clearTargetIndex() {
        _targetInitialIndex.value = -1
    }
}