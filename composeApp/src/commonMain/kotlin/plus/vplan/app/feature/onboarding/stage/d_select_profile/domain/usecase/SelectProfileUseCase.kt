package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateTimetableUseCase
import plus.vplan.app.utils.now

class SelectProfileUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
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
                val group = groupRepository.getById(onboardingProfile.id, false).getFirstValue()!!
                profileRepository.upsert(
                    group = group,
                    disabledSubjectInstances = subjectInstances.filterValues { !it }.keys.toList()
                )
            }
            is OnboardingProfile.TeacherProfile -> {
                val teacher = teacherRepository.getById(onboardingProfile.id, false).getFirstValue()!!
                profileRepository.upsert(teacher)
            }
            is OnboardingProfile.RoomProfile -> {
                val room = roomRepository.getById(onboardingProfile.id, false).getFirstValue()!!
                profileRepository.upsert(room)
            }
        }
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())
        updateProfileLessonIndexUseCase(profile)

        (profile.getSchool().getFirstValue() as? School.IndiwareSchool)?.let {
            updateTimetableUseCase(it, false)
            updateSubstitutionPlanUseCase(it, listOf(LocalDate.now()), allowNotification = false)
        }
    }
}