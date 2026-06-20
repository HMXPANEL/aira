package com.androidassistant.tool.system

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.provider.Settings
import com.androidassistant.core.model.ToolResult
import com.androidassistant.tool.registry.ToolCategory
import com.androidassistant.tool.registry.ToolDefinition
import com.androidassistant.tool.registry.ToolParameter
import com.androidassistant.tool.registry.ToolParameterType
import com.androidassistant.tool.registry.ToolPermissionLevel

class SystemTools(private val context: Context) {

    fun getDeviceInfoTool(): ToolDefinition {
        return ToolDefinition(
            name = "get_device_info",
            description = "Get information about the device including battery level, network status, and Android version",
            category = ToolCategory.INFORMATION,
            permissionLevel = ToolPermissionLevel.NONE,
            executor = { getDeviceInfo() }
        )
    }

    fun getCurrentTimeTool(): ToolDefinition {
        return ToolDefinition(
            name = "get_current_time",
            description = "Get the current date and time on the device",
            category = ToolCategory.INFORMATION,
            permissionLevel = ToolPermissionLevel.NONE,
            executor = {
                val now = System.currentTimeMillis()
                val dateFormat = java.text.SimpleDateFormat("EEEE, MMMM d, yyyy 'at' h:mm a", java.util.Locale.getDefault())
                ToolResult(success = true, data = "Current time: ${dateFormat.format(java.util.Date(now))}")
            }
        )
    }

    fun openAppTool(): ToolDefinition {
        return ToolDefinition(
            name = "open_app",
            description = "Open an Android app by its package name",
            category = ToolCategory.APP_CONTROL,
            permissionLevel = ToolPermissionLevel.NORMAL,
            parameters = listOf(
                ToolParameter(
                    name = "package_name",
                    type = ToolParameterType.STRING,
                    description = "The Android package name of the app to open (e.g., com.whatsapp, com.spotify.music)",
                    required = true
                )
            ),
            executor = { args ->
                val packageName = args["package_name"] as? String
                if (packageName == null) {
                    return@ToolDefinition ToolResult(success = false, error = "Missing package_name parameter")
                }
                openApp(packageName)
            }
        )
    }

    fun setTimerTool(): ToolDefinition {
        return ToolDefinition(
            name = "set_timer",
            description = "Set a timer on the device",
            category = ToolCategory.SYSTEM,
            permissionLevel = ToolPermissionLevel.NORMAL,
            parameters = listOf(
                ToolParameter(
                    name = "duration_seconds",
                    type = ToolParameterType.INTEGER,
                    description = "Duration of the timer in seconds",
                    required = true
                ),
                ToolParameter(
                    name = "label",
                    type = ToolParameterType.STRING,
                    description = "Optional label for the timer",
                    required = false
                )
            ),
            executor = { args ->
                val duration = (args["duration_seconds"] as? Number)?.toInt() ?: return@ToolDefinition ToolResult(success = false, error = "Missing duration_seconds")
                val label = args["label"] as? String ?: "Timer"
                setTimer(duration, label)
            }
        )
    }

    private fun getDeviceInfo(): ToolResult {
        return try {
            val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val isCharging = batteryManager?.let {
                val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING
            } ?: false

            val info = buildString {
                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                appendLine("Battery: $batteryLevel%${if (isCharging) " (charging)" else ""}")
                appendLine("Network: ${getNetworkType()}")
            }
            ToolResult(success = true, data = info.trimEnd())
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to get device info: ${e.message}")
        }
    }

    private fun openApp(packageName: String): ToolResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent == null) {
                return ToolResult(success = false, error = "App not found: $packageName")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ToolResult(success = true, data = "Opened $packageName")
        } catch (e: Exception) {
            ToolResult(success = false, error = "Failed to open $packageName: ${e.message}")
        }
    }

    private fun setTimer(durationSeconds: Int, label: String): ToolResult {
        return try {
            val intent = Intent(Settings.ACTION_TIMER_SETTINGS).apply {
                putExtra("android.intent.extra.alarm.SECONDS", durationSeconds)
                putExtra("android.intro.extra.alarm.MESSAGE", label)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ToolResult(success = true, data = "Timer set for ${durationSeconds}s: $label")
        } catch (e: Exception) {
            ToolResult(success = false, error = "Could not set timer: ${e.message}")
        }
    }

    private fun getNetworkType(): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
        val network = connectivityManager?.activeNetwork ?: return "None"
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"
        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }
}
