@file:OptIn(ExperimentalCoroutinesApi::class)

package plus.vplan.app.core.data.teacher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import plus.vplan.app.core.database.dao.TeacherDao
import plus.vplan.app.core.database.model.database.DbTeacher
import plus.vplan.app.core.database.model.database.DbTeacherAlias
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Teacher
import kotlin.time.Clock
import kotlin.uuid.Uuid

class TeacherRepositoryImpl(
    private val teacherDao: TeacherDao,
    private val applicationScope: CoroutineScope,
): TeacherRepository {

    private val bySchoolCache = mutableMapOf<Uuid, Flow<List<Teacher>>>()
    private val allCache: Flow<List<Teacher>> by lazy {
        teacherDao.getAll()
            .map { teachers -> teachers.map { it.toModel() } }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
            .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
    }

    override fun getByIds(identifiers: Set<Alias>): Flow<Teacher?> {
        if (identifiers.isEmpty()) throw IllegalArgumentException("Identifiers cannot be empty")

        return findLocalIdByIdentifier(identifiers)
            .flatMapLatest { id ->
                id?.let { teacherDao.findById(id).map { it?.toModel() }.distinctUntilChanged() } ?: flowOf(null)
            }
            .flowOn(Dispatchers.Default)
    }

    override fun getBySchool(school: School): Flow<List<Teacher>> {
        return bySchoolCache.getOrPut(school.id) {
            teacherDao.getBySchool(school.id)
                .map { teachers -> teachers.map { it.toModel() } }
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .shareIn(applicationScope, SharingStarted.WhileSubscribed(5_000L), replay = 1)
        }
    }

    override fun getAll(): Flow<List<Teacher>> = allCache

    @Deprecated("Use aliases")
    override fun getByLocalId(id: Uuid): Flow<Teacher?> {
        return teacherDao.findById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    private fun findLocalIdByIdentifier(identifiers: Set<Alias>): Flow<Uuid?> {
        return flow {
            emit(
                identifiers.firstNotNullOfOrNull { alias ->
                    teacherDao.getIdByAlias(
                        value = alias.value,
                        provider = alias.provider,
                        version = alias.version
                    )
                }
            )
        }.flowOn(Dispatchers.Default)
    }

    override suspend fun save(teacher: Teacher): Teacher {
        val id = findLocalIdByIdentifier(teacher.aliases).first() ?: Uuid.random()
        teacherDao.upsertTeacher(
            teacher = DbTeacher(
                id = id,
                schoolId = teacher.school.id,
                name = teacher.name,
                cachedAt = Clock.System.now()
            ),
            aliases = teacher.aliases.map { DbTeacherAlias.fromAlias(it, id) }
        )

        return teacherDao.findById(id).first()!!.toModel()
    }
}