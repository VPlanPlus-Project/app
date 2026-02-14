package plus.vplan.app.feature.onboarding.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.School
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.lib.sp24.source.Authentication
import kotlin.uuid.Uuid

class InitialiseOnboardingWithSchoolIdUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val stundenplan24Repository: Stundenplan24Repository
) {
    suspend operator fun invoke(schoolId: Uuid?) {
        val school = schoolId?.let { schoolRepository.getByLocalId(it).first() as? School.AppSchool }
        onboardingRepository.reset()
        if (school != null) {
            onboardingRepository.startSp24Onboarding(school.sp24Id.toInt())

            val groups = groupRepository.getBySchool(school.id).first()
                .mapNotNull { group ->
                    val groupAlias = group.aliases.firstOrNull { it.provider == AliasProvider.Sp24 }
                    if (groupAlias == null) return@mapNotNull null

                    val subjectInstances = subjectInstanceRepository.getByGroup(group.id).first()

                    OnboardingProfile.StudentProfile(
                        name = group.name,
                        alias = group.aliases.firstOrNull { it.provider == AliasProvider.Sp24 } ?: return@mapNotNull null,
                        subjectInstances = subjectInstances
                    )
                }
            onboardingRepository.addProfileOptions(groups)

            val teachers = teacherRepository.getBySchool(school.id).first()
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
}