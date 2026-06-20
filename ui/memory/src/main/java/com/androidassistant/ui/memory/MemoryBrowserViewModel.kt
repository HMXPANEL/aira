package com.androidassistant.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidassistant.agent.memory.MemoryManager
import com.androidassistant.core.model.Memory
import com.androidassistant.core.model.MemoryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MemoryBrowserUiState(
    val memories: List<Memory> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val selectedType: MemoryType? = null,
    val memoryCount: Int = 0
)

class MemoryBrowserViewModel(
    private val memoryManager: MemoryManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryBrowserUiState())
    val uiState: StateFlow<MemoryBrowserUiState> = _uiState.asStateFlow()

    init {
        loadMemories()
        loadCount()
    }

    fun loadMemories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val memories = memoryManager.getAllSemanticMemories()
            _uiState.update {
                it.copy(memories = memories, isLoading = false)
            }
        }
    }

    fun deleteMemory(memory: Memory) {
        viewModelScope.launch {
            memoryManager.deleteMemory(memory.id, memory.type)
            loadMemories()
            loadCount()
        }
    }

    private fun loadCount() {
        viewModelScope.launch {
            val count = memoryManager.getMemoryCount()
            _uiState.update { it.copy(memoryCount = count) }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
