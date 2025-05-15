package plus.vplan.app.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import plus.vplan.app.MainActivity
import plus.vplan.app.R
import plus.vplan.app.domain.repository.PlatformNotificationRepository

class PlatformNotificationImpl(
    private val context: Context
) : PlatformNotificationRepository {
    companion object {
        const val VPLANPLUS = "VPlanPlus"
    }
    override suspend fun initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "VPlanPlus Benachrichtigungen"
            val descriptionText = "Benachrichtigungen für VPlanPlus"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(VPLANPLUS, name, importance)
            mChannel.description = descriptionText
            val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

    override suspend fun sendNotification(
        title: String,
        message: String,
        category: String?,
        isLarge: Boolean,
        largeText: String?,
        onClickData: String?
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val builder = NotificationCompat.Builder(context, VPLANPLUS)
            .setSmallIcon(R.drawable.app_icon_full)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSubText(category)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also {
                if (isLarge) it.setStyle(NotificationCompat.BigTextStyle().bigText(largeText))
            }

        if (onClickData != null) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("onClickData", onClickData)
            }

            val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.setContentIntent(pendingIntent)
        }

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }

    }
}