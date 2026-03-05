package plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.data.KeyValueRepository
import plus.vplan.app.core.data.Keys
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.profile.ProfileRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.core.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import kotlin.uuid.Uuid

class SelectProfileUseCase(
    private val profileRepository: ProfileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val appScope: CoroutineScope,
) {
    suspend operator fun invoke(
        onboardingProfile: OnboardingProfile,
        subjectInstances: Map<SubjectInstance, Boolean> = emptyMap()
    ) {
        val profile = when (onboardingProfile) {
            is OnboardingProfile.StudentProfile -> {
                val group = groupRepository.getById(onboardingProfile.alias).first()!!
                Profile.StudentProfile(
                    id = Uuid.random(),
                    name = group.name,
                    group = group,
                    subjectInstanceConfiguration = subjectInstances,
                    vppId = null,
                ).also { profileRepository.save(it) }
            }
            is OnboardingProfile.TeacherProfile -> {
                val teacher = teacherRepository.getById(onboardingProfile.alias).first()!!
                Profile.TeacherProfile(
                    id = Uuid.random(),
                    name = teacher.name,
                    teacher = teacher,
                ).also { profileRepository.save(it) }
            }
        }
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())

        // TODO: sendSp24CredentialsToServerUseCase()

        // Launch timetable and substitution plan sync in the app scope so it is
        // not cancelled when the onboarding ViewModel is cleared.
        appScope.launch {
            updateTimetableUseCase(profile.school, forceUpdate = true)
            updateSubstitutionPlanUseCase(
                sp24School = profile.school,
                dates = listOf(LocalDate.now()),
                allowNotification = false
            )
        }
    }
}
