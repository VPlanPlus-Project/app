package plus.vplan.app.core.platform

interface NotificationRepository {
    suspend fun initialize()
    suspend fun isNotificationPermissionGranted(): Boolean
    suspend fun sendNotification(
        title: String,
        message: String,
        category: String? = null,
        isLarge: Boolean = false,
        largeText: String? = null,
        onClickData: String? = null
    )
}
