package plus.vplan.app.android.service

import co.touchlab.kermit.Logger
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.usecase.UpdateFirebaseTokenUseCase
import plus.vplan.app.isDeveloperMode

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
        val type = (message.data["type"] ?: run {
            Logger.w { "No type found in FCM message, ignoring" }
            return
        }).let {
            if (it.startsWith("DEV_") && !isDeveloperMode) {
                Logger.w { "DEV_ message received, but developer mode is not enabled, ignoring" }
                return
            }
            it.substringAfter("DEV_")
        }.also {
            Logger.i { "FCM Message type: $it" }
        }

        MainScope().launch {
            when (type) {
                "HOMEWORK_UPDATE" -> {
                    val homeworkIds = message.data["homework_ids"]?.split(",").orEmpty().mapNotNull { it.toIntOrNull() }
                    Logger.d { "Homework ids: $homeworkIds" }
                    homeworkIds.forEach {
                        App.homeworkSource.getById(it, forceUpdate = true).getFirstValue()
                    }
                }

                "ASSESSMENT_UPDATE" -> {
                    val assessmentIds = message.data["assessment_ids"]?.split(",").orEmpty().mapNotNull { it.toIntOrNull() }
                    Logger.d { "Assessment ids: $assessmentIds" }
                    assessmentIds.forEach {
                        App.assessmentSource.getById(it, forceUpdate = true).getFirstValue()
                    }
                }
            }
        }
    }
}