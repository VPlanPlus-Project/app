package plus.vplan.app.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbTimetableLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableGroup
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableRoom
import plus.vplan.app.data.source.database.model.database.crossovers.DbTimetableTeacher
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.TimetableRepository

class TimetableRepositoryImpl(
    private val vppDatabase: VppDatabase
) : TimetableRepository {
    override suspend fun insertNewTimetable(schoolId: Int, lessons: List<Lesson.TimetableLesson>) {
        val currentVersion = vppDatabase.keyValueDao.get(Keys.TIMETABLE_VERSION).first()?.toIntOrNull() ?: -1
        val newVersion = currentVersion + 1
        vppDatabase.timetableDao.upsert(
            lessons = lessons.map { lesson ->
                DbTimetableLesson(
                    id = lesson.id,
                    dayOfWeek = lesson.date.dayOfWeek,
                    weekId = lesson.week.id,
                    lessonTimeId = lesson.lessonTime.id,
                    subject = lesson.subject,
                    version = newVersion
                )
            },
            groupCrossovers = lessons.flatMap { lesson ->
                lesson.groups.map { group ->
                    DbTimetableGroup(
                        timetableLessonId = lesson.id,
                        groupId = group.id
                    )
                }
            },
            teacherCrossovers = lessons.flatMap { lesson ->
                lesson.teachers.map { teacher ->
                    DbTimetableTeacher(
                        timetableLessonId = lesson.id,
                        teacherId = teacher.id
                    )
                }
            },
            roomCrossovers = lessons.flatMap { lesson ->
                lesson.rooms.orEmpty().map { room ->
                    DbTimetableRoom(
                        timetableLessonId = lesson.id,
                        roomId = room.id
                    )
                }
            }
        )
        vppDatabase.keyValueDao.set(Keys.TIMETABLE_VERSION, newVersion.toString())
        vppDatabase.timetableDao.deleteTimetableByVersion(currentVersion)
    }

    override suspend fun deleteAllTimetables() {
        vppDatabase.timetableDao.deleteAll()
    }

    override suspend fun deleteTimetableByVersion(version: Int) {
        vppDatabase.timetableDao.deleteTimetableByVersion(version)
    }

    override fun getTimetableForSchool(schoolId: Int): Flow<List<Lesson.TimetableLesson>> = channelFlow {
        vppDatabase.keyValueDao.get(Keys.TIMETABLE_VERSION).collectLatest { currentVersionFlow ->
            val currentVersion = currentVersionFlow?.toIntOrNull() ?: -1
            vppDatabase.timetableDao.getTimetableLessons(schoolId, currentVersion).collect { timetableLessons ->
                send(timetableLessons.map { lesson -> lesson.toModel() })
            }
        }
    }
}