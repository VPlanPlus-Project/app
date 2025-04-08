package plus.vplan.app.android.service

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase

class FcmPushNotificationService : FirebaseMessagingService(), KoinComponent {

    val updateFirebaseTokenUseCase: UpdateFirebaseTokenUseCase by inject()
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
        Logger.d { "FCM Message received: $message" }
    }
}