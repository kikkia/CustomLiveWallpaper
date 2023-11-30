package com.kikkia.customlivewallpaper

import android.service.wallpaper.WallpaperService
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource


class WallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()
    }

    private inner class VideoEngine : Engine() {
        private val TAG = javaClass.simpleName
        private val exoMediaPlayer: SimpleExoPlayer

        private var lastTouchTime = 0L

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            exoMediaPlayer.setVideoSurfaceHolder(holder)

            val videoUri = RawResourceDataSource.buildRawResourceUri(R.raw.test)
            val media = MediaItem.Builder().setUri(videoUri).build()
            val dataSourceFactory = DataSource.Factory { RawResourceDataSource(baseContext) }
            val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
            exoMediaPlayer.setMediaSource(mediaSourceFactory.createMediaSource(media))
            exoMediaPlayer.playWhenReady = true
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            playheadTime = exoMediaPlayer.currentPosition
            exoMediaPlayer.release()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            val sp = getSharedPreferences("CWP", MODE_PRIVATE)

            if (visible) {
                exoMediaPlayer.playWhenReady = true
                exoMediaPlayer.repeatMode = if (sp.getBoolean("   ", true)) REPEAT_MODE_ALL else REPEAT_MODE_OFF
            } else {
                // Pause when in background
                exoMediaPlayer.playWhenReady = false
            }
        }

        override fun onTouchEvent(event: MotionEvent?) {

            val action = event!!.actionMasked

            if (action == MotionEvent.ACTION_DOWN) {
                val time = System.currentTimeMillis()
                if (time - lastTouchTime <= 200) { // If 2 touches within 200ms
                    // Handle double tap
                    exoMediaPlayer.playWhenReady = !exoMediaPlayer.playWhenReady
                    lastTouchTime = 0
                } else {
                    lastTouchTime = time
                }
            }

            super.onTouchEvent(event)
        }

        init {
            exoMediaPlayer = initExoMediaPlayer()
            setTouchEventsEnabled(true)
        }

        private fun initExoMediaPlayer(): SimpleExoPlayer {
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
            val trackSelector = DefaultTrackSelector(this@WallpaperService, videoTrackSelectionFactory)
            val player = SimpleExoPlayer
                    .Builder(this@WallpaperService)
                    .setTrackSelector(trackSelector)
                    .setVideoScalingMode(Renderer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
                    .build()
            player.playWhenReady = true
            player.volume = 0f
            return player
        }
    }

    companion object {
        protected var playheadTime = 0L
    }
}