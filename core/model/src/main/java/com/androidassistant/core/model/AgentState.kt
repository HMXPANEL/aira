package com.androidassistant.core.model

data class AgentState(
    val isProcessing: Boolean = false,
    val currentToolCall: ToolCall? = null,
    val iterationCount: Int = 0,
    val lastError: String? = null,
    val mode: AgentMode = AgentMode.IDLE
)

enum class AgentMode {
    IDLE,
    PROCESSING,
    WAITING_FOR_APPROVAL,
    WAITING_FOR_TOOL,
    ERROR
}

data class DeviceContext(
    val batteryLevel: Int = 0,
    val isCharging: Boolean = false,
    val networkType: String = "unknown",
    val isScreenOn: Boolean = false,
    val foregroundApp: String? = null,
    val activeNotifications: Int = 0,
    val ringerMode: String = "normal",
    val timestamp: Long = 0
)
