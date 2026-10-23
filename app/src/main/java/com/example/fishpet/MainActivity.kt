package com.example.fishpet

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var statusText: TextView
    private val overlayRequest = 1001
    private val notifRequest = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(56, 56, 56, 56)
        }

        val title = TextView(this).apply {
            text = "大肥鱼桌面宠物"
            textSize = 22f
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 28, 0, 28)
        }

        val btnPermission = Button(this).apply {
            text = "1. 授予悬浮窗权限"
            setOnClickListener {
                if (Settings.canDrawOverlays(this@MainActivity)) {
                    updateStatus()
                    Toast.makeText(this@MainActivity, "权限已开启", Toast.LENGTH_SHORT).show()
                } else {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:$packageName")
                    )
                    startActivityForResult(intent, overlayRequest)
                }
            }
        }

        val btnStart = Button(this).apply {
            text = "2. 启动大肥鱼"
            setOnClickListener {
                if (!Settings.canDrawOverlays(this@MainActivity)) {
                    Toast.makeText(this@MainActivity, "请先开启悬浮窗权限", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (Build.VERSION.SDK_INT >= 33 &&
                    checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), notifRequest)
                }

                startForegroundService(Intent(this@MainActivity, FishPetService::class.java))

                Toast.makeText(this@MainActivity, "已启动，回到桌面看大肥鱼", Toast.LENGTH_SHORT).show()
                moveTaskToBack(true)
            }
        }

        val btnStop = Button(this).apply {
            text = "3. 停止大肥鱼"
            setOnClickListener {
                stopService(Intent(this@MainActivity, FishPetService::class.java))
                Toast.makeText(this@MainActivity, "已停止", Toast.LENGTH_SHORT).show()
            }
        }

        root.addView(title)
        root.addView(statusText)
        root.addView(btnPermission)
        root.addView(btnStart)
        root.addView(btnStop)

        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
    }

    private fun updateStatus() {
        statusText.text = if (Settings.canDrawOverlays(this)) {
            "悬浮窗权限：已开启"
        } else {
            "悬浮窗权限：未开启"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == overlayRequest) updateStatus()
    }
}
