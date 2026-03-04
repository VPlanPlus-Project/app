package plus.vplan.app.feature.onboarding.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.stundenplan24.Stundenplan24Repository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.lib.sp24.source.Authentication

class InitialiseOnboardingWithSchoolIdUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val stundenplan24Repository: Stundenplan24Repository
) {
    suspend operator fun invoke(school: School.AppSchool) {
        onboardingRepository.reset()
        onboardingRepository.startSp24Onboarding(school.sp24Id.toInt())

        val groups = groupRepository.getBySchool(school).first()
            .mapNotNull { group ->
                val groupAlias = group.aliases.firstOrNull { it.provider == AliasProvider.Sp24 }
                if (groupAlias == null) return@mapNotNull null

                val subjectInstances = subjectInstanceRepository.getByGroup(group).first()

                OnboardingProfile.StudentProfile(
                    name = group.name,
                    alias = group.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@mapNotNull null,
                    subjectInstances = subjectInstances
                )
            }
        onboardingRepository.addProfileOptions(groups)

        val teachers = teacherRepository.getBySchool(school).first()
            .mapNotNull { teacher ->
                val teacherAlias = teacher.aliases.firstOrNull { it.provider == AliasProvider.Sp24 }
                if (teacherAlias == null) return@mapNotNull null

                OnboardingProfile.TeacherProfile(
                    name = teacher.name,
                    alias = teacherAlias
                )
            }
        onboardingRepository.addProfileOptions(teachers)

        onboardingRepository.setSp24Client(stundenplan24Repository.getSp24Client(Authentication(
            school.sp24Id,
            school.username,
            school.password
        ), true))

        onboardingRepository.setNeedToDownloadLessonData(false)
    }
}