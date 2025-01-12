package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacherCrossover
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.TimetableRepository
import kotlin.uuid.Uuid

class TimetableRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TimetableRepository {
    override suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>) {
        val currentVersion = vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).first()?.toIntOrNull() ?: -1
        val newVersion = currentVersion + 1
        vppDatabase.timetableDao.upsert(
            lessons = lessons.map { lesson ->
                DbTimetableLesson(
                    id = lesson.id,
                    dayOfWeek = lesson.dayOfWeek,
                    weekId = lesson.week,
                    weekType = lesson.weekType,
                    lessonTimeId = lesson.lessonTime,
                    subject = lesson.subject,
                    version = "${schoolId}_$newVersion"
                )
            },
            groupCrossovers = lessons.flatMap { lesson ->
                lesson.groups.map { group ->
                    DbTimetableGroupCrossover(
                        timetableLessonId = lesson.id,
                        groupId = group
                    )
                }
            },
            teacherCrossovers = lessons.flatMap { lesson ->
                lesson.teachers.map { teacher ->
                    DbTimetableTeacherCrossover(
                        timetableLessonId = lesson.id,
                        teacherId = teacher
                    )
                }
            },
            roomCrossovers = lessons.flatMap { lesson ->
                lesson.rooms.orEmpty().map { room ->
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
            vppDatabase.timetableDao.getTimetableLessons(schoolId, "${schoolId}_$currentVersion").collect { timetableLessons ->
                send(timetableLessons.map { lesson -> lesson.toModel() })
            }
        }
    }

    override fun getById(id: Uuid): Flow<Lesson.TimetableLesson?> = channelFlow {
        vppDatabase.timetableDao.getById(id.toHexString()).collectLatest {
            if (it.isEmpty()) return@collectLatest send(null)
            val schoolId = vppDatabase.weekDao.getById(it.first().timetableLesson.weekId).first()!!.schoolId
            vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).collectLatest { versionFlow ->
                val currentVersion = versionFlow?.toIntOrNull() ?: -1
                vppDatabase.timetableDao.getById(id.toHexString(), "${schoolId}_$currentVersion").collect { timetableLesson ->
                    send(timetableLesson?.toModel())
                }
            }
        }
    }

    override fun getForSchool(schoolId: Int, minWeekIndex: Int, dayOfWeek: DayOfWeek): Flow<List<Uuid>> = channelFlow {
        vppDatabase.keyValueDao.get(Keys.timetableVersion(schoolId)).collectLatest { versionFlow ->
            val currentVersion = versionFlow?.toIntOrNull() ?: -1
            val allowedWeeks = vppDatabase.weekDao.getBySchool(schoolId).first().filter { it.weekIndex >= minWeekIndex }
            vppDatabase.timetableDao.getTimetableLessons(schoolId, "${schoolId}_$currentVersion", allowedWeeks.map { it.id }).collectLatest collectLessons@{
                send(it.map { it.timetableLesson.id })
            }
        }
    }
}