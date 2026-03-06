package plus.vplan.app.feature.main.domain.usecase

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.core.analytics.AnalyticsRepository

actual suspend fun getFirebaseToken(): String? {
    return object : KoinComponent {
        val analyticsRepository: AnalyticsRepository by inject()
        suspend fun get(): String? {
            return try {
                Firebase.messaging.token.await()
            } catch (e: Exception) {
                analyticsRepository.captureError("UpdateFirebaseTokenUseCase.getFirebaseToken", "Failed to load token: ${e.stackTraceToString()}")
                null
            }
        }
    }.get()
}

actual suspend fun setProperty(property: String, value: String) {
    Firebase.crashlytics.setCustomKey(property, value)
}
