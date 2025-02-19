package plus.vplan.app.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import plus.vplan.app.R
import plus.vplan.app.domain.repository.PlatformNotificationRepository

class PlatformNotificationImpl(
    private val context: Context
) : PlatformNotificationRepository {
    private val channelId = "VPlanPlus"
    override suspend fun initialize() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "VPlanPlus Benachrichtigungen"
            val descriptionText = "Benachrichtigungen für VPlanPlus"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, name, importance)
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
        largeText: String?
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSubText(category)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .also {
                if (isLarge) it.setStyle(NotificationCompat.BigTextStyle().bigText(largeText))
            }
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(1, builder)
        }

    }
}