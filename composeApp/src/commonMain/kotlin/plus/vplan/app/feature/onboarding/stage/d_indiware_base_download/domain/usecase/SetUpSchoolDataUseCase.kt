package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.CourseRepository
import plus.vplan.app.domain.repository.DefaultLessonRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class SetUpSchoolDataUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val indiwareRepository: IndiwareRepository,
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val courseRepository: CourseRepository,
    private val defaultLessonRepository: DefaultLessonRepository
) {
    operator fun invoke(): Flow<SetUpSchoolDataResult> = flow {
        val result = SetUpSchoolDataStep.entries.associateWith { SetUpSchoolDataState.NOT_STARTED }.toMutableMap()
        val emitResult: suspend () -> Unit = { this@flow.emit(SetUpSchoolDataResult.Loading(result.toMap())) }
        emit(SetUpSchoolDataResult.Loading(result.toMap()))
        val prefix = "Onboarding/${this::class.simpleName}"
        try {
            val sp24Id = onboardingRepository.getSp24OnboardingSchool().first()?.sp24Id?.toString()
                ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix sp24Id is null"))
            val username = onboardingRepository.getSp24Credentials()?.username
                ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix username is null"))
            val password = onboardingRepository.getSp24Credentials()?.password
                ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix password is null"))

            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()
            val baseData = indiwareRepository.getBaseData(
                sp24Id = sp24Id,
                username = username,
                password = password
            )
            if (baseData !is Response.Success) return@flow emit(SetUpSchoolDataResult.Error("$prefix baseData is not successful: $baseData"))
            result[SetUpSchoolDataStep.DOWNLOAD_BASE_DATA] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()

            val schoolId = schoolRepository.getIdFromSp24Id(sp24Id.toInt())
            if (schoolId !is Response.Success) throw IllegalStateException("$prefix school-Lookup by sp24 was not successful: $schoolId")
            val school = (schoolRepository.getWithCachingById(schoolId.data).let {
                (it as? Response.Success)?.data?.first() ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix school-Lookup was not successful: $it"))
            }).let {
                schoolRepository.setSp24Info(
                    school = it,
                    sp24Id = sp24Id.toInt(),
                    username = username,
                    password = password,
                    daysPerWeek = baseData.data.daysPerWeek,
                    studentsHaveFullAccess = baseData.data.studentsHaveFullAccess,
                    downloadMode = baseData.data.downloadMode
                )
                onboardingRepository.setSchoolId(it.id)
                schoolRepository.getById(it.id).first()
                    ?: return@flow emit(SetUpSchoolDataResult.Error("Onboarding/${this::class.simpleName}: schoolId ${it.id} not found"))
            }

            result[SetUpSchoolDataStep.GET_SCHOOL_INFORMATION] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()

            val classes = groupRepository.getBySchoolWithCaching(school).let {
                (it as? Response.Success)?.data?.first() ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix groups-Lookup was not successful: $it"))
            }

            result[SetUpSchoolDataStep.GET_GROUPS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()

            val teachers = teacherRepository.getBySchoolWithCaching(school).let {
                (it as? Response.Success)?.data?.first() ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix teachers-Lookup was not successful: $it"))
            }

            result[SetUpSchoolDataStep.GET_TEACHERS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()

            val rooms = roomRepository.getBySchoolWithCaching(school).let {
                (it as? Response.Success)?.data?.first() ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix rooms-Lookup was not successful: $it"))
            }

            result[SetUpSchoolDataStep.GET_ROOMS] = SetUpSchoolDataState.DONE
            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.IN_PROGRESS
            emitResult()

            baseData.data.classes
                .map { it to it.defaultLessons }
                .forEach { (group, defaultLessons) ->
                    defaultLessons
                        .forEach { defaultLesson ->
                            val course = defaultLesson.course?.let { course ->
                                courseRepository.upsert(
                                    id = "sp24.$sp24Id.${group.name}.${course.name}+${course.teacher}",
                                    name = course.name,
                                    groupId = classes.firstOrNull{ it.name == group.name }?.id ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix group ${group.name} not found")),
                                    teacherId = teachers.firstOrNull { it.name == course.teacher }?.id ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix teacher ${course.teacher} not found"))
                                ).firstOrNull() ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix course ${course.name} not found"))
                            }
                            defaultLessonRepository.upsert(
                                id = "sp24.$sp24Id.${group.name}.${defaultLesson.defaultLessonNumber}",
                                subject = defaultLesson.subject,
                                groupId = classes.firstOrNull { it.name == group.name }?.id ?: return@flow emit(SetUpSchoolDataResult.Error("$prefix group ${group.name} not found")),
                                teacherId = teachers.firstOrNull { it.name == defaultLesson.teacher }?.id,
                                courseId = course?.id
                            )
                        }
                }

            result[SetUpSchoolDataStep.SET_UP_DATA] = SetUpSchoolDataState.DONE
            return@flow emitResult()
        } catch (e: Exception) {
            e.printStackTrace()
            return@flow emit(SetUpSchoolDataResult.Error(e.message ?: "Unknown error"))
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
    GET_GROUPS,
    GET_TEACHERS,
    GET_ROOMS,
    SET_UP_DATA
}

enum class SetUpSchoolDataState {
    NOT_STARTED,
    IN_PROGRESS,
    DONE
}