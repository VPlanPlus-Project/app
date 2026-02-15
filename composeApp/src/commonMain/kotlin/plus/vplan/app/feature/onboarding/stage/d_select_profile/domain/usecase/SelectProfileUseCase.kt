package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import plus.vplan.app.capture
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.ProfileRepository
import plus.vplan.app.domain.repository.SubstitutionPlanRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.domain.repository.TimetableRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.profile.domain.usecase.UpdateProfileLessonIndexUseCase
import plus.vplan.app.feature.sync.domain.usecase.fullsync.FullSyncUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateSubstitutionPlanUseCase
import plus.vplan.app.feature.sync.domain.usecase.sp24.UpdateTimetableUseCase
import plus.vplan.app.feature.system.usecase.sp24.SendSp24CredentialsToServerUseCase
import plus.vplan.app.utils.now

class SelectProfileUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val profileRepository: ProfileRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val keyValueRepository: KeyValueRepository,
    private val updateTimetableUseCase: UpdateTimetableUseCase,
    private val updateSubstitutionPlanUseCase: UpdateSubstitutionPlanUseCase,
    private val sendSp24CredentialsToServerUseCase: SendSp24CredentialsToServerUseCase,
    private val updateProfileLessonIndexUseCase: UpdateProfileLessonIndexUseCase,
    private val substitutionPlanRepository: SubstitutionPlanRepository,
    private val timetableRepository: TimetableRepository,
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
        capture(
            event = "CreateProfile",
            properties = mapOf(
                "school_id" to profile.school.aliases.joinToString(),
                "school_name" to profile.school.name,
                "profile_type" to profile.profileType.name,
                "entity_id" to onboardingProfile.alias
            )
        )
        keyValueRepository.set(Keys.CURRENT_PROFILE, profile.id.toHexString())

        sendSp24CredentialsToServerUseCase()

        if (onboardingRepository.getNeedToDownloadLessonData()) {
            val client = onboardingRepository.getSp24Client()!!
            updateTimetableUseCase(profile.school, client, true)
            updateSubstitutionPlanUseCase(profile.school, listOf(LocalDate.now()), client, allowNotification = false)
        } else {
            Logger.i { "Skipping lesson data download as it is not needed." }
        }

        val substitutionPlanVersion = substitutionPlanRepository.getCurrentVersion().first()
        val timetableVersion = timetableRepository.getCurrentVersion().first()
        updateProfileLessonIndexUseCase(profile, substitutionPlanVersion, timetableVersion)

        FullSyncUseCase.isOnboardingRunning = false
    }
}