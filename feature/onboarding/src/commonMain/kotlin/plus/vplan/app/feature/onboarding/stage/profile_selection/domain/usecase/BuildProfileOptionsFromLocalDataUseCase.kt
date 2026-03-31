package plus.vplan.app.feature.onboarding.stage.profile_selection.domain.usecase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import plus.vplan.app.core.data.group.GroupRepository
import plus.vplan.app.core.data.subject_instance.SubjectInstanceRepository
import plus.vplan.app.core.data.teacher.TeacherRepository
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.School
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.lib.sp24.source.NamedEntity
import plus.vplan.lib.sp24.source.SchoolEntityType
import plus.vplan.lib.sp24.source.ValueSource

class BuildProfileOptionsFromLocalDataUseCase(
    private val groupRepository: GroupRepository,
    private val teacherRepository: TeacherRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
) {
    suspend operator fun invoke(school: School.AppSchool): List<OnboardingProfile> =
        withContext(Dispatchers.Default) {
            val groups = groupRepository.getBySchool(school).first()
            val teachers = teacherRepository.getBySchool(school).first()

            val studentProfiles = groups.map { group ->
                val subjectInstances = subjectInstanceRepository.getByGroup(group).first()
                OnboardingProfile.StudentProfile(
                    name = group.name,
                    alias = group.aliases.first { it.provider == AliasProvider.Sp24 },
                    isTrustedName = NamedEntity(name = group.name, type = SchoolEntityType.Class, source = ValueSource.Indexed).isCommon(),
                    subjectInstances = subjectInstances
                )
            }
                .sortedWith(
                    compareBy<OnboardingProfile.StudentProfile, Int?>(nullsLast()) { group -> group.name.takeWhile { it.isDigit() }.ifBlank { null }?.toInt() }
                        .thenBy(nullsLast()) { group -> group.name.dropWhile { it.isDigit() } }
                )

            val teacherProfiles = teachers.map { teacher ->
                OnboardingProfile.TeacherProfile(
                    name = teacher.name,
                    isTrustedName = NamedEntity(name = teacher.name, type = SchoolEntityType.Teacher, source = ValueSource.Indexed).isCommon(),
                    alias = teacher.aliases.first { it.provider == AliasProvider.Sp24 }
                )
            }
                .sortedBy { it.name }

            return@withContext studentProfiles + teacherProfiles
        }
}
