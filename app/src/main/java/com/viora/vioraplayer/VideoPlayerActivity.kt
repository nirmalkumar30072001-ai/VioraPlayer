package com.viora.player

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlin.math.abs

class VideoPlayerActivity : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var audioManager: AudioManager
    private lateinit var videoUri: Uri

    private var startX = 0f
    private var startY = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_player)

        playerView = findViewById(R.id.playerView)
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        val uriString = intent.getStringExtra("video_uri") ?: run {
            finish()
            return
        }

        videoUri = Uri.parse(uriString)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        player.setMediaItem(MediaItem.fromUri(videoUri))
        player.prepare()
        player.play()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()

        if (player.isPlaying) {
            val intent = Intent(this, FloatingPlayerService::class.java)
            intent.putExtra("video_uri", videoUri.toString())
            startService(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        player.stop()
        player.release()
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - startX
                val deltaY = startY - event.y

                if (abs(deltaX) > abs(deltaY)) {
                    val seekOffset = (deltaX / screenWidth) * 10000
                    val newPos =
                        (player.currentPosition + seekOffset)
                            .toLong()
                            .coerceIn(0, player.duration)

                    player.seekTo(newPos)
                } else {
                    val maxVolume =
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val currentVolume =
                        audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

                    val change =
                        (deltaY / screenHeight * maxVolume).toInt()

                    val newVolume =
                        (currentVolume + change).coerceIn(0, maxVolume)

                    audioManager.setStreamVolume(
                        AudioManager.STREAM_MUSIC,
                        newVolume,
                        0
                    )
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }
}
