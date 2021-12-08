package com.kikkia.customlivewallpaper

import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.SurfaceHolder
import com.google.android.exoplayer2.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.REPEAT_MODE_ALL
import com.google.android.exoplayer2.Player.REPEAT_MODE_OFF
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.RawResourceDataSource


class WallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return VideoEngine()

    }

    private inner class VideoEngine : Engine() {
        private val TAG = javaClass.simpleName
        private val exoMediaPlayer: SimpleExoPlayer
        override fun onSurfaceCreated(holder: SurfaceHolder) {
            exoMediaPlayer.setVideoSurfaceHolder(holder)

            val videoUri = RawResourceDataSource.buildRawResourceUri(R.raw.test)
            val dataSourceFactory = DataSource.Factory { RawResourceDataSource(baseContext) }
            val mediaSourceFactory = ExtractorMediaSource.Factory(dataSourceFactory)
            exoMediaPlayer.prepare(mediaSourceFactory.createMediaSource(videoUri))
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            playheadTime = exoMediaPlayer.currentPosition
            exoMediaPlayer.release()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            val sp = getSharedPreferences("CWP", MODE_PRIVATE)

            if (visible) {
                // mediaPlayer.start()
                exoMediaPlayer.repeatMode = if (sp.getBoolean("loop", true)) REPEAT_MODE_ALL else REPEAT_MODE_OFF
            } else {
                // exoMediaPlayer.stop()
            }
        }

        init {
            exoMediaPlayer = initExoMediaPlayer()
        }

        private fun initExoMediaPlayer(): SimpleExoPlayer {
            val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(DefaultBandwidthMeter())
            val trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)
            val player = ExoPlayerFactory.newSimpleInstance(this@WallpaperService,
                    trackSelector)
            player.videoScalingMode = VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            player.playWhenReady = true
            player.volume = 0f
            return player
        }
    }

    companion object {
        protected var playheadTime = 0L
    }
}