package plus.vplan.app.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.domain.model.OnboardingSp24State
import plus.vplan.app.feature.onboarding.stage.school_credentials.ui.Sp24CredentialsState
import plus.vplan.lib.sp24.source.Stundenplan24Client

interface OnboardingRepository {
    suspend fun reset()
    suspend fun startSp24Onboarding(
        sp24Id: Int,
    )

    fun setSp24Client(stundenplan24Client: Stundenplan24Client)
    fun getSp24Client(): Stundenplan24Client?

    suspend fun setSp24Credentials(
        username: String,
        password: String
    )

    suspend fun addProfileOptions(options: List<OnboardingProfile>)

    suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?)

    suspend fun setSchoolName(name: String?)

    suspend fun setSelectedProfile(option: OnboardingProfile)

    suspend fun getNeedToDownloadLessonData(): Boolean
    suspend fun setNeedToDownloadLessonData(needToDownload: Boolean)

    fun getState(): Flow<OnboardingSp24State>
}