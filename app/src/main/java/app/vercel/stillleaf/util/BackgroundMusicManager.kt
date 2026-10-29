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
     * Starts gentle background ambient music at low volume (12%).
     */
    fun startMusic(context: Context) {
        if (!isMusicEnabled(context)) {
            Log.d(TAG, "Background music is disabled in settings.")
            return
        }

        if (mediaPlayer == null) {
            try {
                mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.bg_gentle_bamboo_breeze)?.apply {
                    isLooping = true
                    // Low ambient volume (12% left and right channel)
                    setVolume(0.12f, 0.12f)
                    start()
                }
                isPausedByLifecycle = false
                Log.d(TAG, "Gentle Bamboo Breeze background music started at low volume.")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing background music player", e)
            }
        } else if (mediaPlayer?.isPlaying == false) {
            try {
                mediaPlayer?.start()
                isPausedByLifecycle = false
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming background music", e)
            }
        }
    }

    /**
     * Pauses background music when activity is backgrounded.
     */
    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPausedByLifecycle = true
                Log.d(TAG, "Background music paused for lifecycle.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing background music", e)
        }
    }

    /**
     * Resumes background music when activity returns to foreground.
     */
    fun resumeMusic(context: Context) {
        if (isMusicEnabled(context) && isPausedByLifecycle && mediaPlayer != null) {
            try {
                mediaPlayer?.start()
                isPausedByLifecycle = false
                Log.d(TAG, "Background music resumed.")
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming background music on activity resume", e)
            }
        } else if (isMusicEnabled(context) && mediaPlayer == null) {
            startMusic(context)
        }
    }

    /**
     * Stops and releases background music player.
     */
    fun stopMusic() {
        try {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
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
