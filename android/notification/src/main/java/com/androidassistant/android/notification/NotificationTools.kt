package com.androidassistant.android.notification

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.androidassistant.core.model.ToolResult
import com.androidassistant.tool.registry.ToolCategory
import com.androidassistant.tool.registry.ToolDefinition
import com.androidassistant.tool.registry.ToolParameter
import com.androidassistant.tool.registry.ToolParameterType
import com.androidassistant.tool.registry.ToolPermissionLevel

class NotificationTools(private val context: Context) {

    fun getReadNotificationsTool(): ToolDefinition {
        return ToolDefinition(
            name = "read_notifications",
            description = "Read recent notifications from the device",
            category = ToolCategory.NOTIFICATION,
            permissionLevel = ToolPermissionLevel.NORMAL,
            parameters = listOf(
                ToolParameter(
                    name = "limit",
                    type = ToolParameterType.INTEGER,
                    description = "Maximum number of notifications to return (default: 20)",
                    required = false
                ),
                ToolParameter(
                    name = "package_filter",
                    type = ToolParameterType.STRING,
                    description = "Optional package name to filter notifications (e.g., com.whatsapp)",
                    required = false
                )
            ),
            executor = { args ->
                val limit = (args["limit"] as? Number)?.toInt() ?: 20
                val packageFilter = args["package_filter"] as? String
                readNotifications(limit, packageFilter)
            }
        )
    }

    fun getDismissNotificationTool(): ToolDefinition {
        return ToolDefinition(
            name = "dismiss_notification",
            description = "Dismiss a specific notification by its key",
            category = ToolCategory.NOTIFICATION,
            permissionLevel = ToolPermissionLevel.NORMAL,
            parameters = listOf(
                ToolParameter(
                    name = "key",
                    type = ToolParameterType.STRING,
                    description = "The notification key to dismiss",
                    required = true
                )
            ),
            executor = { args ->
                val key = args["key"] as? String ?: return@ToolDefinition ToolResult(success = false, error = "Missing key parameter")
                dismissNotification(key)
            }
        )
    }

    fun getClickNotificationTool(): ToolDefinition {
        return ToolDefinition(
            name = "click_notification",
            description = "Click/tap a notification action",
            category = ToolCategory.NOTIFICATION,
            permissionLevel = ToolPermissionLevel.NORMAL,
            parameters = listOf(
                ToolParameter(
                    name = "key",
                    type = ToolParameterType.STRING,
                    description = "The notification key to click",
                    required = true
                ),
                ToolParameter(
                    name = "action_index",
                    type = ToolParameterType.INTEGER,
                    description = "Index of the action to click (default: 0 for first action)",
                    required = false
                )
            ),
            executor = { args ->
                val key = args["key"] as? String ?: return@ToolDefinition ToolResult(success = false, error = "Missing key parameter")
                val actionIndex = (args["action_index"] as? Number)?.toInt() ?: 0
                clickNotification(key, actionIndex)
            }
        )
    }

    fun getOpenNotificationSettingsTool(): ToolDefinition {
        return ToolDefinition(
            name = "open_notification_settings",
            description = "Open notification access settings to grant permission",
            category = ToolCategory.SETTINGS,
            permissionLevel = ToolPermissionLevel.NORMAL,
            executor = { openNotificationSettings() }
        )
    }

    private fun readNotifications(limit: Int, packageFilter: String?): ToolResult {
        return try {
            val listener = NotificationListener.getInstance(context)
            if (listener == null) {
                return ToolResult(
                    success = false,
                    error = "NotificationListenerService not connected. Grant notification access in settings."
                )
            }

            val notifications = listener.getCachedNotifications(limit)
            val filtered = if (packageFilter != null) {
                notifications.filter { it.packageName == packageFilter }
            } else {
                notifications
            }

            if (filtered.isEmpty()) {
                return ToolResult(success = true, data = "No notifications found")
            }

            val output = filtered.mapIndexed { index, n ->
                val time = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date(n.timestamp))
                val content = if (n.bigText.isNotBlank()) n.bigText else n.text
                "${index + 1}. [$time] ${n.packageName}\n   ${n.title}\n   $content"
            }.joinToString("\n\n")

            ToolResult(success = true, data = output)
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to read notifications: ${e.message}")
        }
    }

    private fun dismissNotification(key: String): ToolResult {
        return try {
            val listener = NotificationListener.getInstance(context)
            if (listener == null) {
                return ToolResult(success = false, error = "NotificationListenerService not connected")
            }
            val success = listener.dismissNotification(key)
            ToolResult(success = success, data = if (success) "Notification dismissed" else "Failed to dismiss")
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to dismiss notification: ${e.message}")
        }
    }

    private fun clickNotification(key: String, actionIndex: Int): ToolResult {
        return try {
            val listener = NotificationListener.getInstance(context)
            if (listener == null) {
                return ToolResult(success = false, error = "NotificationListenerService not connected")
            }
            val success = listener.clickNotification(key)
            ToolResult(success = success, data = if (success) "Notification clicked" else "Failed to click")
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to click notification: ${e.message}")
        }
    }

    private fun openNotificationSettings(): ToolResult {
        return try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult(success = true, data = "Opened notification access settings")
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to open settings: ${e.message}")
        }
    }
}