@file:OptIn(ExperimentalUuidApi::class)

package plus.vplan.app.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import plus.vplan.app.data.source.database.VppDatabase
import plus.vplan.app.data.source.database.model.database.DbProfileSubstitutionPlanCache
import plus.vplan.app.data.source.database.model.database.DbSubstitutionPlanLesson
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanGroupCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanRoomCrossover
import plus.vplan.app.data.source.database.model.database.crossovers.DbSubstitutionPlanTeacherCrossover
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class SubstitutionPlanRepositoryImpl(
    private val vppDatabase: VppDatabase
) : SubstitutionPlanRepository {
    override suspend fun deleteAllSubstitutionPlans() {
        vppDatabase.substitutionPlanDao.deleteAll()
    }

    override suspend fun upsertLessons(schoolId: Uuid, date: LocalDate, lessons: List<Lesson.SubstitutionPlanLesson>, profiles: List<Profile.StudentProfile>) {
        vppDatabase.substitutionPlanDao.replaceForDay(
            schoolId = schoolId,
            date = date,
            lessons = lessons.map { lesson ->
                DbSubstitutionPlanLesson(
                    id = lesson.id,
                    dayId = "${schoolId}/${lesson.date}",
                    lessonTimeId = lesson.lessonTimeId,
                    subject = lesson.subject,
                    isSubjectChanged = lesson.isSubjectChanged,
                    info = lesson.info,
                    subjectInstanceId = lesson.subjectInstanceId,
                    isRoomChanged = lesson.isRoomChanged,
                    isTeacherChanged = lesson.isTeacherChanged
                )
            },
            groups = lessons.flatMap { lesson ->
                lesson.groupIds.map { group ->
                    DbSubstitutionPlanGroupCrossover(group, lesson.id)
                }
            },
            teachers = lessons.flatMap { lesson ->
                lesson.teacherIds.map { teacher ->
                    DbSubstitutionPlanTeacherCrossover(teacher, lesson.id)
                }
            },
            rooms = lessons.flatMap { lesson ->
                lesson.roomIds.map { room ->
                    DbSubstitutionPlanRoomCrossover(room, lesson.id)
                }
            },
            profileIndex = profiles.flatMap { profile ->
                lessons
                    .filter { lesson -> lesson.isRelevantForProfile(profile) }
                    .map {
                        DbProfileSubstitutionPlanCache(
                            profileId = profile.id,
                            substitutionPlanLessonId = it.id
                        )
                    }
            }
        )
    }

    override suspend fun replaceLessonIndex(profileId: Uuid, lessonIds: Set<Uuid>) {
        vppDatabase.substitutionPlanDao.replaceIndex(lessonIds.map {
            DbProfileSubstitutionPlanCache(
                profileId = profileId,
                substitutionPlanLessonId = it
            )
        })
    }

    override suspend fun getSubstitutionPlanBySchool(schoolId: Uuid, date: LocalDate): Flow<Set<Uuid>> {
        return vppDatabase.substitutionPlanDao.getTimetableLessons(schoolId, date).map { it.toSet() }.distinctUntilChanged()
    }

    override suspend fun getForProfile(profile: Profile, date: LocalDate): Flow<Set<Uuid>> {
        return vppDatabase.substitutionPlanDao.getForProfile(profile.id, date).map { it.map { profileSubstitutionPlanCache -> profileSubstitutionPlanCache.substitutionPlanLessonId }.toSet() }.distinctUntilChanged()
    }

    override suspend fun getAll(): Set<Uuid> {
        return vppDatabase.substitutionPlanDao.getAll().first().map { it.substitutionPlanLesson.id }.toSet()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getSubstitutionPlanBySchool(schoolId: Uuid): Flow<Set<Lesson.SubstitutionPlanLesson>> {
        return vppDatabase.substitutionPlanDao.getTimetableLessons(schoolId).map { items -> items.map { it.toModel() }.toSet() }.distinctUntilChanged()
    }

    override fun getById(id: Uuid): Flow<Lesson.SubstitutionPlanLesson?> {
        return vppDatabase.substitutionPlanDao.getById(id).map { it?.toModel() }
    }
}