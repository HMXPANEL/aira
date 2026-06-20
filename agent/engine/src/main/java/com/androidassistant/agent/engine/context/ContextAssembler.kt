package com.androidassistant.agent.engine.context

import com.androidassistant.core.model.DeviceContext

class ContextAssembler {

    companion object {
        private const val SYSTEM_PROMPT_TEMPLATE = """
You are an Android AI Assistant that lives on the user's device.
You can understand natural language, remember context, and use tools to perform actions on the device.

CAPABILITIES:
- Answer questions about the device and system
- Read and manage notifications
- Open apps and control device settings
- Remember information across conversations
- Execute multi-step tasks

CONSTRAINTS:
- Always explain what you're about to do before doing it
- Never perform destructive actions without explicit user approval
- If a task is ambiguous, ask clarifying questions
- Be concise and helpful in your responses
- Respect user privacy at all times

DEVICE CONTEXT:
- Battery: {battery_level}%{charging_status}
- Network: {network_type}
- Screen: {screen_state}
- Time: {current_time}

CURRENT DEVICE STATE:
- Foreground app: {foreground_app}
- Active notifications: {notification_count}
- Ringer mode: {ringer_mode}

RULES:
- Maximum {max_iterations} reasoning iterations per task
- Use tools when you need to interact with the device
- If a tool fails, explain the error and suggest alternatives
- Ask before sending messages, making calls, or modifying data
"""
    }

    fun buildSystemPrompt(
        deviceContext: DeviceContext,
        sessionId: String
    ): String {
        return SYSTEM_PROMPT_TEMPLATE
            .replace("{battery_level}", deviceContext.batteryLevel.toString())
            .replace("{charging_status}", if (deviceContext.isCharging) " (charging)" else "")
            .replace("{network_type}", deviceContext.networkType)
            .replace("{screen_state}", if (deviceContext.isScreenOn) "On" else "Off")
            .replace("{current_time}", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()))
            .replace("{foreground_app}", deviceContext.foregroundApp ?: "Unknown")
            .replace("{notification_count}", deviceContext.activeNotifications.toString())
            .replace("{ringer_mode}", deviceContext.ringerMode)
            .replace("{max_iterations}", com.androidassistant.core.common.Constants.MAX_AGENT_ITERATIONS.toString())
    }
}
