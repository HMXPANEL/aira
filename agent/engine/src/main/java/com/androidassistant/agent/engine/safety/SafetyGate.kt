package com.androidassistant.agent.engine.safety

import com.androidassistant.core.common.Constants
import com.androidassistant.core.model.ApprovalRequest
import com.androidassistant.core.model.ApprovalResult
import com.androidassistant.tool.registry.ToolDefinition
import com.androidassistant.tool.registry.ToolPermissionLevel

data class SafetyResult(
    val allowed: Boolean,
    val reason: String? = null,
    val requiresApproval: Boolean = false,
    val approvalRequest: ApprovalRequest? = null
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

        if (needsApproval) {
            val request = ApprovalRequest(
                id = "approval_${System.currentTimeMillis()}",
                toolName = tool.name,
                toolDescription = tool.description,
                args = args,
                permissionLevel = tool.permissionLevel,
                explanation = generateExplanation(tool, args)
            )
            return SafetyResult(allowed = true, requiresApproval = true, approvalRequest = request)
        }

        return SafetyResult(allowed = true)
    }

    private fun generateExplanation(tool: ToolDefinition, args: Map<String, Any>): String {
        return when (tool.name) {
            "open_app" -> "Opening app: ${args["package_name"]}"
            "set_timer" -> "Setting timer for ${args["duration_seconds"]} seconds"
            "read_notifications" -> "Reading recent notifications"
            "dismiss_notification" -> "Dismissing a notification"
            "click_notification" -> "Clicking a notification action"
            "get_device_info" -> "Reading device information"
            "get_current_time" -> "Getting current time"
            else -> "Executing ${tool.name} with args: $args"
        }
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