package plus.vplan.app.feature.main.domain.usecase

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await
import plus.vplan.app.captureError

actual suspend fun getFirebaseToken(): String? {
    return try {
        Firebase.messaging.token.await()
    } catch (e: Exception) {
        captureError("UpdateFirebaseTokenUseCase.getFirebaseToken", "Failed to load token: ${e.stackTraceToString()}")
        null
    }
}

actual suspend fun setProperty(property: String, value: String) {
    Firebase.crashlytics.setCustomKey(property, value)
}