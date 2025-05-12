package plus.vplan.app.feature.onboarding.stage.migrate.a_read.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import plus.vplan.app.App
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.findByIndiwareId
import plus.vplan.app.domain.repository.KeyValueRepository
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.feature.homework.domain.usecase.CreateHomeworkUseCase
import plus.vplan.app.feature.homework.domain.usecase.ToggleTaskDoneUseCase
import plus.vplan.app.feature.onboarding.stage.a_school_search.domain.usecase.UseUnknownSp24SchoolUseCase
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.domain.usecase.CheckCredentialsUseCase
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolDataResult
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolDataState
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolDataStep
import plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase.SetUpSchoolDataUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.GetProfileOptionsUseCase
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase.SelectProfileUseCase
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase.GenerateNewAccessCodeUseCase
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase.MigrationDataReadResult
import plus.vplan.app.feature.onboarding.stage.migrate.a_read.domain.usecase.ReadMigrationDataUseCase
import plus.vplan.app.feature.settings.page.security.ui.GradeProtectLevel
import plus.vplan.app.feature.vpp_id.domain.usecase.AddVppIdUseCase
import kotlin.uuid.Uuid

class ImportScreenViewModel(
    private val readMigrationDataUseCase: ReadMigrationDataUseCase,
    private val checkCredentialsUseCase: CheckCredentialsUseCase,
    private val useUnknownSp24SchoolUseCase: UseUnknownSp24SchoolUseCase,
    private val setUpSchoolDataUseCase: SetUpSchoolDataUseCase,
    private val getProfileOptionsUseCase: GetProfileOptionsUseCase,
    private val selectProfileUseCase: SelectProfileUseCase,
    private val createHomeworkUseCase: CreateHomeworkUseCase,
    private val toggleTaskDoneUseCase: ToggleTaskDoneUseCase,
    private val generateNewAccessCodeUseCase: GenerateNewAccessCodeUseCase,
    private val addVppIdUseCase: AddVppIdUseCase,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val keyValueRepository: KeyValueRepository
) : ViewModel() {
    var state by mutableStateOf(ImportScreenState())
        private set

    var allowInit = true

    companion object {
        var migrationText by mutableStateOf<String?>(null)
    }

    init {
        viewModelScope.launch {
            readMigrationDataUseCase(migrationText!!).let { result ->
                state = state.copy(result = result)
                if (result is MigrationDataReadResult.Valid) {
                    state = state.copy(isLoading = true)
                    if (result.data.settings.protectGrades) keyValueRepository.set(Keys.GRADE_PROTECTION_LEVEL, GradeProtectLevel.Biometric.name)
                    result.data.schools.forEach { school ->
                        useUnknownSp24SchoolUseCase(school.indiwareId.toInt())
                        checkCredentialsUseCase(
                            sp24Id = school.indiwareId.toInt(),
                            username = school.username,
                            password = school.password
                        ).let {
                            if (it !is Response.Success || it.data is Response.Error) {
                                state = state.copy(isLoading = false)
                                return@forEach
                            }
                        }
                        setUpSchoolDataUseCase()
                            .takeWhile { it is SetUpSchoolDataResult.Loading && it.data[SetUpSchoolDataStep.SET_UP_DATA] != SetUpSchoolDataState.DONE }
                            .collect()

                        val profileOptions = getProfileOptionsUseCase().first()
                        school.profiles.forEach { profile ->
                            val profileOption = profileOptions.firstOrNull {
                                ((profile.type == "STUDENT" && it is OnboardingProfile.StudentProfile) ||
                                        (profile.type == "TEACHER" && it is OnboardingProfile.TeacherProfile) ||
                                        (profile.type == "ROOM" && it is OnboardingProfile.RoomProfile)) &&
                                        (profile.entityName == it.name)
                            }
                            if (profileOption == null) {
                                Logger.e("Profile option not found for ${profile.entityName}")
                                return@forEach
                            }
                            selectProfileUseCase(
                                onboardingProfile = profileOption,
                                subjectInstances = if (profileOption !is OnboardingProfile.StudentProfile) emptyMap()
                                else profileOption.subjectInstances.associate { subjectInstance ->
                                    subjectInstance to (profile.defaultLessons.orEmpty().firstOrNull { subjectInstance.indiwareId.orEmpty().endsWith(".${it.vpId}") }?.enabled != false)
                                }
                            )
                            val createdProfile = App.profileSource.getById(Uuid.parseHex(keyValueRepository.get(Keys.CURRENT_PROFILE).first()!!)).getFirstValue()!!
                            profile.homework.orEmpty().forEach forEachHomework@{ rawHomework ->
                                if (createdProfile !is Profile.StudentProfile) return@forEachHomework
                                val homeworkId = createHomeworkUseCase(
                                    tasks = rawHomework.tasks.map { it.task },
                                    isPublic = false,
                                    date = LocalDate.parse(rawHomework.date),
                                    subjectInstance = subjectInstanceRepository.getBySchool(school.id, false).first().findByIndiwareId(rawHomework.vpId.toString()),
                                    selectedFiles = emptyList()
                                ) ?: return@forEachHomework
                                val homework = App.homeworkSource.getById(homeworkId).getFirstValue()!!
                                homework.tasks.first().forEach { task ->
                                    val rawTask = rawHomework.tasks.first { it.task == task.content }
                                    if (task.isDone(createdProfile) != rawTask.isDone) toggleTaskDoneUseCase(task, createdProfile)
                                }
                            }

                            if (profile.vppIdToken != null) {
                                val code = generateNewAccessCodeUseCase(profile.vppIdToken)
                                keyValueRepository.set(Keys.VPP_ID_LOGIN_LINK_TO_PROFILE, createdProfile.id.toHexString())
                                if (code != null) addVppIdUseCase(code)
                            }
                        }
                    }

                    allowInit = false
                    state = state.copy(isLoading = false, isDone = true)
                }
            }
        }.invokeOnCompletion { migrationText = null }
    }
}

data class ImportScreenState(
    val result: MigrationDataReadResult? = null,
    val isLoading: Boolean = false,
    val isDone: Boolean = false
)