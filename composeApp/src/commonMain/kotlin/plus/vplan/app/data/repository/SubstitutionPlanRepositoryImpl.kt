package plus.vplan.app.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbProfileSubstitutionPlanCache
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.Uuid

class SubstitutionPlanRepositoryImpl(
    private val vppDatabase: VppDatabase
) : SubstitutionPlanRepository {
    override suspend fun insertNewSubstitutionPlan(
        schoolId: Int,
        lessons: List<Lesson.SubstitutionPlanLesson>
    ) {
        val currentVersion = vppDatabase.keyValueDao.get(Keys.substitutionPlanVersion(schoolId)).first()?.toIntOrNull() ?: -1
        val newVersion = currentVersion + 1
        vppDatabase.substitutionPlanDao.upsert(
            lessons = lessons.map { lesson ->
                if (lesson.version.isNotEmpty()) throw IllegalArgumentException("Provided version '${lesson.version}' will not be used in the database. Insert an empty string instead.")
                DbSubstitutionPlanLesson(
                    id = lesson.id,
                    dayId = "${schoolId}/${lesson.date}",
                    lessonTimeId = lesson.lessonTime,
                    subject = lesson.subject,
                    isSubjectChanged = lesson.isSubjectChanged,
                    info = lesson.info,
                    subjectInstanceId = lesson.subjectInstance,
                    version = "${schoolId}_$newVersion",
                    isRoomChanged = lesson.isRoomChanged,
                    isTeacherChanged = lesson.isTeacherChanged
                )
            },
            groups = lessons.flatMap { lesson ->
                lesson.groups.map { group ->
                    DbSubstitutionPlanGroupCrossover(group, lesson.id)
                }
            },
            teachers = lessons.flatMap { lesson ->
                lesson.teachers.map { teacher ->
                    DbSubstitutionPlanTeacherCrossover(teacher, lesson.id)
                }
            },
            rooms = lessons.flatMap { lesson ->
                lesson.rooms.map { room ->
                    DbSubstitutionPlanRoomCrossover(room, lesson.id)
                }
            }
        )

        vppDatabase.keyValueDao.set(Keys.substitutionPlanVersion(schoolId), newVersion.toString())
        vppDatabase.substitutionPlanDao.deleteSubstitutionPlanByVersion("${schoolId}_$currentVersion")
    }

    override suspend fun deleteAllSubstitutionPlans() {
        vppDatabase.substitutionPlanDao.deleteAll()
    }

    override suspend fun deleteSubstitutionPlansByVersion(schoolId: Int, version: String) {
        vppDatabase.substitutionPlanDao.deleteSubstitutionPlanByVersion("${schoolId}_$version")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSubstitutionPlanBySchool(
        schoolId: Int,
        date: LocalDate
    ): Flow<Set<Uuid>> = vppDatabase.keyValueDao.get(Keys.substitutionPlanVersion(schoolId)).map { it?.toIntOrNull() ?: -1 }.mapLatest { version ->
        vppDatabase.substitutionPlanDao.getTimetableLessons(schoolId, "${schoolId}_$version", date).first().toSet()
    }.distinctUntilChanged()

    override fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?> {
        return vppDatabase.substitutionPlanDao.getById(id).map { it?.toModel() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getForProfile(profile: Profile, date: LocalDate): Flow<Set<Uuid>> {
        return vppDatabase.substitutionPlanDao.getForProfile(profile.id, date).mapLatest { it.map { profileSubstitutionPlanCache -> profileSubstitutionPlanCache.substitutionPlanLessonId }.toSet() }
    }

    override suspend fun dropCacheForProfile(profileId: Uuid) {
        vppDatabase.profileSubstitutionPlanCacheDao.deleteCacheForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, substitutionLessonIds: List<Uuid>) {
        vppDatabase.profileSubstitutionPlanCacheDao.upsert(substitutionLessonIds.map { DbProfileSubstitutionPlanCache(profileId, it) })
    }
}