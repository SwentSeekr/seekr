package com.swentseekr.seekr.model.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.swentseekr.seekr.MainActivity
import com.swentseekr.seekr.R
import android.app.PendingIntent

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

  fun sendNotification(context: Context, title: String, message: String, huntId: String?) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
      return
    }
    createNotificationChannel(context)

      val intent = Intent(context, MainActivity::class.java).apply {
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
          putExtra("huntId", huntId)
      }

      val pendingIntent = TaskStackBuilder.create(context).run {
          addNextIntentWithParentStack(intent)
          getPendingIntent(
              0,
              PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
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
