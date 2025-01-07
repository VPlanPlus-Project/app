package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class SelectProfileUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val keyValueRepository: KeyValueRepository
) {
    suspend operator fun invoke(
        onboardingProfile: OnboardingProfile,
        defaultLessons: Map<DefaultLesson, Boolean> = emptyMap()
    ) {
        onboardingRepository.setSelectedProfile(onboardingProfile)
        val profile = when (onboardingProfile) {
            is OnboardingProfile.StudentProfile -> {
                val group = groupRepository.getById(onboardingProfile.id).first().toValueOrNull()!!
                profileRepository.upsert(
                    group = group,
                    disabledDefaultLessons = defaultLessons.filterValues { !it }.keys.toList()
                )
            }
            is OnboardingProfile.TeacherProfile -> {
                val teacher = teacherRepository.getById(onboardingProfile.id).first().toValueOrNull()!!
                profileRepository.upsert(teacher)
            }
            is OnboardingProfile.RoomProfile -> {
                val room = roomRepository.getById(onboardingProfile.id).first()!!
                profileRepository.upsert(room)
            }
        }
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())
    }
}