package plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.core.analytics.AnalyticsRepository
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
    private val analyticsRepository: AnalyticsRepository,
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

        analyticsRepository.capture(
            event = "CreateProfile",
            properties = mapOf(
                "school_id" to profile.school.aliases.joinToString(),
                "school_name" to profile.school.name,
                "profile_type" to profile.profileType.name,
                "entity_id" to onboardingProfile.alias
            )
        )

        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())

        // Launch timetable and substitution plan sync in the app scope so it is
        // not canceled when the onboarding ViewModel is cleared.
        appScope.launch {
            updateTimetableUseCase.updateTimetableRelatedToDate(
                school = profile.school,
                date = LocalDate.now()
            )
            updateSubstitutionPlanUseCase(
                date = LocalDate.now(),
                sp24School = profile.school,
            )
        }
    }
}
