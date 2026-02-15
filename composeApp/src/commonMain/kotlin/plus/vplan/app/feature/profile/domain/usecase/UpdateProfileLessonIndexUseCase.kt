package plus.vplan.app.feature.profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TimetableRepository

class UpdateProfileLessonIndexUseCase(
    private val timetableRepository: TimetableRepository,
    private val substitutionPlanRepository: SubstitutionPlanRepository
) {
    suspend operator fun invoke(profile: Profile, substitutionPlanVersion: Int, timetableVersion: Int) {
        val substitutionPlanLessons = substitutionPlanRepository.getSubstitutionPlanBySchool(
            schoolId = profile.school.id,
            version = substitutionPlanVersion
        ).first().filter { lesson ->
            if (profile is Profile.StudentProfile) {
                if (profile.group.id !in lesson.groupIds) return@filter false
                if (lesson.subjectInstanceId != null && profile.subjectInstanceConfiguration[lesson.subjectInstanceId] == false) return@filter false
            } else if (profile is Profile.TeacherProfile) {
                if (profile.teacher.id !in lesson.teacherIds) return@filter false
            }

            return@filter true
        }
        substitutionPlanRepository.replaceLessonIndex(profile.id, substitutionPlanLessons.map { it.id }.toSet())

        val timetableLessons = timetableRepository.getTimetableForSchool(
            schoolId = profile.school.id,
            version = timetableVersion
        ).first().filter { lesson ->
            if (profile is Profile.StudentProfile) {
                if (profile.group.id !in lesson.groupIds) return@filter false
            } else if (profile is Profile.TeacherProfile) {
                if (profile.teacher.id !in lesson.teacherIds) return@filter false
            }

            return@filter true
        }
        timetableRepository.replaceLessonIndex(profile.id, timetableLessons.map { it.id }.toSet())
    }
}