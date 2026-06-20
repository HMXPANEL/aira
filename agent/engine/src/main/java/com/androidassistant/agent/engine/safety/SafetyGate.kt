package com.androidassistant.agent.engine.safety

import com.androidassistant.core.common.Constants
import com.androidassistant.tool.registry.ToolDefinition
import com.androidassistant.tool.registry.ToolPermissionLevel

data class SafetyResult(
    val allowed: Boolean,
    val reason: String? = null,
    val requiresApproval: Boolean = false
)

class SafetyGate {

    private val currentSafetyMode: String
        get() = Constants.DEFAULT_SAFETY_MODE

    private val rateLimitCounters = mutableMapOf<String, MutableList<Long>>()

    fun evaluate(tool: ToolDefinition, args: Map<String, Any>): SafetyResult {
        if (tool.permissionLevel == ToolPermissionLevel.BLOCKED) {
            return SafetyResult(false, "This tool is blocked for safety reasons")
        }

        if (!checkRateLimit(tool.name)) {
            return SafetyResult(false, "Rate limit exceeded for tool: ${tool.name}")
        }

        val needsApproval = when (currentSafetyMode) {
            "trusting" -> tool.permissionLevel == ToolPermissionLevel.CRITICAL
            "balanced" -> tool.permissionLevel >= ToolPermissionLevel.SENSITIVE
            "cautious" -> tool.permissionLevel >= ToolPermissionLevel.NORMAL
            "lockdown" -> tool.permissionLevel >= ToolPermissionLevel.NORMAL
            else -> tool.permissionLevel >= ToolPermissionLevel.SENSITIVE
        }

        return SafetyResult(allowed = true, requiresApproval = needsApproval)
    }

    private fun checkRateLimit(toolName: String): Boolean {
        val now = System.currentTimeMillis()
        val window = 60_000L
        val maxCalls = 10

        val calls = rateLimitCounters.getOrPut(toolName) { mutableListOf() }
        calls.removeAll { now - it > window }
        calls.add(now)

        return calls.size <= maxCalls
    }

    fun resetRateLimits() {
        rateLimitCounters.clear()
    }
}
