package plus.vplan.app.feature.onboarding.stage.d_indiware_base_download.domain.usecase

import kotlinx.coroutines.flow.first
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.IndiwareRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.SchoolRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository

class SetUpSchoolData(
    private val onboardingRepository: OnboardingRepository,
    private val indiwareRepository: IndiwareRepository,
    private val schoolRepository: SchoolRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository
) {
    suspend operator fun invoke(): Boolean {
        val prefix = "Onboarding/${this::class.simpleName}"
        try {
            val sp24Id = onboardingRepository.getSp24OnboardingSchool().first()?.sp24Id?.toString() ?: throw NullPointerException("$prefix sp24Id is null")
            val username = onboardingRepository.getSp24Credentials()?.username ?: throw NullPointerException("$prefix username is null")
            val password = onboardingRepository.getSp24Credentials()?.password ?: throw NullPointerException("$prefix password is null")
            val baseData = indiwareRepository.getBaseData(
                sp24Id = sp24Id,
                username = username,
                password = password
            )
            if (baseData !is Response.Success) throw IllegalStateException("$prefix baseData is not successful: $baseData")

            val schoolId = schoolRepository.getIdFromSp24Id(sp24Id.toInt())
            if (schoolId !is Response.Success) throw IllegalStateException("$prefix school-Lookup by sp24 was not successful: $schoolId")
            val school = (schoolRepository.getWithCachingById(schoolId.data).let {
                if (it is Response.Success) it.data.first() else throw IllegalStateException("$prefix school-Lookup was not successful: $it")
            } ?: return false).let {
                schoolRepository.setSp24Info(
                    school = it,
                    sp24Id = sp24Id.toInt(),
                    username = username,
                    password = password,
                    daysPerWeek = baseData.data.daysPerWeek,
                    studentsHaveFullAccess = baseData.data.studentsHaveFullAccess,
                    downloadMode = baseData.data.downloadMode
                )
                schoolRepository.getById(it.id).first() ?: throw IllegalStateException("Onboarding/${this::class.simpleName}: schoolId ${it.id} not found")
            }

            val classes = groupRepository.getBySchoolWithCaching(school).let {
                if (it is Response.Success) it.data.first() else throw IllegalStateException("$prefix groups-Lookup was not successful: $it")
            }

            val teachers = teacherRepository.getBySchoolWithCaching(school).let {
                if (it is Response.Success) it.data.first() else throw IllegalStateException("$prefix teachers-Lookup was not successful: $it")
            }

            val rooms = roomRepository.getBySchoolWithCaching(school).let {
                if (it is Response.Success) it.data.first() else throw IllegalStateException("$prefix rooms-Lookup was not successful: $it")
            }

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}