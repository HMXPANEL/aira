package com.androidassistant.android.overlay

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.compose.ui.platform.ComposeView
import com.androidassistant.R
import com.androidassistant.ui.chat.ChatScreen
import com.androidassistant.agent.engine.AgentOrchestrator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private var expandedView: View? = null
    private var isExpanded = false
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    override fun onCreate() {
        super.onCreate()
        createBubble()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        removeViews()
        super.onDestroy()
    }

    private fun createBubble() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create the bubble (floating action button)
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        bubbleView = LayoutInflater.from(this).inflate(R.layout.overlay_bubble, null)
        val bubbleImage = bubbleView?.findViewById<ImageView>(R.id.bubble_icon)

        bubbleView?.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }

        bubbleImage?.setOnClickListener {
            if (!isExpanded) {
                expandBubble()
            }
        }

        windowManager?.addView(bubbleView!!, params)
    }

    private fun expandBubble() {
        isExpanded = true

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            getWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        expandedView = ComposeView(this).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeDetached)
            setContent {
                val orchestrator: AgentOrchestrator = get()
                ChatScreen(
                    viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                        factory = androidx.lifecycle.viewmodel.ViewModelProvider.Factory { clazz ->
                            com.androidassistant.ui.chat.ChatViewModel(orchestrator, com.androidassistant.agent.engine.safety.DefaultApprovalCallback())
                        }
                    ),
                    onNavigateToSettings = { collapseBubble() }
                )
            }
        }

        windowManager?.addView(expandedView!!, params)
        bubbleView?.visibility = View.GONE
    }

    private fun collapseBubble() {
        isExpanded = false
        expandedView?.let { windowManager?.removeView(it) }
        expandedView = null
        bubbleView?.visibility = View.VISIBLE
    }

    private fun handleTouch(event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params?.x?.toFloat() ?: 0f
                initialY = params?.y?.toFloat() ?: 0f
                initialTouchX = event.rawX
                initialTouchY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - initialTouchX
                val dy = event.rawY - initialTouchY
                params?.x = (initialX + dx).toInt()
                params?.y = (initialY + dy).toInt()
                bubbleView?.let { windowManager?.updateViewLayout(it, params!!) }
            }
            MotionEvent.ACTION_UP -> {
                // Snap to edge
                snapToEdge()
            }
        }
    }

    private fun snapToEdge() {
        val displayWidth = windowManager?.defaultDisplay?.width ?: 1080
        val bubbleWidth = bubbleView?.width ?: 60
        val params = bubbleView?.layoutParams as? WindowManager.LayoutParams

        params?.x = if ((params?.x ?: 0) < displayWidth / 2) {
            0
        } else {
            displayWidth - bubbleWidth
        }

        bubbleView?.let { windowManager?.updateViewLayout(it, params!!) }
    }

    private fun removeViews() {
        bubbleView?.let { windowManager?.removeView(it) }
        expandedView?.let { windowManager?.removeView(it) }
        bubbleView = null
        expandedView = null
    }

    private fun getWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private var params: WindowManager.LayoutParams? = null
}