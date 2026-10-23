package com.example.fishpet

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.WindowManager
import kotlin.random.Random

class FishPetService : Service() {

    private lateinit var wm: WindowManager
    private var fish: FishView? = null

    private val handler = Handler(Looper.getMainLooper())

    private val thoughts = listOf(
        "在思考鱼生…",
        "今天也要摸鱼",
        "吐个泡泡吧",
        "你忙你的，我挂着",
        "水好静…",
        "有点想睡觉",
        "我是大肥鱼"
    )

    private val chatter = listOf(
        "别戳我啦",
        "再戳就装死",
        "咕噜咕噜",
        "我在呢",
        "你回来啦",
        "嘿嘿"
    )

    private val tick = object : Runnable {
        override fun run() {
            fish?.say(thoughts.random(), 2600)
            handler.postDelayed(this, 7000L + Random.nextLong(0, 5000))
        }
    }

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        createChannel()
        startForeground(1, buildNotification())
        showFish()
        handler.postDelayed(tick, 5000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        fish?.let { runCatching { wm.removeView(it) } }
        fish = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showFish() {
        if (fish != null) return

        val view = FishView(this)
        view.windowManager = wm

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 300
        }

        view.layoutParams = params
        view.onClicked = {
            view.say(chatter.random(), 2000)
        }

        runCatching {
            wm.addView(view, params)
            fish = view
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "fishpet",
                "大肥鱼桌面宠物",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, "fishpet")
            .setContentTitle("大肥鱼在挂机")
            .setContentText("点按小鱼可以互动")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }
}
