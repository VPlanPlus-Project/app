package plus.vplan.app.feature.onboarding.domain.repository

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState

interface OnboardingRepository {
    suspend fun clear()
    suspend fun startSp24Onboarding(
        sp24Id: Int,
    )
    suspend fun getSp24OnboardingSchool(): Flow<CurrentOnboardingSchool?>

    suspend fun setSp24Credentials(
        username: String,
        password: String
    )
    suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?)
    fun getSp24CredentialsState(): Flow<Sp24CredentialsState>
    suspend fun clearSp24Credentials()

    suspend fun getSp24Credentials(): Sp24Credentials?
}

data class Sp24Credentials(
    val username: String,
    val password: String
)

data class CurrentOnboardingSchool(
    val sp24Id: Int,
    val schoolId: Int? = null,
    val schoolName: String? = null
)