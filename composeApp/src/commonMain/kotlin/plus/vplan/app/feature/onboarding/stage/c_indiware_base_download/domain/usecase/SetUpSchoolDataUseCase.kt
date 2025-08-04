@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.onboarding.stage.c_indiware_base_download.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Alias
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.model.Holiday
import plus.vplan.app.domain.model.School
import plus.vplan.app.domain.repository.DayRepository
import plus.vplan.app.domain.repository.GroupDbDto
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomDbDto
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolDbDto
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.TeacherDbDto
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateLessonTimesUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateSubjectInstanceUseCase
import plus.vplan.app.feature.sync.domain.usecase.indiware.UpdateWeeksUseCase
import plus.vplan.lib.sp24.source.Authentication
import kotlin.time.ExperimentalTime

class SetUpSchoolDataUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val indiwareRepository: IndiwareRepository,
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val dayRepository: DayRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val updateWeeksUseCase: UpdateWeeksUseCase,
    private val updateLessonTimesUseCase: UpdateLessonTimesUseCase,
    private val updateSubjectInstanceUseCase: UpdateSubjectInstanceUseCase,
) {
    operator fun invoke(): Flow<SetUpSchoolDataResult> = channelFlow {
        val result = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED }.toMutableMap()
        val trySendResult: suspend () -> Unit = { this@channelFlow.trySend(SetUpSchoolDataResult.Loading(result.toMap())) }
        trySend(SetUpSchoolDataResult.Loading(result.toMap()))
        val prefix = "Onboarding/${this::class.simpleName}"

        val state = onboardingRepository.getState().first()
        require(state.sp24Id != null) { "$prefix sp24Id is null" }
        require(state.username != null) { "$prefix username is null" }
        require(state.password != null) { "$prefix password is null" }

        try {
            val sp24Authentication = Authentication(state.sp24Id.toString(), state.username, state.password)
            val client = indiwareRepository.getSp24Client(authentication = sp24Authentication, true)

            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val schoolName = (client.getSchoolName() as? plus.vplan.lib.sp24.source.Response.Success)
            onboardingRepository.setSchoolName(schoolName?.data)

            val baseData = client.getMobileBaseDataStudent(sp24Authentication)
            if (baseData !is plus.vplan.lib.sp24.source.Response.Success) {
                trySend(SetUpSchoolDataResult.Error("$prefix baseData is not successful: $baseData"))
                return@channelFlow
            }
            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val sp24Alias = Alias(
                provider = AliasProvider.Sp24,
                value = state.sp24Id.toString(),
                version = 1
            )

            val schoolId = schoolRepository.upsert(SchoolDbDto(
                name = schoolName?.data ?: "Unbekannte Schule",
                aliases = listOf(sp24Alias)
            ))

            schoolRepository.setSp24Access(
                schoolId = schoolId,
                sp24Id = state.sp24Id,
                username = state.username,
                password = state.password,
                daysPerWeek = 5,
            )

            val school = schoolRepository.getByLocalId(schoolId).first() as School.Sp24School

            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_HOLIDAYS] = SetUpSchoolDataState.IN_PROGRESS

            val holidays = (client.holiday.getHolidays() as? plus.vplan.lib.sp24.source.Response.Success)?.data

            dayRepository.upsert(holidays.orEmpty().map { Holiday(it, schoolId) })
            result[SetUpSchoolDataStep.GET_HOLIDAYS] = SetUpSchoolDataState.DONE

            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val classes = (client.getAllClassesIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            val classIds = classes.orEmpty().associateWith { group ->
                Alias(
                    provider = AliasProvider.Sp24,
                    value = "${school.sp24Id}/${group.name}",
                    version = 1
                )
            }.map { (group, aliases) ->
                groupRepository.upsert(GroupDbDto(
                    schoolId = schoolId,
                    name = group.name,
                    aliases = listOf(aliases)
                ))
            }

            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val teachers = (client.getAllTeachersIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            teachers.orEmpty().associateWith { teacher ->
                Alias(
                    provider = AliasProvider.Sp24,
                    value = "${school.sp24Id}/${teacher.name}",
                    version = 1
                )
            }.onEach { (teacher, aliases) ->
                teacherRepository.upsert(TeacherDbDto(
                    schoolId = schoolId,
                    name = teacher.name,
                    aliases = listOf(aliases)
                ))
            }.also {
                onboardingRepository.addProfileOptions(it.map { (teacher, alias) ->
                    OnboardingProfile.TeacherProfile(teacher.name, alias)
                })
            }

            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            val rooms = (client.getAllRoomsIntelligent() as? plus.vplan.lib.sp24.source.Response.Success)?.data
            rooms.orEmpty().associateWith { room ->
                Alias(
                    provider = AliasProvider.Sp24,
                    value = "${school.sp24Id}/${room.name}",
                    version = 1
                )
            }.forEach { (room, aliases) ->
                roomRepository.upsert(RoomDbDto(
                    schoolId = schoolId,
                    name = room.name,
                    aliases = listOf(aliases)
                ))
            }

            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_WEEKS] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            result[SetUpSchoolDataStep.GET_WEEKS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_LESSON_TIMES] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()

            updateLessonTimesUseCase(school, client)

            result[SetUpSchoolDataStep.GET_LESSON_TIMES] = SetUpSchoolDataState.DONE

            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.IN_PROGRESS
            trySendResult()
            require(updateWeeksUseCase(school, client) == null) { "Couldn't update weeks" }
            require(updateSubjectInstanceUseCase(school, client) == null) { "Couldn't update subject instances" }

            onboardingRepository.addProfileOptions(classIds.map { classId ->
                val group = groupRepository.getByLocalId(classId).first()!!
                val subjectInstances = subjectInstanceRepository.getByGroup(classId).first()

                OnboardingProfile.StudentProfile(
                    name = group.name,
                    alias = group.aliases.first { it.provider == AliasProvider.Sp24 },
                    subjectInstances = subjectInstances
                )
            })

            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.DONE

            return@channelFlow trySendResult()
        } catch (e: Exception) {
            e.printStackTrace()
            trySend(SetUpSchoolDataResult.Error(e.message ?: "Unknown error"))
            return@channelFlow
        }
    }
}

sealed class SetUpSchoolDataResult {
    data class Loading(val data: Map<SetUpSchoolDataStep, SetUpSchoolDataState> = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED }) : SetUpSchoolDataResult()
    data class Error(val message: String) : SetUpSchoolDataResult() {
        init {
            Logger.e("SetUpSchoolDataResult.Error") { message }
        }
    }
}

enum class SetUpSchoolDataStep {
    DOWNLOAD_BASE_DATA,
    GET_SCHOOL_INFORMATION,
    GET_HOLIDAYS,
    GET_GROUPS,
    GET_TEACHERS,
    GET_ROOMS,
    GET_WEEKS,
    GET_LESSON_TIMES,
    SET_UP_DATA
}

enum class SetUpSchoolDataState {
    NOT_STARTED,
    IN_PROGRESS,
    DONE
}
