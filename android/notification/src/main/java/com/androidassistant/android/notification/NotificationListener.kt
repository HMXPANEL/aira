package com.androidassistant.android.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.RequiresApi
import com.androidassistant.core.common.logD
import com.androidassistant.core.common.logE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class NotificationListener : NotificationListenerService() {

    companion object {
        private const val MAX_STORED_NOTIFICATIONS = 100
        val notificationChannel = Channel<NotificationData>(MAX_STORED_NOTIFICATIONS)
        private var notificationCache = mutableListOf<NotificationData>()
        private val cacheLock = Any()
        @Suppress("UNUSED_PARAMETER")
        private var instance: NotificationListener? = null

        fun getInstance(context: Context): NotificationListener? {
            return instance
        }

        private fun setInstance(listener: NotificationListener?) {
            instance = listener
        }
    }

    override fun onCreate() {
        super.onCreate()
        setInstance(this)
        logD("NotificationListenerService created")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        val notification = extractNotificationData(sbn)
        if (notification != null) {
            addToCache(notification)
            CoroutineScope(Dispatchers.IO).launch {
                notificationChannel.send(notification)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        val key = sbn.key
        synchronized(cacheLock) {
            notificationCache.removeAll { it.key == key }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        logD("NotificationListener connected")
        val currentNotifications = currentNotifications
        currentNotifications.forEach { sbn ->
            val notification = extractNotificationData(sbn)
            if (notification != null) {
                addToCache(notification)
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        logD("NotificationListener disconnected")
    }

    override fun onDestroy() {
        super.onDestroy()
        setInstance(null)
        logD("NotificationListenerService destroyed")
    }

    private fun extractNotificationData(sbn: StatusBarNotification): NotificationData? {
        return try {
            val notification = sbn.notification
            val extras = notification.extras

            val title = extras.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
            val bigText = extras.getCharSequence(android.app.Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
            val packageName = sbn.packageName
            val timestamp = sbn.postTime
            val key = sbn.key

            val actions = notification.actions?.map { action ->
                NotificationAction(
                    title = action.title.toString(),
                    action = action.actionIntent?.let { PendingIntentData(it) }
                )
            } ?: emptyList()

            NotificationData(
                key = key,
                packageName = packageName,
                title = title,
                text = text,
                bigText = bigText,
                timestamp = timestamp,
                actions = actions,
                isOngoing = (notification.flags and android.app.Notification.FLAG_ONGOING_EVENT) != 0,
                isClearable = (notification.flags and android.app.Notification.FLAG_AUTO_CANCEL) != 0
            )
        } catch (e: Exception) {
            logE("Failed to extract notification data", e)
            null
        }
    }

    private fun addToCache(notification: NotificationData) {
        synchronized(cacheLock) {
            notificationCache.removeAll { it.key == notification.key }
            notificationCache.add(0, notification)
            if (notificationCache.size > MAX_STORED_NOTIFICATIONS) {
                notificationCache = notificationCache.take(MAX_STORED_NOTIFICATIONS).toMutableList()
            }
        }
    }

    fun getCachedNotifications(limit: Int = 20): List<NotificationData> {
        synchronized(cacheLock) {
            return notificationCache.take(limit)
        }
    }

    fun clearCache() {
        synchronized(cacheLock) {
            notificationCache.clear()
        }
    }

    fun dismissNotification(key: String): Boolean {
        return try {
            cancelNotification(key)
            true
        } catch (e: Exception) {
            logE("Failed to dismiss notification", e)
            false
        }
    }

    fun clickNotification(key: String): Boolean {
        return try {
            val notification = getCachedNotifications(MAX_STORED_NOTIFICATIONS).find { it.key == key }
            notification?.actions?.firstOrNull()?.action?.let { pendingIntent ->
                pendingIntent.send()
                true
            } ?: false
        } catch (e: Exception) {
            logE("Failed to click notification", e)
            false
        }
    }
}

data class NotificationData(
    val key: String,
    val packageName: String,
    val title: String,
    val text: String,
    val bigText: String,
    val timestamp: Long,
    val actions: List<NotificationAction>,
    val isOngoing: Boolean,
    val isClearable: Boolean
)

data class NotificationAction(
    val title: String,
    val action: PendingIntentData?
)

class PendingIntentData(private val pendingIntent: android.app.PendingIntent) {
    fun send() {
        try {
            pendingIntent.send()
        } catch (e: Exception) {
            com.androidassistant.core.common.logE("Failed to send pending intent", e)
        }
    }
}