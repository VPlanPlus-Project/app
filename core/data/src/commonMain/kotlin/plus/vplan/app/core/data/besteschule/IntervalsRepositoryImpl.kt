package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.besteschule.BesteschuleIntervalDao
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleInterval
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.network.besteschule.IntervalApi
import plus.vplan.app.network.besteschule.IntervalDto
import kotlin.time.Clock

class IntervalsRepositoryImpl(
    private val yearsRepository: YearsRepository,
    private val intervalApi: IntervalApi,
    private val besteschuleIntervalDao: BesteschuleIntervalDao,
) : IntervalsRepository {
    override fun getById(
        id: Int,
        forceRefresh: Boolean
    ): Flow<BesteSchuleInterval> {
        return besteschuleIntervalDao.getById(id).map {
            if (it == null || forceRefresh) {
                val item = intervalApi.getById(id)?.toEntity()

                if (item != null) {
                    yearsRepository.getById(item.yearId).first()!!
                    besteschuleIntervalDao.upsert(listOf(item))
                }
                besteschuleIntervalDao.getById(id).first()!!.toModel()

            } else it.toModel()
        }
    }

    override fun getAll(forceRefresh: Boolean): Flow<List<BesteSchuleInterval>> {
        return besteschuleIntervalDao.getAll().map { items ->
            if (items.isEmpty() || forceRefresh) {
                val items = intervalApi.getAll().map { it.toEntity() }
                items.forEach { yearsRepository.getById(it.yearId).first()!! }
                besteschuleIntervalDao.upsert(items.filter { it.includedIntervalId == null })
                besteschuleIntervalDao.upsert(items.filter { it.includedIntervalId != null })
                besteschuleIntervalDao.getAll().first().map { it.toModel() }
            } else {
                items.map { it.toModel() }
            }
        }
    }
}

fun IntervalDto.toEntity() = DbBesteSchuleInterval(
    id = this.id,
    type = BesteSchuleInterval.Type.fromString(this.type).name,
    name = this.name,
    from = LocalDate.parse(this.from),
    to = LocalDate.parse(this.to),
    includedIntervalId = this.includedIntervalId,
    yearId = this.yearId,
    cachedAt = Clock.System.now()
)