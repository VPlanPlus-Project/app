package plus.vplan.app.android.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import plus.vplan.app.data.repository.PlatformNotificationImpl
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase

class SyncWorker(
    context: Context,
    workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters), KoinComponent {
    private val fullSyncUseCase: FullSyncUseCase by inject()
    override suspend fun doWork(): Result {
        fullSyncUseCase()
        return Result.success()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1,
            NotificationCompat.Builder(applicationContext, PlatformNotificationImpl.channelId)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentTitle("Synchronisiere...")
                .setContentText("VPlanPlus aktualisiert die Daten auf deinem Gerät")
                .build()
        )
    }
}