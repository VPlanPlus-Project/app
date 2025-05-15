@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbProfileTimetableCache
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimetableRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TimetableRepository {

    override suspend fun upsertLessons(schoolId: Int, lessons: List<Lesson.TimetableLesson>, profiles: List<Profile.StudentProfile>) {
        vppDatabase.timetableDao.replaceForSchool(
            schoolId = schoolId,
            lessons = lessons.map { lesson ->
                DbTimetableLesson(
                    id = lesson.id,
                    dayOfWeek = lesson.dayOfWeek,
                    weekId = lesson.week,
                    weekType = lesson.weekType,
                    lessonTimeId = lesson.lessonTimeId,
                    subject = lesson.subject
                )
            },
            groups = lessons.flatMap { lesson ->
                lesson.groupIds.map { group ->
                    DbTimetableGroupCrossover(
                        timetableLessonId = lesson.id,
                        groupId = group
                    )
                }
            },
            teachers = lessons.flatMap { lesson ->
                lesson.teacherIds.map { teacher ->
                    DbTimetableTeacherCrossover(
                        timetableLessonId = lesson.id,
                        teacherId = teacher
                    )
                }
            },
            rooms = lessons.flatMap { lesson ->
                lesson.roomIds.orEmpty().map { room ->
                    DbTimetableRoomCrossover(
                        timetableLessonId = lesson.id,
                        roomId = room
                    )
                }
            },
            profileIndex = profiles.flatMap { profile ->
                lessons
                    .filter { lesson -> lesson.isRelevantForProfile(profile) }
                    .map {
                        DbProfileTimetableCache(
                            profileId = profile.id,
                            timetableLessonId = it.id
                        )
                    }
            }
        )
    }

    override suspend fun deleteAllTimetables() {
        vppDatabase.timetableDao.deleteAll()
    }

    override suspend fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>> {
        return vppDatabase.timetableDao.getBySchool(schoolId).map { it.map { l -> l.toModel() } }
    }

    override fun getById(id: Uuid): Flow<Lesson.TimetableLesson?> {
        return vppDatabase.timetableDao.getById(id.toString()).map { it?.toModel() }
    }

    override suspend fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>> {
        val weeks = vppDatabase.timetableDao.getWeekIds(weekIndex).ifEmpty { return flowOf(emptySet()) }
        return vppDatabase.timetableDao.getBySchool(schoolId, weeks.last(), dayOfWeek).map { it.toSet() }.distinctUntilChanged()
    }

    override suspend fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>> {
        val weeks = vppDatabase.timetableDao.getWeekIds(weekIndex).ifEmpty { return flowOf(emptySet()) }
        return vppDatabase.timetableDao.getLessonsForProfile(profile.id, weeks.last(), dayOfWeek).map { it.map { it.timetableLessonId }.toSet() }.distinctUntilChanged()
    }

    override suspend fun dropCacheForProfile(profileId: Uuid) {
        vppDatabase.profileTimetableCacheDao.deleteCacheForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, timetableLessonIds: List<Uuid>) {
        vppDatabase.profileTimetableCacheDao.upsert(timetableLessonIds.map { DbProfileTimetableCache(profileId, it) })
    }
}