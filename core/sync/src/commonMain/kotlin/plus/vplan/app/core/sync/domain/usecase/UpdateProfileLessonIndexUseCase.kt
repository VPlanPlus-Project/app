package plus.vplan.app.core.sync.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.substitution_plan.SubstitutionPlanRepository
import plus.vplan.app.core.data.timetable.TimetableRepository
import plus.vplan.app.core.model.Profile

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
                if (profile.group.id !in lesson.groups.map { it.id }) return@filter false
                if (lesson.subjectInstance != null && profile.subjectInstanceConfiguration.toList().firstOrNull { it.first.id == lesson.subjectInstance!!.id }?.second == false) return@filter false
            } else if (profile is Profile.TeacherProfile) {
                if (profile.teacher.id !in lesson.teachers.map { it.id }) return@filter false
            }

            return@filter true
        }
        substitutionPlanRepository.replaceLessonIndex(profile.id, substitutionPlanLessons.map { it.id }.toSet())

        val timetableLessons = timetableRepository.getTimetableForSchool(
            schoolId = profile.school.id,
            version = timetableVersion
        ).first().filter { lesson ->
            if (profile is Profile.StudentProfile) {
                if (profile.group.id !in lesson.groups.map { it.id }) return@filter false
            } else if (profile is Profile.TeacherProfile) {
                if (profile.teacher.id !in lesson.teachers.map { it.id }) return@filter false
            }

            return@filter true
        }
        timetableRepository.replaceLessonIndex(profile.id, timetableLessons.map { it.id }.toSet())
    }
}
