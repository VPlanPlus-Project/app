package plus.vplan.app.android.service

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.analytics.AnalyticsRepository
import plus.vplan.app.core.data.FcmRepository
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.feature.system.usecase.HandlePushNotificationUseCase

class FcmPushNotificationService : FirebaseMessagingService(), KoinComponent {

    val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase by inject()
    val handlePushNotificationService: HandlePushNotificationUseCase by inject()
    val keyValueRepository: KeyValueRepository by inject()
    val fcmRepository: FcmRepository by inject()
    val analyticsRepository: AnalyticsRepository by inject()

    private val logger = Logger.withTag("FcmPushNotificationService")

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        logger.i { "New token: $token" }
        MainScope().launch {
            updateFirebaseTokenUseCase(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        MainScope().launch {
            try {
                val type = (message.data["type"] ?: run {
                    Logger.w { "No type found in FCM message, ignoring" }
                    return@launch
                }).let {
                    analyticsRepository.capture("FCM.Message", mapOf("Type" to it))
                    fcmRepository.log(
                        topic = it,
                        message = message.data["data"].orEmpty()
                    )
                    if (it.startsWith("DEV_") && keyValueRepository.get(Keys.DEVELOPER_SETTINGS_ACTIVE).first() != "true") {
                        Logger.w { "DEV_ message received, but developer mode is not enabled, ignoring" }
                        return@launch
                    }
                    it.substringAfter("DEV_")
                }.also {
                    Logger.i { "FCM Message type: $it" }
                }

                handlePushNotificationService(type, message.data["data"].orEmpty())
            } catch (e: Exception) {
                logger.e(e) { "Error processing FCM message" }
                analyticsRepository.capture("FCM.Error", mapOf("Error" to e.stackTraceToString()))
            }
        }
    }
}