package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.core.model.Response
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.domain.model.besteschule.api.ApiStudentData
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleIntervalsRepository {
    /**
     * Uses [BesteSchuleApiRepository.getStudentData] in the background
     */
    suspend fun getIntervalsFromApi(schulverwalterAccessToken: String, withCache: Boolean): Response<List<ApiStudentData.Interval>>
    suspend fun addIntervalsToCache(intervals: Set<BesteSchuleInterval>)
    fun getIntervalFromCache(intervalId: Int): Flow<BesteSchuleInterval?>
    fun getAllFromCache(): Flow<List<BesteSchuleInterval>>


    /**
     * @param contextBesteschuleAccessToken If a network request is being made, this is required to get the intervals from the API.
     * @param contextBesteschuleUserId If set, used as a filter.
     */
    fun getIntervals(
        responsePreference: ResponsePreference = ResponsePreference.Fast,
        contextBesteschuleAccessToken: String? = null,
        contextBesteschuleUserId: Int? = null,
        withCache: Boolean = true
    ): Flow<Response<List<BesteSchuleInterval>>>
}