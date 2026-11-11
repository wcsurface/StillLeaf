package app.vercel.stillleaf.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import app.vercel.stillleaf.data.model.Plant
import java.util.Calendar

object CareNotificationManager {

    private const val TAG = "CareNotificationManager"
    private const val PREFS_NAME = "still_leaf_notification_settings"
    private const val KEY_DAILY_REMINDERS = "key_daily_reminders"
    private const val KEY_MILESTONE_REMINDERS = "key_milestone_reminders"

    const val CHANNEL_CARE_REMINDERS = "still_leaf_daily_reminders"
    const val CHANNEL_MILESTONES = "still_leaf_growth_milestones"

    /**
     * Initializes Android Notification Channels for daily care and 90-day milestones.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daily Care Channel
            val dailyChannel = NotificationChannel(
                CHANNEL_CARE_REMINDERS,
                "Daily Care Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Morning notifications for watering, nourishment, and rotation care tasks."
            }

            // Milestone Channel
            val milestoneChannel = NotificationChannel(
                CHANNEL_MILESTONES,
                "90-Day Growth Milestones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Quarterly reminders to photograph visual plant growth progress."
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(milestoneChannel)
            Log.d(TAG, "Notification channels initialized successfully.")
        }
    }

    /**
     * Reads daily care reminders toggle preference.
     */
    fun isDailyRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_DAILY_REMINDERS, true)
    }

    /**
     * Reads 90-day milestone photo reminders toggle preference.
     */
    fun isMilestoneRemindersEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MILESTONE_REMINDERS, true)
    }

    /**
     * Saves daily care reminders toggle preference and updates scheduled alarm state.
     */
    fun setDailyRemindersEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_DAILY_REMINDERS, enabled).apply()

        if (enabled) {
            scheduleDailyMorningAlarm(context)
        } else {
            cancelDailyMorningAlarm(context)
        }
    }

    /**
     * Saves 90-day milestone reminders toggle preference.
     */
    fun setMilestoneRemindersEnabled(context: Context, enabled: Boolean, plants: List<Plant> = emptyList()) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MILESTONE_REMINDERS, enabled).apply()

        if (enabled) {
            plants.forEach { schedule90DayMilestoneAlarm(context, it) }
        }
    }

    /**
     * Schedules a recurring 8:00 AM daily care alarm.
     */
    fun scheduleDailyMorningAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CareNotificationReceiver::class.java).apply {
                action = CareNotificationReceiver.ACTION_DAILY_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, 8) // 8:00 AM morning
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)

                // If 8:00 AM today has already passed, schedule for tomorrow 8:00 AM
                if (before(Calendar.getInstance())) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            Log.d(TAG, "Daily 8:00 AM Care Alarm scheduled for ${calendar.time} successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling daily morning alarm", e)
        }
    }

    /**
     * Cancels the scheduled daily morning care alarm.
     */
    fun cancelDailyMorningAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CareNotificationReceiver::class.java).apply {
                action = CareNotificationReceiver.ACTION_DAILY_REMINDER
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                2001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Daily Care Alarm cancelled.")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling daily care alarm", e)
        }
    }

    /**
     * Schedules a 90-day milestone photo reminder for a specific plant.
     */
    fun schedule90DayMilestoneAlarm(context: Context, plant: Plant) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CareNotificationReceiver::class.java).apply {
                action = CareNotificationReceiver.ACTION_MILESTONE_REMINDER
                putExtra(CareNotificationReceiver.EXTRA_PLANT_NAME, plant.nickname)
            }

            val requestCode = plant.id.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 90 Days from plant added date
            val milestoneMillis = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000)

            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                milestoneMillis,
                pendingIntent
            )
            Log.d(TAG, "90-Day Milestone Alarm scheduled for ${plant.nickname}.")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling 90-day milestone alarm for ${plant.nickname}", e)
        }
    }
}
