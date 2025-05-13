package plus.vplan.app.android.service

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.feature.system.usecase.HandlePushNotificationUseCase
import plus.vplan.app.isDeveloperMode

class FcmPushNotificationService : FirebaseMessagingService(), KoinComponent {

    val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase by inject()
    val handlePushNotificationService: HandlePushNotificationUseCase by inject()

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
        val type = (message.data["type"] ?: run {
            Logger.w { "No type found in FCM message, ignoring" }
            return
        }).let {
            @Suppress("KotlinConstantConditions")
            if (it.startsWith("DEV_") && !isDeveloperMode) {
                Logger.w { "DEV_ message received, but developer mode is not enabled, ignoring" }
                return
            }
            it.substringAfter("DEV_")
        }.also {
            Logger.i { "FCM Message type: $it" }
        }

        MainScope().launch {
            handlePushNotificationService(type, message.data["data"].orEmpty())
        }
    }
}