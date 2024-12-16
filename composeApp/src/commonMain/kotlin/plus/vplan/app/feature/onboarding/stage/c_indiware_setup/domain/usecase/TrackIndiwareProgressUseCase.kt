package plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.usecase

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepState
import plus.vplan.app.feature.onboarding.stage.c_indiware_setup.domain.model.IndiwareInitStepType

class TrackIndiwareProgressUseCase(
    private val onboardingRepository: OnboardingRepository
) {
    operator fun invoke(): Flow<Response<Map<IndiwareInitStepType, IndiwareInitStepState>>> = flow {
        while (true) {
            delay(500)
            val finishedJobs = onboardingRepository.getSp24UpdateJobProgress()
            if (finishedJobs is Response.Error) {
                emit(finishedJobs)
                continue
            }
            if (finishedJobs !is Response.Success) continue
            val finished = IndiwareInitStepType.entries.filter { it.name in finishedJobs.data }
            val current = IndiwareInitStepType.entries.getOrNull(finished.lastIndex + 1)
            val unfinished = IndiwareInitStepType.entries.filter { it.name !in finishedJobs.data }.let {
                if (current != null) it.minus(current) else it
            }
            val summary =
                finished.associateWith { IndiwareInitStepState.SUCCESS } +
                        (current?.let { mapOf(it to IndiwareInitStepState.IN_PROGRESS) } ?: emptyMap()) +
                        unfinished.associateWith { IndiwareInitStepState.NOT_STARTED }
            emit(Response.Success(summary))
            if (summary.all { it.value == IndiwareInitStepState.SUCCESS }) break
        }
    }.distinctUntilChanged()
}