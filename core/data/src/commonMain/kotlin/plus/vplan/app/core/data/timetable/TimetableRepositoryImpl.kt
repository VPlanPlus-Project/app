package plus.vplan.app.core.data.timetable

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.core.database.VppDatabase
import plus.vplan.app.core.database.model.database.DbProfileTimetableCache
import plus.vplan.app.core.database.model.database.DbTimetable
import plus.vplan.app.core.database.model.database.DbTimetableLesson
import plus.vplan.app.core.database.model.database.DbTimetableWeekLimitation
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.core.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.Timetable
import kotlin.uuid.Uuid

class TimetableRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TimetableRepository {

    override suspend fun upsertLessons(
        timetableId: Uuid,
        lessons: List<Lesson.TimetableLesson>,
        profileMapping: Map<Profile, List<Lesson.TimetableLesson>>,
    ) {
        vppDatabase.timetableDao.replaceForTimetable(
            timetableId = timetableId,
            lessons = lessons.map { lesson ->
                DbTimetableLesson(
                    id = lesson.id,
                    dayOfWeek = lesson.dayOfWeek,
                    weekType = lesson.weekType,
                    lessonNumber = lesson.lessonNumber,
                    subject = lesson.subject,
                    timetableId = lesson.timetableId,
                    lessonTimeId = lesson.lessonTime?.id,
                )
            },
            groups = lessons.flatMap { lesson ->
                lesson.groups.map { group ->
                    DbTimetableGroupCrossover(
                        timetableLessonId = lesson.id,
                        groupId = group.id,
                    )
                }
            },
            teachers = lessons.flatMap { lesson ->
                lesson.teachers.map { teacher ->
                    DbTimetableTeacherCrossover(
                        timetableLessonId = lesson.id,
                        teacherId = teacher.id,
                    )
                }
            },
            rooms = lessons.flatMap { lesson ->
                lesson.rooms.orEmpty().map { room ->
                    DbTimetableRoomCrossover(
                        timetableLessonId = lesson.id,
                        roomId = room.id,
                    )
                }
            },
            weekLimitations = lessons.flatMap { lesson ->
                lesson.limitedToWeeks.orEmpty().map { week ->
                    DbTimetableWeekLimitation(timetableLessonId = lesson.id, weekId = week.id)
                }
            },
            index = profileMapping.flatMap { (profile, lessons) ->
                lessons.map { lesson ->
                    DbProfileTimetableCache(
                        profileId = profile.id,
                        timetableLessonId = lesson.id,
                    )
                }
            }
        )
    }

    override suspend fun deleteAllTimetables() {
        vppDatabase.timetableDao.deleteAll()
    }

    override fun getTimetableForSchool(schoolId: Uuid): Flow<List<Lesson.TimetableLesson>> {
        return vppDatabase.timetableDao.getBySchool(schoolId).map { it.map { l -> l.toModel() } }
    }

    override fun getForProfile(profile: Profile): Flow<Set<Lesson.TimetableLesson>> {
        return when (profile) {
            is Profile.StudentProfile -> vppDatabase.timetableDao.getLessonIdsByGroupAndVersion(profile.group.id)
            is Profile.TeacherProfile -> vppDatabase.timetableDao.getLessonIdsByTeacherAndVersion(profile.teacher.id)
        }
            .map { items -> items.map { it.toModel() }.toSet() }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)
    }

    override fun getById(id: Uuid): Flow<Lesson.TimetableLesson?> {
        return vppDatabase.timetableDao.getById(id.toString())
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override fun getForSchool(schoolId: Uuid, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Lesson.TimetableLesson>> {
        return vppDatabase.timetableDao.getBySchool(schoolId, weekIndex, dayOfWeek)
            .map { lessons -> lessons.map { it.toModel() }.toSet() }
            .distinctUntilChanged()
    }

    override suspend fun upsertTimetable(timetable: Timetable) {
        vppDatabase.timetableDao.upsert(
            DbTimetable(
                id = timetable.id,
                schoolId = timetable.schoolId,
                weekId = timetable.week.id,
                dataState = timetable.dataState
            )
        )
    }

    override suspend fun getTimetableData(schoolId: Uuid, weekId: String): Flow<Timetable?> {
        return vppDatabase.timetableDao.getTimetableData(schoolId, weekId)
            .map { it?.toModel() }
            .distinctUntilChanged()
    }

    override fun getTimetables(school: School.AppSchool): Flow<List<Timetable>> {
        return vppDatabase.timetableDao.getTimetables(school.id)
            .map { it.map { item -> item.toModel() } }
            .distinctUntilChanged()
    }

    override fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Lesson.TimetableLesson>> {
        return vppDatabase.timetableDao.getLessonsForProfile(profile.id, weekIndex, dayOfWeek)
            .map { lessons -> lessons.map { it.toModel() }.toSet() }
            .distinctUntilChanged()
    }
}
