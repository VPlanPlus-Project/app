package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.besteschule.BesteschuleCollectionDao
import plus.vplan.app.core.database.dao.besteschule.BesteschuleSubjectDao
import plus.vplan.app.core.database.dao.besteschule.BesteschuleTeacherDao
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleSubject
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleTeacher
import plus.vplan.app.core.model.besteschule.BesteSchuleCollection
import plus.vplan.app.network.besteschule.CollectionApi
import plus.vplan.app.network.besteschule.CollectionDto
import kotlin.time.Clock

class CollectionsRepositoryImpl(
    private val intervalsRepository: IntervalsRepository,
    private val collectionDao: BesteschuleCollectionDao,
    private val teacherDao: BesteschuleTeacherDao,
    private val subjectDao: BesteschuleSubjectDao,
    private val collectionApi: CollectionApi,
): CollectionsRepository {
    override fun getById(
        id: Int,
        forceRefresh: Boolean
    ): Flow<BesteSchuleCollection> {
        return collectionDao.getById(id).map { item ->
            if (item == null || forceRefresh) {
                val result = collectionApi.getById(id)
                teacherDao.upsert(listOf(result.teacher.toEntity()))
                subjectDao.upsert(listOf(result.subject.toEntity()))
                intervalsRepository.getById(result.interval.id).first()
                collectionDao.upsert(listOf(result.toEntity()))
                collectionDao.getById(id).first()!!.toModel()
            } else item.toModel()
        }
    }

    override fun getAll(forceRefresh: Boolean): Flow<List<BesteSchuleCollection>> {
        return collectionDao.getAll().map { items ->
            if (items.isEmpty() || forceRefresh) {
                val result = collectionApi.getAll()
                teacherDao.upsert(result.map { it.teacher.toEntity() })
                subjectDao.upsert(result.map { it.subject.toEntity() })
                result.map { it.interval.id }.distinct().forEach { intervalsRepository.getById(it).first() }
                collectionDao.upsert(result.map { it.toEntity() })
                collectionDao.getAll().first().map { it.toModel() }
            } else {
                items.map { it.toModel() }
            }
        }
    }
}

private fun CollectionDto.TeacherDto.toEntity() = DbBesteschuleTeacher(
    id = this.id,
    forename = this.forename,
    surname = this.lastname,
    localId = this.localId,
    cachedAt = Clock.System.now(),
)

private fun CollectionDto.SubjectDto.toEntity() = DbBesteschuleSubject(
    id = this.id,
    longName = this.name,
    shortName = this.localId,
    cachedAt = Clock.System.now(),
)

private fun CollectionDto.toEntity() = DbBesteSchuleCollection(
    id = this.id,
    type = this.type,
    name = this.name,
    subjectId = this.subject.id,
    givenAt = LocalDate.parse(this.givenAt),
    intervalId = this.interval.id,
    teacherId = this.teacher.id,
    cachedAt = Clock.System.now(),
)