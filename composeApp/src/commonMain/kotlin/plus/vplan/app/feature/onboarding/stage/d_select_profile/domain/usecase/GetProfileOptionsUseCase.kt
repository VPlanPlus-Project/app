package plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.SubjectInstanceRepository
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.RoomRepository
import plus.vplan.app.domain.repository.TeacherRepository
import plus.vplan.app.feature.onboarding.domain.repository.OnboardingRepository
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

class GetProfileOptionsUseCase(
    private val onboardingRepository: OnboardingRepository,
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val roomRepository: RoomRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository
) {
    operator fun invoke(): Flow<List<OnboardingProfile>> = channelFlow {
        onboardingRepository.getSchoolId().collectLatest { schoolId ->
            if (schoolId == null) return@collectLatest send(emptyList())
            combine(
                groupRepository.getBySchool(schoolId),
                teacherRepository.getBySchool(schoolId),
                roomRepository.getBySchool(schoolId)
            ) { groups, teachers, rooms ->
                groups.map {
                    OnboardingProfile.StudentProfile(
                        id = it.id,
                        name = it.name,
                        subjectInstances = subjectInstanceRepository.getByGroup(it.id).first().map { App.subjectInstanceSource.getById(it.id).filterIsInstance<CacheState.Done<SubjectInstance>>().first().data }
                    )
                }.sortedBy { it.name.first().isDigit().not().toString() + it.name.takeWhile { n -> n.isDigit() }.padStart(10, '0') + it.name.dropWhile { n -> n.isDigit() }} + teachers.map {
                    OnboardingProfile.TeacherProfile(
                        id = it.id,
                        name = it.name
                    )
                }.sortedBy { it.name } + rooms.map {
                    OnboardingProfile.RoomProfile(
                        id = it.id,
                        name = it.name
                    )
                }.sortedBy { it.name }
            }.collect {
                send(it)
            }
        }
    }
}