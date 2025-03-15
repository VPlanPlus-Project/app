package plus.vplan.app.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbProfileTimetableCache
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.TimetableRepository
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

    override fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>> = channelFlow {
        vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).collectLatest { currentVersionFlow ->
            val currentVersion = currentVersionFlow?.toIntOrNull() ?: -1
            send(vppDatabase.timetableDao.getTimetableLessons(schoolId, "${schoolId}_$currentVersion").first().map { it.toModel() })
        }
    }

    override fun getById(id: Uuid): Flow<Lesson.TimetableLesson?> {
        return vppDatabase.timetableDao.getById(id.toString()).map { it?.toModel() }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getForSchool(schoolId: Int, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>> = vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).flatMapLatest { versionFlow ->
        val currentVersion = versionFlow?.toIntOrNull() ?: -1
        val weeks = vppDatabase.timetableDao.getWeekIds("${schoolId}_$currentVersion", weekIndex).ifEmpty { return@flatMapLatest flowOf(emptySet()) }
        vppDatabase.timetableDao.getTimetableLessons(schoolId, "${schoolId}_$currentVersion", weeks.last(), dayOfWeek).map { it.toSet() }
    }.distinctUntilChanged()

    override fun getForProfile(profile: Profile, weekIndex: Int, dayOfWeek: DayOfWeek): Flow<Set<Uuid>> = channelFlow {
        val school = profile.getSchool().getFirstValue()!!
        vppDatabase.keyValueDao.get(Keys.timetableVersion(school.id)).collectLatest { versionFlow ->
            val currentVersion = versionFlow?.toIntOrNull() ?: -1
            val weeks = vppDatabase.timetableDao.getWeekIds("${school.id}_$currentVersion", weekIndex).ifEmpty { trySend(emptySet()); return@collectLatest }
            vppDatabase.timetableDao.getLessonsForProfile(profile.id, weeks.last(), dayOfWeek).collectLatest {
                trySend(it.map { it.timetableLessonId }.toSet())
            }
        }
    }.distinctUntilChanged()

    override suspend fun dropCacheForProfile(profileId: Uuid) {
        vppDatabase.profileTimetableCacheDao.deleteCacheForProfile(profileId)
    }

    override suspend fun createCacheForProfile(profileId: Uuid, timetableLessonIds: List<Uuid>) {
        vppDatabase.profileTimetableCacheDao.upsert(timetableLessonIds.map { DbProfileTimetableCache(profileId, it) })
    }
}