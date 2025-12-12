package plus.vplan.app.domain.repository.besteschule

import kotlinx.coroutines.flow.Flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import plus.vplan.app.domain.model.besteschule.api.ApiYear
import plus.vplan.app.domain.repository.base.ResponsePreference

interface BesteSchuleYearsRepository {
    suspend fun getYearsFromApi(schulverwalterAccessToken: String): Response<List<ApiYear>>
    suspend fun addYearsToCache(years: Set<BesteSchuleYear>)
    fun getYearFromCache(yearId: Int): Flow<BesteSchuleYear?>


    /**
     * @param contextBesteschuleAccessToken If a network request is being made, this is required to get the years from the API.
     */
    fun getYears(
        responsePreference: ResponsePreference = ResponsePreference.Fast,
        contextBesteschuleAccessToken: String? = null
    ): Flow<Response<List<BesteSchuleYear>>>
}