package plus.vplan.app.core.data.besteschule

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.dao.besteschule.BesteschuleCollectionDao
import plus.vplan.app.core.database.dao.besteschule.BesteschuleGradesDao
import plus.vplan.app.core.database.dao.besteschule.BesteschuleSubjectDao
import plus.vplan.app.core.database.dao.besteschule.BesteschuleTeacherDao
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleCollection
import plus.vplan.app.core.database.model.database.besteschule.DbBesteSchuleGrade
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleSubject
import plus.vplan.app.core.database.model.database.besteschule.DbBesteschuleTeacher
import plus.vplan.app.core.model.besteschule.BesteSchuleGrade
import plus.vplan.app.network.besteschule.GradesApi
import plus.vplan.app.network.besteschule.GradesDto
import kotlin.collections.map
import kotlin.time.Clock

class GradesRepositoryImpl(
    private val gradesDao: BesteschuleGradesDao,
    private val intervalsRepository: IntervalsRepository,
    private val gradesApi: GradesApi,
    private val collectionsDao: BesteschuleCollectionDao,
    private val teachersDao: BesteschuleTeacherDao,
    private val subjectDao: BesteschuleSubjectDao,
): GradesRepository {

    override fun getById(id: Int, forceRefresh: Boolean): Flow<BesteSchuleGrade?> {
        return gradesDao.getById(id).map { item  ->
            if (item == null || forceRefresh) {
                val result = gradesApi.getById(id) ?: return@map null
                val existing = gradesDao.getAll().first()
                try {
                    teachersDao.upsert(listOf(result.teacher.toEntity()))
                    subjectDao.upsert(listOf(result.subject.toEntity()))
                    intervalsRepository.getById(result.collection.intervalId).first()
                    collectionsDao.upsert(listOf(result.collection.toEntity().copy(teacherId = result.teacher.id)))
                    gradesDao.upsert(listOf(result.toEntity(existing)))
                } catch (e: Exception) {
                    throw RuntimeException("Failed to getById", e)
                }
                return@map getById(id).first()
            } else return@map item.toModel()
        }
    }

    override fun getAll(forceRefresh: Boolean): Flow<List<BesteSchuleGrade>> {
        return gradesDao.getAll().map { items ->
            if (items.isEmpty() || forceRefresh) {
                val result = gradesApi.getAll()
                try {
                    teachersDao.upsert(result.map { it.teacher.toEntity() }.distinctBy { it.id })
                    subjectDao.upsert(result.map { it.subject.toEntity() }.distinctBy { it.id })
                    result.map { it.collection.intervalId }.distinct().forEach { intervalsRepository.getById(it).first() }
                    collectionsDao.upsert(result.map { it.collection.toEntity().copy(teacherId = it.teacher.id) }.distinctBy { it.id })
                    gradesDao.upsert(result.map { it.toEntity(items) })
                    gradesDao.getAll().first().map { it.toModel() }
                } catch (e: Exception) {
                    throw RuntimeException("Failed to getAll", e)
                }
            } else {
                items.map { it.toModel() }
            }
        }
    }

    override fun getAllForUser(
        schulverwalterUserId: Int,
        forceRefresh: Boolean
    ): Flow<List<BesteSchuleGrade>> {
        return gradesDao.getAllForUser(schulverwalterUserId).map { items ->
            if (items.isEmpty() || forceRefresh) {
                val result = gradesApi.getAllForUser(schulverwalterUserId)
                try {
                    teachersDao.upsert(result.map { it.teacher.toEntity() }.distinctBy { it.id })
                    subjectDao.upsert(result.map { it.subject.toEntity() }.distinctBy { it.id })
                    result.map { it.collection.intervalId }.distinct().forEach { intervalsRepository.getById(it).first() }
                    collectionsDao.upsert(result.map { it.collection.toEntity().copy(teacherId = it.teacher.id) }.distinctBy { it.id })
                    gradesDao.upsert(result.map { it.toEntity(items) })
                } catch (e: Exception) {
                    throw RuntimeException("Failed to getAllForUser", e)
                }
                gradesDao.getAll().first().map { it.toModel() }
            } else {
                items.map { it.toModel() }
            }
        }
    }

    override suspend fun save(grade: BesteSchuleGrade) {
        gradesDao.upsert(listOf(grade.toEntity()))
    }

    override suspend fun removeGradesForUser(userId: Int) {
        gradesDao.clearCacheForUser(userId)
    }
}

private fun GradesDto.Teacher.toEntity() = DbBesteschuleTeacher(
    id = this.id,
    localId = this.localId,
    forename = this.forename,
    surname = this.lastname,
    cachedAt = Clock.System.now(),
)

private fun GradesDto.Subject.toEntity() = DbBesteschuleSubject(
    id = this.id,
    shortName = this.localId,
    longName = this.name,
    cachedAt = Clock.System.now(),
)

private fun GradesDto.Collection.toEntity() = DbBesteSchuleCollection(
    id = this.id,
    type = this.type,
    name = this.name,
    subjectId = this.subjectId,
    givenAt = LocalDate.parse(this.givenAt),
    intervalId = this.intervalId,
    teacherId = this.teacherId,
    cachedAt = Clock.System.now(),
)

private fun GradesDto.toEntity(
    existingGrades: List<DbBesteSchuleGrade>
): DbBesteSchuleGrade {
    val regexForGradeInParentheses = "\\((.*?)\\)".toRegex()
    val matchResult = regexForGradeInParentheses.find(this.value)

    val isOptional = matchResult != null
    val value =
        if (matchResult != null) matchResult.groupValues[1]
        else if (this.value == "-") null
        else this.value

    if (matchResult != null) matchResult.groupValues[1] else this.value

    return DbBesteSchuleGrade(
        id = this.id,
        value = value,
        isOptional = isOptional,
        isSelectedForFinalGrade = existingGrades.find { it.id == this.id }?.isSelectedForFinalGrade ?: true,
        schulverwalterUserId = this.schulverwalterUserId,
        collectionId = this.collection.id,
        givenAt = LocalDate.parse(this.givenAt),
        cachedAt = Clock.System.now(),
    )
}

private fun BesteSchuleGrade.toEntity() = DbBesteSchuleGrade(
    id = this.id,
    value = this.value,
    isOptional = this.isOptional,
    isSelectedForFinalGrade = this.isSelectedForFinalGrade,
    schulverwalterUserId = this.schulverwalterUserId,
    collectionId = this.collectionId,
    givenAt = this.givenAt,
    cachedAt = Clock.System.now(),
)