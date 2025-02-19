package plus.vplan.app.data.repository

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.os.Build
import plus.vplan.app.domain.repository.PlatformNotificationRepository

class PlatformNotificationImpl(
    private val context: Context
) : PlatformNotificationRepository {
    override suspend fun initialize() {
        val channelId = "VPlanPlus"
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
}