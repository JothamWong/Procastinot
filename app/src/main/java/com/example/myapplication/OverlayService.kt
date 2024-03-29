package com.example.myapplication

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.children
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.DurationUnit

@RequiresApi(Build.VERSION_CODES.R)
class OverlayService : Service() {



    companion object {
        private const val ACTION_SHOW = "SHOW"
        private const val ACTION_HIDE = "HIDE"

        var instance: OverlayService? = null

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_SHOW
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java).apply {
                action = ACTION_HIDE
            }
            context.startService(intent)
        }
    }

    private val overlay by lazy {
        val overlay = View.inflate(applicationContext, R.layout.overlay_service, null)
        overlay
    }

    private val textView by lazy {
        overlay.findViewById(R.id.textView) as TextView
    }

    private val windowManager by lazy {
        applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    private var curDur: Duration? = null

    fun setDuration(dur: Duration?) {
        if (dur == null) {
            overlay.visibility = View.GONE
        } else {
            overlay.visibility = View.VISIBLE
            textView.text = dur.toComponents { minutes, seconds, ns ->
                String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
                )
            }
        }
        curDur = dur
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun setText(s: String) {
        textView.text = s
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        instance = this

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).also {
            it.gravity = Gravity.LEFT or Gravity.BOTTOM
        }
        val overlay = overlay

        setDuration(Duration.parse("15s"))
        kotlin.concurrent.timer(period = 1000L, action = {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    if (curDur != null) {
                        var nDur: Duration? = curDur!!.minus(Duration.parse("1s"))
                        if (nDur!!.isNegative()) nDur = null;
                        setDuration(nDur)
                    }
                }
            }
        });

        windowManager.addView(overlay, layoutParams)

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        windowManager.removeView(overlay)
    }
}