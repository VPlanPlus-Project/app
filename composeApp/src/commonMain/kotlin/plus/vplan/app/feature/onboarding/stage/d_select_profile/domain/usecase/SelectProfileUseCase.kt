package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.capture
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.sync.domain.usecase.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.utils.now

class SelectProfileUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase
) {
    suspend operator fun invoke(
        onboardingProfile: OnboardingProfile,
        subjectInstances: Map<SubjectInstance, Boolean> = emptyMap()
    ) {
        onboardingRepository.setSelectedProfile(onboardingProfile)
        val profile = when (onboardingProfile) {
            is OnboardingProfile.StudentProfile -> {
                val group = groupRepository.getByLocalId(groupRepository.resolveAliasToLocalId(onboardingProfile.alias)!!).first()!!
                profileRepository.upsert(
                    group = group,
                    disabledSubjectInstances = subjectInstances.filterValues { !it }.keys.toList()
                )
            }
            is OnboardingProfile.TeacherProfile -> {
                val teacher = teacherRepository.getByLocalId(teacherRepository.resolveAliasToLocalId(onboardingProfile.alias)!!).first()!!
                profileRepository.upsert(teacher)
            }
        }
        capture("CreateProfile", mapOf("school_id" to profile.getSchool().getFirstValue()!!.id, "profile_type" to profile.profileType.name, "entity_id" to onboardingProfile.alias))
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())

        (profile.getSchool().getFirstValue() as? School.AppSchool)?.let {
            val client = onboardingRepository.getSp24Client()!!
            updateTimetableUseCase(it, client, false)
            updateSubstitutionPlanUseCase(it, listOf(LocalDate.now()), client, allowNotification = false)
        }

        FullSyncUseCase.isOnboardingRunning = false
    }
}