package com.androidassistant.core.model

data class ToolCall(
    val id: String,
    val name: String,
    val args: Map<String, Any>,
    val status: ToolCallStatus,
    val result: ToolResult? = null,
    val error: String? = null,
    val startedAt: Long? = null,
    val completedAt: Long? = null
)

enum class ToolCallStatus {
    PENDING,
    APPROVED,
    RUNNING,
    SUCCESS,
    ERROR,
    BLOCKED,
    REJECTED
}

data class ToolResult(
    val success: Boolean,
    val data: String? = null,
    val error: String? = null,
    val durationMs: Long = 0
)
