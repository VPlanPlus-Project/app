package plus.vplan.app.core.data.substitution_plan

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbProfileSubstitutionPlanCache
import plus.vplan.app.core.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import kotlin.uuid.Uuid

class SubstitutionPlanRepositoryImpl(
    private val vppDatabase: VppDatabase
) : SubstitutionPlanRepository {
    override suspend fun deleteAllSubstitutionPlans() {
        vppDatabase.substitutionPlanDao.deleteAll()
    }

    override suspend fun replaceLessons(
        date: LocalDate,
        schoolId: Uuid,
        lessons: List<Lesson.SubstitutionPlanLesson>,
        profileMappings: Map<Profile, List<Lesson.SubstitutionPlanLesson>>
    ) {
        vppDatabase.substitutionPlanDao.insertDayVersion(
            schoolId = schoolId,
            date = date,
            lessons = lessons.map { lesson ->
                DbSubstitutionPlanLesson(
                    id = lesson.id,
                    dayId = Day.buildId(schoolId, date),
                    lessonNumber = lesson.lessonNumber,
                    subject = lesson.subject,
                    isSubjectChanged = lesson.isSubjectChanged,
                    info = lesson.info,
                    subjectInstanceId = lesson.subjectInstance?.id,
                    isRoomChanged = lesson.isRoomChanged,
                    isTeacherChanged = lesson.isTeacherChanged,
                    lessonTimeId = lesson.lessonTime?.id,
                    version = 0
                )
            },
            groups = lessons.flatMap { lesson ->
                lesson.groups.map { group ->
                    DbSubstitutionPlanGroupCrossover(
                        groupId = group.id,
                        substitutionPlanLessonId = lesson.id
                    )
                }
            },
            teachers = lessons.flatMap { lesson ->
                lesson.teachers.map { teacher ->
                    DbSubstitutionPlanTeacherCrossover(
                        teacherId = teacher.id,
                        substitutionPlanLessonId = lesson.id
                    )
                }
            },
            rooms = lessons.flatMap { lesson ->
                lesson.rooms.map { room ->
                    DbSubstitutionPlanRoomCrossover(
                        roomId = room.id,
                        substitutionPlanLessonId = lesson.id
                    )
                }
            },
            index = profileMappings.map { (profile, lessons) ->
                lessons.map { lesson ->
                    DbProfileSubstitutionPlanCache(
                        profileId = profile.id,
                        substitutionPlanLessonId = lesson.id,
                    )
                }
            }.flatten()
        )
    }

    override fun getSubstitutionPlanBySchool(schoolId: Uuid, date: LocalDate): Flow<List<Lesson.SubstitutionPlanLesson>> {
        return vppDatabase.substitutionPlanDao.getTimetableLessons(schoolId, date, 0).map { it.map { it.toModel() } }.distinctUntilChanged()
    }

    override fun getForProfile(profile: Profile, date: LocalDate): Flow<List<Lesson.SubstitutionPlanLesson>> {
        return vppDatabase.substitutionPlanDao.getForProfile(profile.id, date, 0)
            .map { it.map { it.toModel() } }
            .distinctUntilChanged()
    }

    override suspend fun getAll(): Set<Uuid> {
        return vppDatabase.substitutionPlanDao.getAll().first().map { it.substitutionPlanLesson.id }.toSet()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSubstitutionPlanBySchool(schoolId: Uuid): Flow<Set<Lesson.SubstitutionPlanLesson>> {
        return vppDatabase.substitutionPlanDao.getTimetableLessons(schoolId, 0)
            .map { items -> items.map { it.toModel() }.toSet() }
            .distinctUntilChanged()
    }

    override fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?> {
        return vppDatabase.substitutionPlanDao.getById(id)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }
}