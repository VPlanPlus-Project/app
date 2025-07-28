package plus.vplan.app.feature.onboarding.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import plus.vplan.app.feature.onboarding.data.source.database.OnboardingDatabase
import plus.vplan.app.feature.onboarding.domain.repository.CurrentOnboardingSchool
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24Credentials
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class OnboardingRepositoryImpl(
    private val onboardingDatabase: OnboardingDatabase
) : OnboardingRepository {
    override suspend fun clear() {
        onboardingDatabase.keyValueDao.delete("indiware.sp24_id")
        onboardingDatabase.keyValueDao.delete("indiware.username")
        onboardingDatabase.keyValueDao.delete("indiware.password")
        onboardingDatabase.keyValueDao.delete("indiware.credentials_state")
    }

    override suspend fun startSp24Onboarding(sp24Id: Int) {
        onboardingDatabase.keyValueDao.insert("indiware.sp24_id", sp24Id.toString())
    }

    override suspend fun getSp24OnboardingSchool(): Flow<CurrentOnboardingSchool?> {
        return combine(
            onboardingDatabase.keyValueDao.get("indiware.sp24_id"),
            onboardingDatabase.keyValueDao.get("onboarding.school_id")
        ) { sp24Id, schoolId ->
            if (sp24Id == null) return@combine null
            CurrentOnboardingSchool(sp24Id.toInt(), schoolId?.toIntOrNull())
        }
    }

    override suspend fun setSp24Credentials(username: String, password: String) {
        onboardingDatabase.keyValueDao.insert("indiware.username", username)
        onboardingDatabase.keyValueDao.insert("indiware.password", password)
    }

    override suspend fun setSp24CredentialsValid(state: Sp24CredentialsState?) {
        if (state == null) onboardingDatabase.keyValueDao.delete("indiware.credentials_state")
        else onboardingDatabase.keyValueDao.insert("indiware.credentials_state", state.name)
    }

    override fun getSp24CredentialsState(): Flow<Sp24CredentialsState> {
        return onboardingDatabase.keyValueDao.get("indiware.credentials_state")
            .map { Sp24CredentialsState.valueOf(it ?: "NOT_CHECKED") }
    }

    override suspend fun clearSp24Credentials() {
        onboardingDatabase.keyValueDao.delete("indiware.username")
        onboardingDatabase.keyValueDao.delete("indiware.password")
    }

    override suspend fun setSchoolId(id: Int) {
        onboardingDatabase.keyValueDao.insert("onboarding.school_id", id.toString())
    }

    override fun getSchoolId(): Flow<Int?> {
        return onboardingDatabase.keyValueDao.get("onboarding.school_id").map { it?.toIntOrNull() }
    }

    override suspend fun getSp24Credentials(): Sp24Credentials? {
        val username = onboardingDatabase.keyValueDao.get("indiware.username").first()
        val password = onboardingDatabase.keyValueDao.get("indiware.password").first()
        if (username == null || password == null) return null
        return Sp24Credentials(username, password)
    }

    override suspend fun setSelectedProfile(onboardingProfile: OnboardingProfile) {
        onboardingDatabase.keyValueDao.insert("onboarding.profile_type", onboardingProfile.type.name)
        onboardingDatabase.keyValueDao.insert("onboarding.profile_id", onboardingProfile.id.toString())
    }
}
