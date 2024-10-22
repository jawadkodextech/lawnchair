package app.lawnchair

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import app.lawnchair.search.FullScreenActivity
import com.android.launcher3.R

const val POST_NOTIFY_ID = 12212
const val NOTIFY_CHANNEL_DEFAULT = "default_browser_channel_launcher"
fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}


fun requestNotificationPermission(notificationPermissionLauncher: ActivityResultLauncher<String>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}

fun createChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "${context.getString(R.string.app_name)} Default Notification"
        val descriptionText = "Notification when the app is not the default launcher"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(NOTIFY_CHANNEL_DEFAULT, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

@SuppressLint("RemoteViewLayout")
fun showNotification(context: Context) {
    // Create the notification channel if necessary (Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = NotificationManagerCompat.from(context)
        val activeNotifications = notificationManager.activeNotifications

        // Check if the notification with POST_NOTIFY_ID is already displayed
        if (activeNotifications.any { it.id == POST_NOTIFY_ID }) {
            return // Do nothing if the notification is already displayed
        }
    }

    val customView = RemoteViews(context.packageName, R.layout.custom_notification)

    // Optionally set custom content in the notification
//    customView.setTextViewText(R.id.custom_title, "${context.getString(R.string.app_name)} as Default Browser")
    customView.setTextViewText(R.id.custom_title, "Search the web")
//    customView.setTextViewText(R.id.custom_text, "${context.getString(R.string.app_name)} is not set as the default browser. Please set it now to browse safely.")


    // Intent for when the user clicks the notification
//    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    val intent = Intent(context, FullScreenActivity::class.java)
    val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // Build the notification
    val builder = NotificationCompat.Builder(context, NOTIFY_CHANNEL_DEFAULT)
        .setSmallIcon(R.mipmap.ic_launcher_home)
//        .setCustomContentView(customView) // Set the custom view
        .setCustomContentView(customView) // Set the custom view
        .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Use decorated style to keep icon and time
//        .setPriority(NotificationCompat.PRIORITY_HIGH)
//        .setContentTitle("${context.getString(R.string.app_name)} as Default Browser")
//        .setContentText("${context.getString(R.string.app_name)} is not set as the default browser. Please set it now to browse safely.")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true) // non-cancelable notification
        .setContentIntent(pendingIntent)
    if (checkNotificationPermission(context)) {
        with(NotificationManagerCompat.from(context)) {
            notify(POST_NOTIFY_ID, builder.build())
        }
    }
}

fun removeNotification(context: Context, NOTIFICATION_ID: Int) {
//    with(NotificationManagerCompat.from(context)) {
//        cancel(NOTIFICATION_ID)
//    }
}


@SuppressLint("RemoteViewLayout")
fun showNotificationForSetDefault(context: Context) {
    // Create the notification channel if necessary (Android 8.0+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = NotificationManagerCompat.from(context)
        val activeNotifications = notificationManager.activeNotifications

        // Check if the notification with POST_NOTIFY_ID is already displayed
        if (activeNotifications.any { it.id == POST_NOTIFY_ID }) {
            return // Do nothing if the notification is already displayed
        }
    }

    val customView = RemoteViews(context.packageName, R.layout.custom_notification)

    // Optionally set custom content in the notification
//    customView.setTextViewText(R.id.custom_title, "${context.getString(R.string.app_name)} as Default Browser")
    customView.setTextViewText(R.id.custom_title, "Search the web")
//    customView.setTextViewText(R.id.custom_text, "${context.getString(R.string.app_name)} is not set as the default browser. Please set it now to browse safely.")


    // Intent for when the user clicks the notification
//    val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
    val intent = Intent(context, FullScreenActivity::class.java)
    val pendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
        addNextIntentWithParentStack(intent)
        getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    }

    // Build the notification
    val builder = NotificationCompat.Builder(context, NOTIFY_CHANNEL_DEFAULT)
        .setSmallIcon(R.mipmap.ic_launcher_home)
//        .setCustomContentView(customView) // Set the custom view
        .setCustomContentView(customView) // Set the custom view
        .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Use decorated style to keep icon and time
//        .setPriority(NotificationCompat.PRIORITY_HIGH)
//        .setContentTitle("${context.getString(R.string.app_name)} as Default Browser")
//        .setContentText("${context.getString(R.string.app_name)} is not set as the default browser. Please set it now to browse safely.")
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .setOngoing(true) // non-cancelable notification
        .setContentIntent(pendingIntent)
    if (checkNotificationPermission(context)) {
        with(NotificationManagerCompat.from(context)) {
            notify(POST_NOTIFY_ID, builder.build())
        }
    }
}
