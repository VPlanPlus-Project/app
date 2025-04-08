package plus.vplan.app.feature.main.domain.usecase

import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.tasks.await

actual suspend fun getFirebaseToken(): String? {
    return Firebase.messaging.token.await()
}