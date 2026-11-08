package app.vercel.stillleaf.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import app.vercel.stillleaf.MainActivity
import app.vercel.stillleaf.R
import app.vercel.stillleaf.data.repository.PlantPersistenceManager

class CareNotificationReceiver : BroadcastReceiver() {

    companion object {
        const val TAG = "CareNotificationReceiver"
        const val ACTION_DAILY_REMINDER = "app.vercel.stillleaf.DAILY_REMINDER"
        const val ACTION_MILESTONE_REMINDER = "app.vercel.stillleaf.MILESTONE_REMINDER"
        const val EXTRA_PLANT_NAME = "extra_plant_name"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d(TAG, "Received notification broadcast action: $action")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build intent to open app when tapping notification
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                Log.d(TAG, "Device boot completed. Rescheduling daily care morning alarm.")
                CareNotificationManager.scheduleDailyMorningAlarm(context)
            }

            ACTION_DAILY_REMINDER -> {
                val plants = PlantPersistenceManager.loadAllLocalPlants(context)
                if (plants.isEmpty()) return

                val firstPlant = plants.first()
                val plantCount = plants.size
                val bodyText = if (plantCount == 1) {
                    "Your ${firstPlant.nickname} could use a drink and care check today!"
                } else {
                    "Your ${firstPlant.nickname} and ${plantCount - 1} other plants have care tasks due today."
                }

                val notification = NotificationCompat.Builder(context, CareNotificationManager.CHANNEL_CARE_REMINDERS)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("🌿 Plant Care")
                    .setContentText(bodyText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1001, notification)
            }

            ACTION_MILESTONE_REMINDER -> {
                val plantName = intent.getStringExtra(EXTRA_PLANT_NAME) ?: "your plant"
                val bodyText = "It's time for $plantName's quarterly growth photo! Capture a snapshot to celebrate its 90-day progress."

                val notification = NotificationCompat.Builder(context, CareNotificationManager.CHANNEL_MILESTONES)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("📷 90-Day Growth Milestone!")
                    .setContentText(bodyText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1002, notification)
            }
        }
    }
}
