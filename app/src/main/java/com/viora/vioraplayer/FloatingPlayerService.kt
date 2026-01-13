package com.viora.player

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class FloatingPlayerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this)
            .inflate(R.layout.layout_floating_player, null)

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.x = 0
        layoutParams.y = 200

        windowManager.addView(floatingView, layoutParams)

        enableDrag()
    }

    private fun enableDrag() {
        floatingView.setOnTouchListener(object : View.OnTouchListener {

            private var startX = 0
            private var startY = 0
            private var touchX = 0f
            private var touchY = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = layoutParams.x
                        startY = layoutParams.y
                        touchX = event.rawX
                        touchY = event.rawY
                        return true
                    }

                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x =
                            startX + (event.rawX - touchX).toInt()
                        layoutParams.y =
                            startY + (event.rawY - touchY).toInt()
                        windowManager.updateViewLayout(
                            floatingView,
                            layoutParams
                        )
                        return true
                    }
                }
                return false
            }
        })
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val videoPath =
            intent?.getStringExtra("video_path")
                ?: return START_NOT_STICKY

        val playerView =
            floatingView.findViewById<PlayerView>(
                R.id.floatingPlayerView
            )

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player.setMediaItem(
            MediaItem.fromUri(Uri.parse(videoPath))
        )
        player.prepare()
        player.play()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        if (::player.isInitialized) {
            player.release()
        }

        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
