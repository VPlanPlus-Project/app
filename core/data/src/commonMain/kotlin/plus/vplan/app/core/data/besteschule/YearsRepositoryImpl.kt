package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.besteschule.BesteschuleYearDao
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleYear
import plus.vplan.app.core.model.besteschule.BesteSchuleYear
import plus.vplan.app.network.besteschule.YearApi
import plus.vplan.app.network.besteschule.YearDto
import kotlin.time.Clock

class YearsRepositoryImpl(
    private val besteschuleYearDao: BesteschuleYearDao,
    private val yearApi: YearApi,
): YearsRepository {

    override fun getById(
        id: Int,
        forceRefresh: Boolean,
    ): Flow<BesteSchuleYear?> {
        return besteschuleYearDao.getById(id).map {
            if (it == null || forceRefresh) {
                val item = yearApi.getById(id)?.toEntity()
                if (item != null) besteschuleYearDao.upsert(listOf(item))
                item?.toModel()
            } else it.toModel()
        }
    }

    override fun getAll(forceRefresh: Boolean): Flow<List<BesteSchuleYear>> {
        return besteschuleYearDao.getAll().map { items ->
            if (items.isEmpty() || forceRefresh) {
                val apiItems = yearApi.getAll().map { it.toEntity() }
                besteschuleYearDao.upsert(apiItems)
                items.map { it.toModel() }
            } else {
                items.map { it.toModel() }
            }
        }
    }
}

fun YearDto.toEntity() = DbBesteschuleYear(
    id = this.id,
    name = this.name,
    from = LocalDate.parse(this.from),
    to = LocalDate.parse(this.to),
    cachedAt = Clock.System.now(),
)
