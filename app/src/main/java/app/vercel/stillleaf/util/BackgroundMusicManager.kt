package app.vercel.stillleaf.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import app.vercel.stillleaf.R

object BackgroundMusicManager {

    private const val TAG = "BackgroundMusicManager"
    private const val PREFS_NAME = "still_leaf_audio_settings"
    private const val KEY_MUSIC_ENABLED = "key_music_enabled"

    private var mediaPlayer: MediaPlayer? = null
    private var isPausedByLifecycle = false
    private var savedPlaybackPosition = 0

    /**
     * Reads background music toggle preference (defaults to true).
     */
    fun isMusicEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSIC_ENABLED, true)
    }

    /**
     * Saves background music toggle preference and updates playback state immediately.
     */
    fun setMusicEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()

        if (enabled) {
            startMusic(context)
        } else {
            stopMusic()
        }
    }

    /**
     * Starts gentle background ambient music at low volume (12%), maintaining uninterrupted playback across screen rotations.
     */
    fun startMusic(context: Context) {
        if (!isMusicEnabled(context)) {
            Log.d(TAG, "Background music is disabled in settings.")
            return
        }

        // If player is already playing, do NOT restart the track
        if (mediaPlayer?.isPlaying == true) {
            Log.d(TAG, "Background music is already actively playing.")
            return
        }

        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bg_gentle_bamboo_breeze)?.apply {
                    isLooping = true
                    setVolume(0.12f, 0.12f)
                    if (savedPlaybackPosition > 0) {
                        seekTo(savedPlaybackPosition)
                    }
                    start()
                }
                isPausedByLifecycle = false
                Log.d(TAG, "Gentle Bamboo Breeze background music playing from $savedPlaybackPosition ms.")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing background music player", e)
            }
        } else {
            try {
                if (savedPlaybackPosition > 0) {
                    mediaPlayer?.seekTo(savedPlaybackPosition)
                }
                mediaPlayer?.start()
                isPausedByLifecycle = false
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming background music", e)
            }
        }
    }

    /**
     * Saves playback position and pauses music for lifecycle transitions.
     */
    fun pauseMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    savedPlaybackPosition = player.currentPosition
                    player.pause()
                    isPausedByLifecycle = true
                    Log.d(TAG, "Background music paused at $savedPlaybackPosition ms.")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing background music", e)
        }
    }

    /**
     * Resumes background music seamlessly from the saved position.
     */
    fun resumeMusic(context: Context) {
        if (isMusicEnabled(context)) {
            if (mediaPlayer == null) {
                startMusic(context)
            } else if (isPausedByLifecycle || mediaPlayer?.isPlaying == false) {
                try {
                    if (savedPlaybackPosition > 0) {
                        mediaPlayer?.seekTo(savedPlaybackPosition)
                    }
                    mediaPlayer?.start()
                    isPausedByLifecycle = false
                    Log.d(TAG, "Background music resumed at $savedPlaybackPosition ms.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error resuming background music on activity resume", e)
                }
            }
        }
    }

    /**
     * Stops and releases background music player on explicit exit.
     */
    fun stopMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    savedPlaybackPosition = player.currentPosition
                    player.stop()
                }
                player.release()
            }
            mediaPlayer = null
            isPausedByLifecycle = false
            Log.d(TAG, "Background music stopped and released.")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping background music", e)
            mediaPlayer = null
        }
    }
}
