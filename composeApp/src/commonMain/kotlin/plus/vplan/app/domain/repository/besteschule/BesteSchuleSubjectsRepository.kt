package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.besteschule.BesteSchuleSubject
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleSubjectsRepository {
    suspend fun getSubjectsFromApi(schulverwalterAccessToken: String): Response<List<ApiStudentData.Subject>>

    suspend fun addSubjectsToCache(subjects: Set<BesteSchuleSubject>)
    fun getSubjectFromCache(subjectId: Int): Flow<BesteSchuleSubject?>
    fun getAllFromCache(): Flow<List<BesteSchuleSubject>>

    fun getSubjects(
        responsePreference: ResponsePreference,
        contextBesteschuleAccessToken: String?,
        contextBesteschuleUserId: Int?
    ): Flow<Response<List<BesteSchuleSubject>>>
}
