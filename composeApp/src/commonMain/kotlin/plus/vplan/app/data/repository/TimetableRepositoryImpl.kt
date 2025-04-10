@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
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
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class TimetableRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TimetableRepository {
    override suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>) {
        val currentVersion = vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).first()?.toIntOrNull() ?: -1
        val newVersion = currentVersion + 1
        val versionString = "${schoolId}_$newVersion"
        vppDatabase.timetableDao.deleteTimetableByVersion(versionString)
        vppDatabase.timetableDao.upsert(
            lessons = lessons.map { lesson ->
                if (lesson.version.isNotEmpty()) throw IllegalArgumentException("Provided version '${lesson.version}' will not be used in the database. Insert an empty string instead.")
                DbTimetableLesson(
                    id = lesson.id,
                    dayOfWeek = lesson.dayOfWeek,
                    weekId = lesson.week,
                    weekType = lesson.weekType,
                    lessonTimeId = lesson.lessonTimeId,
                    subject = lesson.subject,
                    version = versionString
                )
            },
            groupCrossovers = lessons.flatMap { lesson ->
                lesson.groupIds.map { group ->
                    DbTimetableGroupCrossover(
                        timetableLessonId = lesson.id,
                        groupId = group
                    )
                }
            },
            teacherCrossovers = lessons.flatMap { lesson ->
                lesson.teacherIds.map { teacher ->
                    DbTimetableTeacherCrossover(
                        timetableLessonId = lesson.id,
                        teacherId = teacher
                    )
                }
            },
            roomCrossovers = lessons.flatMap { lesson ->
                lesson.roomIds.orEmpty().map { room ->
                    DbTimetableRoomCrossover(
                        timetableLessonId = lesson.id,
                        roomId = room
                    )
                }
            }
        )
        vppDatabase.keyValueDao.set(Keys.timetableVersion(schoolId), newVersion.toString())
        vppDatabase.timetableDao.deleteTimetableByVersion("${schoolId}_$currentVersion")
    }

    override suspend fun deleteAllTimetables() {
        vppDatabase.timetableDao.deleteAll()
    }

    override suspend fun deleteTimetableByVersion(schoolId: Int, version: Int) {
        vppDatabase.timetableDao.deleteTimetableByVersion("${schoolId}_$version")
    }

    override suspend fun getTimetableForSchool(schoolId: Int, versionString: String): List<Lesson.TimetableLesson> {
        return vppDatabase.timetableDao.getTimetableLessons(schoolId, versionString).first().map { it.toModel() }
    }

    override fun getById(id: Uuid): Flow<Lesson.TimetableLesson?> {
        return vppDatabase.timetableDao.getById(id.toString()).map { it?.toModel() }
    }

    override suspend fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek, versionString: String): Set<Uuid> {
        val weeks = vppDatabase.timetableDao.getWeekIds(versionString, weekIndex).ifEmpty { return emptySet() }
        return vppDatabase.timetableDao.getTimetableLessons(schoolId, versionString, weeks.last(), dayOfWeek).first().toSet()
    }

    override suspend fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek, versionString: String): Set<Uuid> {
        val weeks = vppDatabase.timetableDao.getWeekIds(versionString, weekIndex).ifEmpty { return emptySet() }
        return vppDatabase.timetableDao.getLessonsForProfile(profile.id, weeks.last(), dayOfWeek).first().map { it.timetableLessonId }.toSet()
    }

    override suspend fun dropCacheForProfile(profileId: Uuid) {
        vppDatabase.profileTimetableCacheDao.deleteCacheForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, timetableLessonIds: List<Uuid>) {
        vppDatabase.profileTimetableCacheDao.upsert(timetableLessonIds.map { DbProfileTimetableCache(profileId, it) })
    }
}