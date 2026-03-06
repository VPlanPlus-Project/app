package plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile

class BuildProfileOptionsFromLocalDataUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) {
    suspend operator fun invoke(school: School.AppSchool): List<OnboardingProfile> {
        val groups = groupRepository.getBySchool(school).first()
        val teachers = teacherRepository.getBySchool(school).first()

        val studentProfiles = groups.map { group ->
            val subjectInstances = subjectInstanceRepository.getByGroup(group).first()
            OnboardingProfile.StudentProfile(
                name = group.name,
                alias = group.aliases.first { it.provider == AliasProvider.Sp24 },
                subjectInstances = subjectInstances
            )
        }

        val teacherProfiles = teachers.map { teacher ->
            OnboardingProfile.TeacherProfile(
                name = teacher.name,
                alias = teacher.aliases.first { it.provider == AliasProvider.Sp24 }
            )
        }

        return studentProfiles + teacherProfiles
    }
}
