package plus.vplan.app.feature.onboarding.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.request.basicAuth
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import plus.vplan.app.VPP_SP24_URL
import plus.vplan.app.data.repository.ResponseDataWrapper
import plus.vplan.app.data.source.network.saveRequest
import plus.vplan.app.data.source.network.toResponse
import plus.vplan.app.domain.data.Response
import plus.vplan.app.feature.onboarding.data.source.database.OnboardingDatabase
import plus.vplan.app.feature.onboarding.domain.repository.CurrentOnboardingSchool
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.domain.repository.Sp24Credentials
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.Sp24CredentialsState
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class OnboardingRepositoryImpl(
    private val onboardingDatabase: OnboardingDatabase,
    private val httpClient: HttpClient
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

    override suspend fun startSp24UpdateJob(): Response<String> {
        val sp24Id = onboardingDatabase.keyValueDao.get("indiware.sp24_id").first() ?: return Response.Error.Other("schoolId is null")
        val username = onboardingDatabase.keyValueDao.get("indiware.username").first() ?: return Response.Error.Other("username is null")
        val password = onboardingDatabase.keyValueDao.get("indiware.password").first() ?: return Response.Error.Other("password is null")
        return saveRequest {
            val result = httpClient.get {
                url("$VPP_SP24_URL/school/sp24/$sp24Id/initialize")
                basicAuth("$username@$sp24Id", password)
            }
            if (result.status.isSuccess()) {
                val jobId = ResponseDataWrapper.fromJson<String>(result.bodyAsText())
                    ?: return Response.Error.ParsingError(result.bodyAsText())
                onboardingDatabase.keyValueDao.insert("indiware.job_id", jobId)
                return Response.Success(jobId)
            }
            return result.toResponse()
        }
    }

    override suspend fun getSp24UpdateJobProgress(): Response<List<String>> {
        val jobId = onboardingDatabase.keyValueDao.get("indiware.job_id").first() ?: return Response.Error.Other("jobId is null")
        val username = onboardingDatabase.keyValueDao.get("indiware.username").first() ?: return Response.Error.Other("username is null")
        val password = onboardingDatabase.keyValueDao.get("indiware.password").first() ?: return Response.Error.Other("password is null")
        val sp24Id = onboardingDatabase.keyValueDao.get("indiware.sp24_id").first() ?: return Response.Error.Other("schoolId is null")
        return saveRequest {
            val response = httpClient.get {
                url("$VPP_SP24_URL/school/sp24/$sp24Id/status/$jobId")
                basicAuth("$username@$sp24Id", password)
            }
            if (response.status.isSuccess()) {
                val data = ResponseDataWrapper.fromJson<Sp24UpdateJobWrapper>(response.bodyAsText())
                    ?: return Response.Error.ParsingError(response.bodyAsText())

                return Response.Success(data.log.map { it.code })
            }
            return response.toResponse()
        }
    }

    override suspend fun setSelectedProfile(onboardingProfile: OnboardingProfile) {
        onboardingDatabase.keyValueDao.insert("onboarding.profile_type", onboardingProfile.type.name)
        onboardingDatabase.keyValueDao.insert("onboarding.profile_id", onboardingProfile.id.toString())
    }
}

@Serializable
data class Sp24UpdateJobWrapper(
    @SerialName("log") val log: List<Sp24UpdateJob>
)

@Serializable
data class Sp24UpdateJob(
    @SerialName("code") val code: String
)