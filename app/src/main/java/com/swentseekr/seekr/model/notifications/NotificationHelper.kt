package com.swentseekr.seekr.model.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.swentseekr.seekr.MainActivity
import com.swentseekr.seekr.R
import com.swentseekr.seekr.model.notifications.NotificationConstants.HUNT_ID
import com.swentseekr.seekr.model.notifications.NotificationConstants.NULL_PENDING_INTENT_REQUEST_CODE

object NotificationHelper {

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
          NotificationChannel(
              NotificationConstants.CHANNEL_ID,
              NotificationConstants.GENERAL_CHANNEL_NAME,
              NotificationManager.IMPORTANCE_DEFAULT)

      val manager = context.getSystemService(NotificationManager::class.java)
      manager.createNotificationChannel(channel)
    }
  }

  /**
   * Sends a notification to the user with the specified title, message, and optional hunt ID.
   *
   * This method:
   * - Checks the POST_NOTIFICATIONS permission on Android 13 (Tiramisu) and above, returning early
   *   if the permission is not granted.
   * - Ensures the notification channel is created before posting the notification.
   * - Builds an intent that opens `MainActivity` and includes the hunt ID in the extras.
   * - Uses a `TaskStackBuilder` to create a proper back stack for the activity.
   * - Posts the notification using `NotificationManagerCompat`.
   *
   * @param context The context used to access system services and create the notification.
   * @param title The title displayed in the notification.
   * @param message The message body displayed in the notification.
   * @param huntId An optional identifier included in the launched activity’s intent extras. May be
   *   null.
   */
  fun sendNotification(context: Context, title: String, message: String, huntId: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
      return
    }
    createNotificationChannel(context)

    val intent =
        Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra(HUNT_ID, huntId)
        }

    val pendingIntent =
        TaskStackBuilder.create(context).run {
          addNextIntentWithParentStack(intent)
          getPendingIntent(
              NULL_PENDING_INTENT_REQUEST_CODE,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    val builder =
        NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_seekr)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

    val manager = NotificationManagerCompat.from(context)
    manager.notify(System.currentTimeMillis().toInt(), builder.build())
  }
}
