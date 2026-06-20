package com.androidassistant.core.model

data class Memory(
    val id: String,
    val type: MemoryType,
    val content: String,
    val importance: Int,
    val timestamp: Long,
    val metadata: Map<String, String> = emptyMap(),
    val embedding: FloatArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Memory
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

enum class MemoryType {
    EPISODIC,
    SEMANTIC,
    PROCEDURAL
}
