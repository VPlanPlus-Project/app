package plus.vplan.app.domain.repository

interface PlatformNotificationRepository {
    suspend fun initialize()
    suspend fun sendNotification(
        title: String,
        message: String,
        category: String? = null,
        isLarge: Boolean = false,
        largeText: String? = null,
    )
}