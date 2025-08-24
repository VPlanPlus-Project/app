package plus.vplan.app.feature.homework.domain.usecase

import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.data.AliasProvider
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.repository.GroupRepository
import plus.vplan.app.domain.repository.HomeworkRepository
import plus.vplan.app.domain.repository.SubjectInstanceRepository

class EditHomeworkSubjectInstanceUseCase(
    private val homeworkRepository: HomeworkRepository,
    private val subjectInstanceRepository: SubjectInstanceRepository,
    private val groupRepository: GroupRepository
) {
    suspend operator fun invoke(homework: Homework, subjectInstance: SubjectInstance?, profile: Profile.StudentProfile) {
        var group = if (subjectInstance == null) profile.group.getFirstValue() else null
        var subjectInstance = subjectInstance

        if (subjectInstance != null && subjectInstance.aliases.none { it.provider == AliasProvider.Vpp }) {
            subjectInstance = subjectInstanceRepository.findByAlias(subjectInstance.aliases.first(), forceUpdate = true, preferCurrentState = true).getFirstValue() ?: return
            if (subjectInstance.aliases.none { it.provider == AliasProvider.Vpp }) return
        }

        if (group != null && group.aliases.none { it.provider == AliasProvider.Vpp }) {
            group = groupRepository.findByAlias(group.aliases.first(), forceUpdate = true, preferCurrentState = true).getFirstValue() ?: return
            if (group.aliases.none { it.provider == AliasProvider.Vpp }) return
        }
        homeworkRepository.editHomeworkSubjectInstance(homework, subjectInstance, group, profile)
    }
}